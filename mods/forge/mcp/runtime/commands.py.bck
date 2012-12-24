# -*- coding: utf-8 -*-
"""
Created on Fri Apr  8 16:36:26 2011

@author: ProfMobius, Searge, Fesh0r
@version: v1.2
"""

import sys
import fnmatch
import logging
import os
import shutil
import zipfile
import csv
import re
import subprocess
import ConfigParser
import urllib
import time
import stat
import errno
import shlex
import platform
from hashlib import md5  # pylint: disable-msg=E0611
from contextlib import closing
from textwrap import TextWrapper

from filehandling.srgsexport import writesrgsfromcsvs
from filehandling.srgshandler import parse_srg
from pylibs.annotate_gl_constants import annotate_file
from pylibs.whereis import whereis
from pylibs.jadfix import jadfix
from pylibs.fffix import fffix
from pylibs.cleanup_src import strip_comments, src_cleanup
from pylibs.normlines import normaliselines
from pylibs.normpatch import normalisepatch


CLIENT = 0
SERVER = 1
SIDE_NAME = {CLIENT: 'client', SERVER: 'server'}


class Error(Exception):
    pass


class CalledProcessError(Error):
    def __init__(self, returncode, cmd, output=None):
        super(CalledProcessError, self).__init__()
        self.returncode = returncode
        self.cmd = cmd
        self.output = output

    def __str__(self):
        return "Command '%s' returned non-zero exit status %d" % (self.cmd, self.returncode)


def reallyrmtree(path):
    if not sys.platform.startswith('win'):
        if os.path.exists(path):
            shutil.rmtree(path)
    else:
        i = 0
        try:
            while os.stat(path) and i < 20:
                shutil.rmtree(path, onerror=rmtree_onerror)
                i += 1
        except OSError:
            pass

        # raise OSError if the path still exists even after trying really hard
        try:
            os.stat(path)
        except OSError:
            pass
        else:
            raise OSError(errno.EPERM, "Failed to remove: '" + path + "'", path)


def rmtree_onerror(func, path, _):
    if not os.access(path, os.W_OK):
        os.chmod(path, stat.S_IWUSR)
    time.sleep(0.5)
    try:
        func(path)
    except OSError:
        pass


def filterdirs(src_dir, pattern=None, ignore_dirs=None, append_pattern=False, all_files=False):
    """Return list of subdirs containing given file pattern filtering out ignored dirs"""
    # avoid problems with mutable default parameters
    if ignore_dirs is None:
        ignore_dirs = []
    dirs = []
    for path, dirlist, filelist in os.walk(src_dir, followlinks=True):
        sub_dir = os.path.relpath(path, src_dir)
        test_dirlist = dirlist[:]
        for cur_dir in test_dirlist:
            if os.path.normpath(os.path.join(sub_dir, cur_dir)) in ignore_dirs:
                # if the full subdir is in the ignored package list delete it so that we don't descend into it
                dirlist.remove(cur_dir)
        if pattern is None:
            if all_files:
                dirs.extend([os.path.join(path, f) for f in filelist])
            else:
                dirs.append(path)
        else:
            files = fnmatch.filter(filelist, pattern)
            if files:
                if all_files:
                    dirs.extend([os.path.join(path, f) for f in files])
                elif append_pattern:
                    dirs.append(os.path.join(path, pattern))
                else:
                    dirs.append(path)
    return dirs


def cmdsplit(args):
    if os.sep == '\\':
        args = args.replace('\\', '\\\\')
    return shlex.split(args)


def truncate(text, length):
    if len(text) > length:
        return text[:length] + '...'
    return text


def csv_header(csvfile):
    fieldnames = []
    if os.path.isfile(csvfile):
        with open(csvfile, 'rb') as fh:
            csvreader = csv.DictReader(fh)
            fieldnames = csvreader.fieldnames
    return set(fieldnames)


class Commands(object):
    """Contains the commands and initialisation for a full mcp run"""

    MCPVersion = '7.25'
    _default_config = 'conf/mcp.cfg'
    _version_config = 'conf/version.cfg'

    @classmethod
    def fullversion(cls):
        """Read the version configuration file and return a full version string"""
        full_version = None
        try:
            config = ConfigParser.SafeConfigParser()
            with open(os.path.normpath(cls._version_config)) as fh:
                config.readfp(fh)
            data_version = config.get('VERSION', 'MCPVersion')
            client_version = config.get('VERSION', 'ClientVersion')
            server_version = config.get('VERSION', 'ServerVersion')
            full_version = ' (data: %s, client: %s, server: %s)' % (data_version, client_version, server_version)
        except IOError:
            pass
        except ConfigParser.Error:
            pass

        if full_version is None:
            return cls.MCPVersion
        else:
            return cls.MCPVersion + full_version

    def __init__(self, conffile=None, verify=False, no_patch=False):
        self.conffile = conffile
        self.readconf()
        self.checkfolders()
        self.startlogger()
        self.logger.info('== MCP %s ==', Commands.fullversion())

        if sys.platform.startswith('linux'):
            self.osname = 'linux'
        elif sys.platform.startswith('darwin'):
            self.osname = 'osx'
        elif sys.platform.startswith('win'):
            self.osname = 'win'
        else:
            self.logger.error('OS not supported : %s', sys.platform)
            sys.exit(1)
        self.logger.debug('OS : %s', sys.platform)

        # tell off people running as root as it screws up wine
        if self.osname in ['linux', 'osx']:
            if not os.getuid():  # pylint: disable-msg=E1101
                self.logger.error("!! Please don't run MCP as root !!")
                sys.exit(1)

        self.checkjava()
        self.readcommands(verify, no_patch=no_patch)

    def checkcommand(self, name, command, java=False, single_line=False, check_return=True, error=True):
        self.logger.debug("# %s: '%s'", name, command)
        try:
            if java:
                command = '%s -jar %s' % (self.cmdjava, command)
            output = self.runcmd(command, quiet=True, check_return=check_return)
            if single_line:
                output = output.splitlines()[0]
            output = output.strip()
            self.logger.debug(output)
            return True
        except OSError as ex:
            if error:
                self.logger.error('!! %s check FAILED !!', name)
                self.logger.error(ex)
                sys.exit(1)
        except CalledProcessError as ex:
            output = ex.output
            output = output.strip()
            self.logger.debug(output)
            if error:
                self.logger.error('!! %s check FAILED !!', name)
                self.logger.error(ex)
                sys.exit(1)
        return False

    def readcommands(self, verify=False, no_patch=False):
        if verify:
            self.logger.debug('# VERSION INFO')
            self.logger.debug('python: %s', sys.version)
            self.logger.debug('platform: %s', platform.platform())
            self.checkcommand('java', '%s -version' % self.cmdjava)
            self.checkcommand('javac', '%s -version' % self.cmdjavac)
            self.checkcommand('javac runtime', '%s -J-version' % self.cmdjavac)
            self.checkcommand('retroguard', '%s --version' % self.retroguard, java=True)

        self.exceptor = os.path.normpath(self.config.get('COMMANDS', 'Exceptor'))
        if verify:
            self.checkcommand('mcinjector', '%s --version' % self.exceptor, java=True)

        # verify below along with jad if required
        self.jadretro = os.path.normpath(self.config.get('COMMANDS', 'JadRetro'))

        self.patcher = os.path.normpath(self.config.get('COMMANDS', 'Patcher_%s' % self.osname))
        if verify:
            self.checkcommand('patch', '%s --version' % self.patcher, single_line=True)

        self.has_wine = False
        self.has_jad = False
        self.has_ff = False
        self.has_astyle = False
        if self.osname in ['linux']:
            self.wine = os.path.normpath(self.config.get('COMMANDS', 'Wine'))
            if verify:
                self.has_wine = self.checkcommand('wine', '%s --version' % self.wine, error=False)
            self.astyle = os.path.normpath(self.config.get('COMMANDS', 'AStyle_linux'))
            if verify:
                self.has_astyle = self.checkcommand('astyle', '%s --version' % self.astyle, error=False)
            if self.has_wine:
                self.jad = self.wine + ' ' + os.path.normpath(self.config.get('COMMANDS', 'Jad_win'))
                if not self.has_astyle:
                    self.astyle = self.wine + ' ' + os.path.normpath(self.config.get('COMMANDS', 'AStyle_win'))
                    if verify:
                        self.has_astyle = self.checkcommand('astyle', '%s --version' % self.astyle, error=False)
            else:
                # need to set to string so the below CmdJad stuff doesn't error out
                self.jad = ''
        else:
            self.jad = os.path.normpath(self.config.get('COMMANDS', 'Jad_%s' % self.osname))
            self.astyle = os.path.normpath(self.config.get('COMMANDS', 'AStyle_%s' % self.osname))
            if verify:
                self.has_astyle = self.checkcommand('astyle', '%s --version' % self.astyle, error=False)

        # only check jad and jadretro if we can use it
        if self.jad:
            if verify:
                has_jadretro = self.checkcommand('jadretro', '%s' % self.jadretro, java=True, single_line=True,
                                                 error=False)
                has_jad = self.checkcommand('jad', '%s' % self.jad, single_line=True, check_return=False,
                                            error=False)
                self.has_jad = has_jad and has_jadretro

        self.fernflower = os.path.normpath(self.config.get('COMMANDS', 'Fernflower'))
        if verify:
            self.has_ff = self.checkcommand('fernflower', '%s' % self.fernflower, java=True, single_line=True,
                                            error=False)

        # check requirements
        # windows: all requirements supplied
        # osx: require python and patch, will error out before now anyway so don't need to check further
        # linux: require python, patch and either wine for jad or fernflower, and optionally astyle if wine not present
        if verify:
            reqs = []
            if self.osname in ['linux']:
                if not self.has_wine:
                    if not self.has_ff:
                        self.logger.error('!! Please install either wine or fernflower for decompilation !!')
                        sys.exit(1)
                    if not self.has_astyle:
                        self.logger.error('!! Please install either wine or astyle for source cleanup !!')
                else:
                    reqs.append('wine')
            if self.has_jad:
                reqs.append('jad')
            if self.has_ff:
                reqs.append('ff')
            if self.has_jad_patch:
                reqs.append('jad patches')
            if self.has_ff_patch:
                reqs.append('ff patches')
            if self.has_osx_patch:
                reqs.append('osx patches')
            if self.has_srg:
                reqs.append('srgs')
            if self.has_map_csv:
                reqs.append('map csvs')
            if self.has_name_csv:
                reqs.append('name csvs')
            if self.has_doc_csv:
                reqs.append('doc csvs')
            if self.has_param_csv:
                reqs.append('param csvs')
            if self.has_renumber_csv:
                reqs.append('renumber csv')
            if self.has_astyle:
                reqs.append('astyle')
            if self.has_astyle_cfg:
                reqs.append('astyle config')
            self.logger.info('# found %s', ', '.join(reqs))
            if not self.has_jad_patch and not no_patch:
                self.has_jad = False
            if not self.has_ff_patch and not no_patch:
                self.has_ff = False
            if not self.has_astyle_cfg:
                self.has_astyle = False
            if not self.has_map_csv and not self.has_srg:
                self.logger.error('!! need either srgs or csvs !!')
                sys.exit(1)
            if not self.has_exc:
                self.logger.error('!! need mcinjector configs !!')
                sys.exit(1)
            if not self.has_jad and not self.has_ff:
                self.logger.error('!! need either jad or fernflower available with patches !!')
                sys.exit(1)

        self.cmdpatch = self.config.get('COMMANDS', 'CmdPatch', raw=1) % self.patcher
        self.cmdjad = self.config.get('COMMANDS', 'CmdJad', raw=1) % self.jad
        self.cmdastyle = self.config.get('COMMANDS', 'CmdAStyle', raw=1) % self.astyle
        self.cmdrg = self.config.get('COMMANDS', 'CmdRG', raw=1) % self.cmdjava
        self.cmdrgreobf = self.config.get('COMMANDS', 'CmdRGReobf', raw=1) % self.cmdjava
        self.cmdjadretro = self.config.get('COMMANDS', 'CmdJadretro', raw=1) % (self.cmdjava, self.jadretro)
        self.cmdfernflower = self.config.get('COMMANDS', 'CmdFernflower', raw=1) % (self.cmdjava, self.fernflower)
        self.cmdexceptor = self.config.get('COMMANDS', 'CmdExceptor', raw=1) % (self.cmdjava, self.exceptor)
        self.cmdrecomp = self.config.get('COMMANDS', 'CmdRecomp', raw=1) % self.cmdjavac
        self.cmdstartsrv = self.config.get('COMMANDS', 'CmdStartSrv', raw=1) % self.cmdjava
        self.cmdstartclt = self.config.get('COMMANDS', 'CmdStartClt', raw=1) % self.cmdjava

    def startlogger(self):
        self.logger = logging.getLogger()
        self.logger.setLevel(logging.DEBUG)
        # create file handler which logs even debug messages
        filehandler = logging.FileHandler(filename=self.mcplogfile)
        filehandler.setLevel(logging.DEBUG)
        # create console handler with a higher log level
        consolehandler = logging.StreamHandler()
        consolehandler.setLevel(logging.INFO)
        # File output of everything Warning or above
        errorhandler = logging.FileHandler(filename=self.mcperrlogfile)
        errorhandler.setLevel(logging.WARNING)
        # create formatter and add it to the handlers
        formatterconsole = logging.Formatter('%(message)s')
        consolehandler.setFormatter(formatterconsole)
        formatterfile = logging.Formatter('%(asctime)s - %(message)s', datefmt='%H:%M:%S')
        filehandler.setFormatter(formatterfile)
        errorhandler.setFormatter(formatterfile)
        # add the handlers to logger
        self.logger.addHandler(consolehandler)
        self.logger.addHandler(filehandler)
        self.logger.addHandler(errorhandler)

        # HINT: SECONDARY LOGGER FOR CLIENT & SERVER
        self.loggermc = logging.getLogger('MCRunLog')
        self.loggermc.setLevel(logging.DEBUG)
        chmc = logging.StreamHandler()
        chmc.setLevel(logging.DEBUG)
        formatterconsolemc = logging.Formatter('[%(asctime)s] %(message)s', datefmt='%H:%M:%S')
        chmc.setFormatter(formatterconsolemc)
        # add the handlers to logger
        self.loggermc.addHandler(chmc)

    def readconf(self):
        """Read the configuration file to setup some basic paths"""
        config = ConfigParser.SafeConfigParser()
        try:
            with open(os.path.normpath(self._default_config)) as fh:
                config.readfp(fh)
            if self.conffile is not None:
                config.read(os.path.normpath(self.conffile))
        except IOError:
            logging.error('!! Missing mcp.cfg !!')
            sys.exit(1)
        self.config = config

        # HINT: We read the directories for cleanup
        self.dirtemp = os.path.normpath(config.get('DEFAULT', 'DirTemp'))
        self.dirsrc = os.path.normpath(config.get('DEFAULT', 'DirSrc'))
        self.dirlogs = os.path.normpath(config.get('DEFAULT', 'DirLogs'))
        self.dirbin = os.path.normpath(config.get('DEFAULT', 'DirBin'))
        self.dirjars = os.path.normpath(config.get('DEFAULT', 'DirJars'))
        self.dirreobf = os.path.normpath(config.get('DEFAULT', 'DirReobf'))
        self.dirlib = os.path.normpath(config.get('DEFAULT', 'DirLib'))
        self.dirmodsrc = os.path.normpath(config.get('DEFAULT', 'DirModSrc'))

        # HINT: We read the position of the CSV files
        self.csvclasses = os.path.normpath(config.get('CSV', 'Classes'))
        self.csvmethods = os.path.normpath(config.get('CSV', 'Methods'))
        self.csvfields = os.path.normpath(config.get('CSV', 'Fields'))
        self.csvparams = os.path.normpath(config.get('CSV', 'Params'))
        self.csvnewids = os.path.normpath(config.get('CSV', 'NewIds'))

        # check what csvs we have
        self.has_map_csv = False
        self.has_name_csv = False
        self.has_doc_csv = False
        self.has_param_csv = False
        self.has_renumber_csv = False
        header_classes = csv_header(self.csvclasses)
        header_methods = csv_header(self.csvmethods)
        header_fields = csv_header(self.csvfields)
        header_params = csv_header(self.csvparams)
        header_newids = csv_header(self.csvnewids)
        if set(['notch', 'name', 'package', 'side']) <= header_classes and \
           set(['notch', 'searge', 'notchsig', 'sig', 'classnotch', 'classname', 'package', 'side']) <= header_methods and \
           set(['notch', 'searge', 'classnotch', 'classname', 'package', 'side']) <= header_fields:
            self.has_map_csv = True
        if header_methods >= set(['searge', 'name', 'side']) <= header_fields:
            self.has_name_csv = True
        if header_methods >= set(['searge', 'name', 'desc', 'side']) <= header_fields:
            self.has_doc_csv = True
        if set(['param', 'name', 'side']) <= header_params:
            self.has_param_csv = True
        if set(['client', 'server', 'newid']) <= header_newids:
            self.has_renumber_csv = True

        # HINT: We read the names of the SRG output
        self.srgsconfclient = os.path.normpath(config.get('SRGS', 'ConfClient'))
        self.srgsconfserver = os.path.normpath(config.get('SRGS', 'ConfServer'))
        self.srgsclient = os.path.normpath(config.get('SRGS', 'Client'))
        self.srgsserver = os.path.normpath(config.get('SRGS', 'Server'))
        self.deobsrgclient = os.path.normpath(config.get('SRGS', 'DeobfClient'))
        self.deobsrgserver = os.path.normpath(config.get('SRGS', 'DeobfServer'))
        self.reobsrgclient = os.path.normpath(config.get('SRGS', 'ReobfClient'))
        self.reobsrgserver = os.path.normpath(config.get('SRGS', 'ReobfServer'))

        # do we have full srg files
        self.has_srg = False
        if os.path.isfile(self.srgsconfclient) and os.path.isfile(self.srgsconfserver):
            self.has_srg = True

        # HINT: We read the position of the jar files
        self.dirnatives = os.path.normpath(config.get('JAR', 'DirNatives'))
        self.jarclient = os.path.normpath(config.get('JAR', 'Client'))
        self.jarserver = os.path.normpath(config.get('JAR', 'Server'))
        self.md5jarclt = config.get('JAR', 'MD5Client').lower()
        self.md5jarsrv = config.get('JAR', 'MD5Server').lower()
        jarslwjgl = config.get('JAR', 'LWJGL').split(',')
        self.jarslwjgl = [os.path.normpath(p) for p in jarslwjgl]

        # HINT: We read keys relevant to retroguard
        self.retroguard = os.path.normpath(config.get('RETROGUARD', 'Location'))
        self.rgconfig = os.path.normpath(config.get('RETROGUARD', 'RetroConf'))
        self.rgclientconf = os.path.normpath(config.get('RETROGUARD', 'ClientConf'))
        self.rgserverconf = os.path.normpath(config.get('RETROGUARD', 'ServerConf'))
        self.rgclientout = os.path.normpath(config.get('RETROGUARD', 'ClientOut'))
        self.rgserverout = os.path.normpath(config.get('RETROGUARD', 'ServerOut'))
        self.rgclientlog = os.path.normpath(config.get('RETROGUARD', 'ClientLog'))
        self.rgserverlog = os.path.normpath(config.get('RETROGUARD', 'ServerLog'))
        self.rgclientdeoblog = os.path.normpath(config.get('RETROGUARD', 'ClientDeobLog'))
        self.rgserverdeoblog = os.path.normpath(config.get('RETROGUARD', 'ServerDeobLog'))
        self.rgreobconfig = os.path.normpath(config.get('RETROGUARD', 'RetroReobConf'))
        self.rgclientreobconf = os.path.normpath(config.get('RETROGUARD', 'ClientReobConf'))
        self.rgserverreobconf = os.path.normpath(config.get('RETROGUARD', 'ServerReobConf'))
        self.nullpkg = config.get('RETROGUARD', 'NullPkg')

        # HINT: We read keys relevant to exceptor
        self.xclientconf = os.path.normpath(config.get('EXCEPTOR', 'XClientCfg'))
        self.xserverconf = os.path.normpath(config.get('EXCEPTOR', 'XServerCfg'))
        self.xclientout = os.path.normpath(config.get('EXCEPTOR', 'XClientOut'))
        self.xserverout = os.path.normpath(config.get('EXCEPTOR', 'XServerOut'))
        self.xclientlog = os.path.normpath(config.get('EXCEPTOR', 'XClientLog'))
        self.xserverlog = os.path.normpath(config.get('EXCEPTOR', 'XServerLog'))

        # do we have the exc files
        self.has_exc = False
        if os.path.isfile(self.xclientconf) and os.path.isfile(self.xserverconf):
            self.has_exc = True

        # HINT: We read keys relevant to decompilation
        self.srcclienttmp = os.path.normpath(config.get('DECOMPILE', 'SrcClientTemp'))
        self.srcservertmp = os.path.normpath(config.get('DECOMPILE', 'SrcServerTemp'))
        self.clsclienttmp = os.path.normpath(config.get('DECOMPILE', 'ClsClientTemp'))
        self.clsservertmp = os.path.normpath(config.get('DECOMPILE', 'ClsServerTemp'))
        self.ffsource = config.get('DECOMPILE', 'FFSource')

        # HINT: We read the output directories
        self.binclienttmp = os.path.normpath(config.get('OUTPUT', 'BinClientTemp'))
        self.binservertmp = os.path.normpath(config.get('OUTPUT', 'BinServerTemp'))
        self.srcclient = os.path.normpath(config.get('OUTPUT', 'SrcClient'))
        self.srcserver = os.path.normpath(config.get('OUTPUT', 'SrcServer'))
        self.testclient = config.get('OUTPUT', 'TestClient')
        self.testserver = config.get('OUTPUT', 'TestServer')

        # HINT: Patcher related configs
        self.patchclient = os.path.normpath(config.get('PATCHES', 'PatchClient'))
        self.patchserver = os.path.normpath(config.get('PATCHES', 'PatchServer'))
        self.patchtemp = os.path.normpath(config.get('PATCHES', 'PatchTemp'))
        self.ffpatchclient = os.path.normpath(config.get('PATCHES', 'FFPatchClient'))
        self.ffpatchserver = os.path.normpath(config.get('PATCHES', 'FFPatchServer'))
        self.patchclient_osx = os.path.normpath(config.get('PATCHES', 'PatchClient_osx'))
        self.patchserver_osx = os.path.normpath(config.get('PATCHES', 'PatchServer_osx'))

        # check what patches we have
        self.has_jad_patch = False
        if os.path.isfile(self.patchclient) and os.path.isfile(self.patchserver):
            self.has_jad_patch = True
        self.has_ff_patch = False
        if os.path.isfile(self.ffpatchclient) and os.path.isfile(self.ffpatchserver):
            self.has_ff_patch = True
        self.has_osx_patch = False
        if os.path.isfile(self.patchclient_osx) and os.path.isfile(self.patchserver_osx):
            self.has_osx_patch = True

        # HINT: Recompilation related configs
        self.binclient = os.path.normpath(config.get('RECOMPILE', 'BinClient'))
        self.binserver = os.path.normpath(config.get('RECOMPILE', 'BinServer'))
        self.clientrecomplog = os.path.normpath(config.get('RECOMPILE', 'LogClient'))
        self.serverrecomplog = os.path.normpath(config.get('RECOMPILE', 'LogServer'))
        cpathclient = config.get('RECOMPILE', 'ClassPathClient').split(',')
        self.cpathclient = [os.path.normpath(p) for p in cpathclient]
        self.fixesclient = os.path.normpath(config.get('RECOMPILE', 'ClientFixes'))
        cpathserver = config.get('RECOMPILE', 'ClassPathServer').split(',')
        self.cpathserver = [os.path.normpath(p) for p in cpathserver]
        if config.has_option('RECOMPILE', 'FixSound'):
            self.fixsound = config.get('RECOMPILE', 'FixSound')
        else:
            self.fixsound = None
        self.fixstart = config.get('RECOMPILE', 'FixStart')
        self.ignorepkg = config.get('RECOMPILE', 'IgnorePkg').split(',')

        # HINT: Reobf related configs
        self.md5client = os.path.normpath(config.get('REOBF', 'MD5Client'))
        self.md5server = os.path.normpath(config.get('REOBF', 'MD5Server'))
        self.md5reobfclient = os.path.normpath(config.get('REOBF', 'MD5PreReobfClient'))
        self.md5reobfserver = os.path.normpath(config.get('REOBF', 'MD5PreReobfServer'))
        self.rgclientrolog = os.path.normpath(config.get('REOBF', 'ClientRoLog'))
        self.rgserverrolog = os.path.normpath(config.get('REOBF', 'ServerRoLog'))
        self.cmpjarclient = os.path.normpath(config.get('REOBF', 'RecompJarClient'))
        self.cmpjarserver = os.path.normpath(config.get('REOBF', 'RecompJarServer'))
        self.reobfjarclient = os.path.normpath(config.get('REOBF', 'ObfJarClient'))
        self.reobfjarserver = os.path.normpath(config.get('REOBF', 'ObfJarServer'))
        self.dirreobfclt = os.path.normpath(config.get('REOBF', 'ReobfDirClient'))
        self.dirreobfsrv = os.path.normpath(config.get('REOBF', 'ReobfDirServer'))
        self.clientreoblog = os.path.normpath(config.get('REOBF', 'ReobfClientLog'))
        self.serverreoblog = os.path.normpath(config.get('REOBF', 'ReobfServerLog'))

        self.mcplogfile = os.path.normpath(config.get('MCP', 'LogFile'))
        self.mcperrlogfile = os.path.normpath(config.get('MCP', 'LogFileErr'))
        if config.has_option('MCP', 'UpdateUrl'):
            updateurl = config.get('MCP', 'UpdateUrl')
            self.updateurl = updateurl.format(version=Commands.MCPVersion)
        else:
            self.updateurl = None
        ignoreupdate = config.get('MCP', 'IgnoreUpdate').split(',')
        self.ignoreupdate = [os.path.normpath(p) for p in ignoreupdate]
        self.mcprgindex = os.path.normpath(config.get('MCP', 'RGIndex'))
        self.mcpparamindex = os.path.normpath(config.get('MCP', 'ParamIndex'))

        # Get changed source
        self.srcmodclient = os.path.normpath(config.get('GETMODSOURCE', 'OutSRCClient'))
        self.srcmodserver = os.path.normpath(config.get('GETMODSOURCE', 'OutSRCServer'))

        # Source formatter
        self.astyleconf = os.path.normpath(config.get('ASTYLE', 'AstyleConfig'))

        # do we have a config for astyle
        self.has_astyle_cfg = False
        if os.path.isfile(self.astyleconf):
            self.has_astyle_cfg = True

    def creatergcfg(self, reobf=False, keep_lvt=False, keep_generics=False, rg_update=False):
        """Create the files necessary for RetroGuard"""
        if reobf:
            rgconfig_file = self.rgreobconfig
            rgclientconf_file = self.rgclientreobconf
            rgserverconf_file = self.rgserverreobconf
        else:
            rgconfig_file = self.rgconfig
            rgclientconf_file = self.rgclientconf
            rgserverconf_file = self.rgserverconf

        with open(rgconfig_file, 'w') as rgout:
            rgout.write('.option Application\n')
            rgout.write('.option Applet\n')
            rgout.write('.option Repackage\n')
            rgout.write('.option Annotations\n')
            rgout.write('.option MapClassString\n')
            rgout.write('.attribute LineNumberTable\n')
            rgout.write('.attribute EnclosingMethod\n')
            rgout.write('.attribute Deprecated\n')
            if keep_lvt:
                # may cause issues trying to patch/recompile when decompiling mods
                rgout.write('.attribute LocalVariableTable\n')
            if keep_generics:
                # still not very reliable even with rg fixed
                rgout.write('.option Generic\n')
                rgout.write('.attribute LocalVariableTypeTable\n')
            if reobf:
                # this is obfuscated in vanilla and breaks the patches
                rgout.write('.attribute SourceFile\n')

        with open(rgclientconf_file, 'w') as rgout:
            rgout.write('%s = %s\n' % ('input', self.jarclient))
            rgout.write('%s = %s\n' % ('output', self.rgclientout))
            rgout.write('%s = %s\n' % ('reobinput', self.cmpjarclient))
            rgout.write('%s = %s\n' % ('reoboutput', self.reobfjarclient))
            if reobf:
                rgout.write('%s = %s\n' % ('script', self.rgreobconfig))
                rgout.write('%s = %s\n' % ('log', self.rgclientrolog))
            else:
                rgout.write('%s = %s\n' % ('script', self.rgconfig))
                rgout.write('%s = %s\n' % ('log', self.rgclientlog))
            rgout.write('%s = %s\n' % ('deob', self.srgsclient))
            rgout.write('%s = %s\n' % ('reob', self.reobsrgclient))
            rgout.write('%s = %s\n' % ('nplog', self.rgclientdeoblog))
            rgout.write('%s = %s\n' % ('rolog', self.clientreoblog))
            rgout.write('%s = %s\n' % ('verbose', '0'))
            rgout.write('%s = %s\n' % ('quiet', '1'))
            if rg_update:
                rgout.write('%s = %s\n' % ('fullmap', '1'))
                rgout.write('%s = %s\n' % ('startindex', self.mcprgindex))
            else:
                rgout.write('%s = %s\n' % ('fullmap', '0'))
                rgout.write('%s = %s\n' % ('startindex', '0'))
            for pkg in self.ignorepkg:
                rgout.write('%s = %s\n' % ('protectedpackage', pkg))

        with open(rgserverconf_file, 'w') as rgout:
            rgout.write('%s = %s\n' % ('startindex', '0'))
            rgout.write('%s = %s\n' % ('input', self.jarserver))
            rgout.write('%s = %s\n' % ('output', self.rgserverout))
            rgout.write('%s = %s\n' % ('reobinput', self.cmpjarserver))
            rgout.write('%s = %s\n' % ('reoboutput', self.reobfjarserver))
            if reobf:
                rgout.write('%s = %s\n' % ('script', self.rgreobconfig))
                rgout.write('%s = %s\n' % ('log', self.rgserverrolog))
            else:
                rgout.write('%s = %s\n' % ('script', self.rgconfig))
                rgout.write('%s = %s\n' % ('log', self.rgserverlog))
            rgout.write('%s = %s\n' % ('deob', self.srgsserver))
            rgout.write('%s = %s\n' % ('reob', self.reobsrgserver))
            rgout.write('%s = %s\n' % ('nplog', self.rgserverdeoblog))
            rgout.write('%s = %s\n' % ('rolog', self.serverreoblog))
            rgout.write('%s = %s\n' % ('verbose', '0'))
            rgout.write('%s = %s\n' % ('quiet', '1'))
            if rg_update:
                rgout.write('%s = %s\n' % ('fullmap', '1'))
                rgout.write('%s = %s\n' % ('startindex', self.mcprgindex))
            else:
                rgout.write('%s = %s\n' % ('fullmap', '0'))
                rgout.write('%s = %s\n' % ('startindex', '0'))
            for pkg in self.ignorepkg:
                rgout.write('%s = %s\n' % ('protectedpackage', pkg))

    def createsrgs(self, side, use_srg=False):
        """Write the srgs files."""
        sidelk = {CLIENT: self.srgsclient, SERVER: self.srgsserver}
        srglk = {CLIENT: self.srgsconfclient, SERVER: self.srgsconfserver}

        if use_srg:
            if not self.has_srg:
                self.logger.error('!! srgs not found !!')
                sys.exit(1)
            shutil.copyfile(srglk[side], sidelk[side])
        else:
            if not self.has_map_csv:
                self.logger.error('!! csvs not found !!')
                sys.exit(1)
            fixes = [self.fixstart]
            if self.fixsound:
                fixes.append(self.fixsound)
            writesrgsfromcsvs(self.csvclasses, self.csvmethods, self.csvfields, sidelk[side],
                              side, fixes)

    def checkjava(self):
        """Check for java and setup the proper directory if needed"""
        results = []
        if self.osname == 'win':
            if not results:
                import _winreg

                for flag in [_winreg.KEY_WOW64_64KEY, _winreg.KEY_WOW64_32KEY]:
                    try:
                        k = _winreg.OpenKey(_winreg.HKEY_LOCAL_MACHINE, r'Software\JavaSoft\Java Development Kit', 0,
                                            _winreg.KEY_READ | flag)
                        version, _ = _winreg.QueryValueEx(k, 'CurrentVersion')
                        k.Close()
                        k = _winreg.OpenKey(_winreg.HKEY_LOCAL_MACHINE,
                                            r'Software\JavaSoft\Java Development Kit\%s' % version, 0,
                                            _winreg.KEY_READ | flag)
                        path, _ = _winreg.QueryValueEx(k, 'JavaHome')
                        k.Close()
                        path = os.path.join(str(path), 'bin')
                        self.runcmd('"%s" -version' % os.path.join(path, 'javac'), quiet=True)
                        results.append(path)
                    except (CalledProcessError, OSError):
                        pass
            if not results:
                try:
                    self.runcmd('javac -version', quiet=True)
                    results.append('')
                except (CalledProcessError, OSError):
                    pass
            if not results and 'ProgramW6432' in os.environ:
                results.extend(whereis('javac.exe', os.environ['ProgramW6432']))
            if not results and 'ProgramFiles' in os.environ:
                results.extend(whereis('javac.exe', os.environ['ProgramFiles']))
            if not results and 'ProgramFiles(x86)' in os.environ:
                results.extend(whereis('javac.exe', os.environ['ProgramFiles(x86)']))
        elif self.osname in ['linux', 'osx']:
            if not results:
                try:
                    self.runcmd('javac -version', quiet=True)
                    results.append('')
                except (CalledProcessError, OSError):
                    pass
            if not results:
                results.extend(whereis('javac', '/usr/bin'))
            if not results:
                results.extend(whereis('javac', '/usr/local/bin'))
            if not results:
                results.extend(whereis('javac', '/opt'))
        if not results:
            self.logger.error('Java JDK is not installed ! Please install java JDK from http://java.oracle.com')
            sys.exit(1)
        self.cmdjavac = '"%s"' % os.path.join(results[0], 'javac')
        self.cmdjava = '"%s"' % os.path.join(results[0], 'java')

    def checkjars(self, side):
        jarlk = {CLIENT: self.jarclient, SERVER: self.jarserver}
        md5jarlk = {CLIENT: self.md5jarclt, SERVER: self.md5jarsrv}

        if not os.path.exists(jarlk[side]):
            return False
        with open(jarlk[side], 'rb') as fh:
            md5jar = md5(fh.read()).hexdigest()
        self.logger.debug('%s md5: %s', SIDE_NAME[side], md5jar)
        if md5jar != md5jarlk[side]:
            self.logger.warning('!! Modified jar detected. Unpredictable results !!')
        if side == CLIENT:
            fail = False
            for jar in self.jarslwjgl:
                if not os.path.exists(jar):
                    self.logger.error('!! %s not found !!' % jar)
                    fail = True
            if not os.path.exists(self.dirnatives):
                self.logger.error('!! %s not found !!' % self.dirnatives)
                fail = True
            if fail:
                self.logger.error('!! LWJGL check FAILED. Make sure to copy the entire .minecraft/bin folder into jars !!')
                sys.exit(1)
        return True

    def checksourcedir(self, side):
        srclk = {CLIENT: self.srcclient, SERVER: self.srcserver}
        srcdir = os.path.join(srclk[side], os.path.normpath(self.ffsource))

        if not os.path.exists(srcdir):
            return False
        return True

    def checksources(self, side):
        srclk = {CLIENT: self.srcclient, SERVER: self.srcserver}
        testlk = {CLIENT: self.testclient, SERVER: self.testserver}

        if not os.path.exists(os.path.join(srclk[side], os.path.normpath(testlk[side] + '.java'))):
            return False
        return True

    def checkbins(self, side):
        binlk = {CLIENT: self.binclient, SERVER: self.binserver}
        testlk = {CLIENT: self.testclient, SERVER: self.testserver}

        if not os.path.exists(os.path.join(binlk[side], os.path.normpath(testlk[side] + '.class'))):
            return False
        return True

    def checkmd5s(self, side, reobf=False):
        if not reobf:
            md5lk = {CLIENT: self.md5client, SERVER: self.md5server}
        else:
            md5lk = {CLIENT: self.md5reobfclient, SERVER: self.md5reobfserver}

        if not os.path.isfile(md5lk[side]):
            return False
        return True

    def checkfolders(self):
        if not os.path.exists(self.dirtemp):
            os.makedirs(self.dirtemp)
        if not os.path.exists(self.dirsrc):
            os.makedirs(self.dirsrc)
        if not os.path.exists(self.dirlogs):
            os.makedirs(self.dirlogs)
        if not os.path.exists(self.dirbin):
            os.makedirs(self.dirbin)
        if not os.path.exists(self.dirreobf):
            os.makedirs(self.dirreobf)
        if not os.path.exists(self.dirlib):
            os.makedirs(self.dirlib)

    def checkupdates(self, silent=False):
        # updates disabled?
        if not self.updateurl:
            if not silent:
                self.logger.debug('Updates disabled')
            return []

        # HINT: Each local entry is of the form dict[filename]=(md5,modificationtime)
        md5lcldict = {}
        files = filterdirs('.', ignore_dirs=self.ignoreupdate, all_files=True)
        for trgfile in files:
            cur_file = os.path.normpath(trgfile)
            with open(cur_file, 'rb') as fh:
                md5_file = md5(fh.read()).hexdigest()
            md5lcldict[cur_file] = (md5_file, os.stat(cur_file).st_mtime)
        try:
            update_url = self.updateurl + 'mcp.md5'
            listfh = urllib.urlopen(update_url)
            if listfh.getcode() != 200:
                return []
            md5srvlist = listfh.readlines()
            md5srvdict = {}
        except IOError:
            return []

        # HINT: Each remote entry is of the form dict[filename]=(md5,modificationtime,action)
        for entry in md5srvlist:
            md5srvdict[entry.split()[0]] = (entry.split()[1], float(entry.split()[2]), entry.split()[3])

        results = []
        for key, value in md5srvdict.items():
            cur_file = os.path.normpath(key)
            # HINT: If the remote entry is not in the local table, append
            if cur_file not in md5lcldict:
                results.append([key, value[0], value[1], value[2]])
                continue

            # HINT: If the remote entry has a different MD5 checksum and modtime is > local entry modtime
            if md5lcldict[cur_file][0] != value[0] and md5lcldict[cur_file][1] < value[1]:
                results.append([key, value[0], value[1], value[2]])

        if results and not silent:
            self.logger.warning('!! Updates available. Please run updatemcp to get them. !!')

        return results

    def cleanbindirs(self, side):
        pathbinlk = {CLIENT: self.binclient, SERVER: self.binserver}

        for path, _, filelist in os.walk(pathbinlk[side]):
            for bin_file in fnmatch.filter(filelist, '*.class'):
                os.remove(os.path.normpath(os.path.join(path, bin_file)))

    def cleanreobfdir(self, side):
        outpathlk = {CLIENT: self.dirreobfclt, SERVER: self.dirreobfsrv}

        reallyrmtree(outpathlk[side])
        os.makedirs(outpathlk[side])

    def applyrg(self, side, reobf=False):
        """Apply rg to the given side"""
        rgcplk = {CLIENT: self.cpathclient, SERVER: self.cpathserver}
        if reobf:
            rgcmd = self.cmdrgreobf
            rgconflk = {CLIENT: self.rgclientreobconf, SERVER: self.rgserverreobconf}
            rgdeoblog = None
            deobsrg = None
            reobsrg = None
        else:
            rgcmd = self.cmdrg
            rgconflk = {CLIENT: self.rgclientconf, SERVER: self.rgserverconf}
            rgdeoblog = {CLIENT: self.rgclientdeoblog, SERVER: self.rgserverdeoblog}
            deobsrg = {CLIENT: self.deobsrgclient, SERVER: self.deobsrgserver}
            reobsrg = {CLIENT: self.reobsrgclient, SERVER: self.reobsrgserver}

        # add retroguard.jar to copy of client or server classpath
        rgcp = [self.retroguard] + rgcplk[side]
        rgcp = os.pathsep.join(rgcp)
        forkcmd = rgcmd.format(classpath=rgcp, conffile=rgconflk[side])
        try:
            self.runcmd(forkcmd)
            if not reobf:
                shutil.copyfile(rgdeoblog[side], deobsrg[side])
                shutil.copyfile(deobsrg[side], reobsrg[side])
        except CalledProcessError as ex:
            self.logger.error('')
            self.logger.error('== ERRORS FOUND ==')
            self.logger.error('')
            for line in ex.output.splitlines():
                if line.strip():
                    if line[0] != '#':
                        self.logger.error(line)
            self.logger.error('==================')
            self.logger.error('')
            raise

    def applyff(self, side):
        """Apply fernflower to the given side"""
        pathclslk = {CLIENT: self.clsclienttmp, SERVER: self.clsservertmp}
        pathsrclk = {CLIENT: self.srcclienttmp, SERVER: self.srcservertmp}

        # HINT: We delete the old temp source folder and recreate it
        reallyrmtree(pathsrclk[side])
        os.makedirs(pathsrclk[side])

        forkcmd = self.cmdfernflower.format(indir=pathclslk[side], outdir=pathsrclk[side])
        self.runcmd(forkcmd)

    def applyexceptor(self, side, exc_update=False):
        """Apply exceptor to the given side"""
        excinput = {CLIENT: self.rgclientout, SERVER: self.rgserverout}
        excoutput = {CLIENT: self.xclientout, SERVER: self.xserverout}
        excconf = {CLIENT: self.xclientconf, SERVER: self.xserverconf}
        exclog = {CLIENT: self.xclientlog, SERVER: self.xserverlog}

        forkcmd = self.cmdexceptor.format(input=excinput[side], output=excoutput[side], conf=excconf[side],
                                          log=exclog[side])
        if exc_update:
            forkcmd += ' %s.exc %s' % (exclog[side], self.mcpparamindex)
        self.runcmd(forkcmd)

    def applyjadretro(self, side):
        """Apply jadretro to the class output directory"""
        pathclslk = {CLIENT: self.clsclienttmp, SERVER: self.clsservertmp}

        ignoredirs = [os.path.normpath(p) for p in self.ignorepkg]
        pkglist = filterdirs(pathclslk[side], '*.class', ignore_dirs=ignoredirs)
        dirs = ' '.join(pkglist)
        forkcmd = self.cmdjadretro.format(targetdir=dirs)
        self.runcmd(forkcmd)

    def applyjad(self, side):
        """Decompile the code using jad"""
        pathclslk = {CLIENT: self.clsclienttmp, SERVER: self.clsservertmp}
        pathsrclk = {CLIENT: self.srcclienttmp, SERVER: self.srcservertmp}

        # HINT: We delete the old temp source folder and recreate it
        reallyrmtree(pathsrclk[side])
        os.makedirs(pathsrclk[side])

        ignoredirs = [os.path.normpath(p) for p in self.ignorepkg]
        pkglist = filterdirs(pathclslk[side], '*.class', ignore_dirs=ignoredirs, append_pattern=True)
        outdir = pathsrclk[side]
        # on linux with wine we need to use \\ as a directory seperator
        if self.cmdjad[:4] == 'wine':
            pkglist = [p.replace(os.sep, '\\\\') for p in pkglist]
            outdir = outdir.replace(os.sep, '\\\\')
        dirs = ' '.join(pkglist)
        forkcmd = self.cmdjad.format(outdir=outdir, classes=dirs)
        self.runcmd(forkcmd)

    def process_jadfixes(self, side):
        """Fix up some JAD miscompiles"""
        pathsrclk = {CLIENT: self.srcclient, SERVER: self.srcserver}

        jadfix(pathsrclk[side])

    def process_fffixes(self, side):
        """Clean up fernflower output"""
        pathsrclk = {CLIENT: self.srcclient, SERVER: self.srcserver}

        fffix(pathsrclk[side])

    def applypatches(self, side, use_ff=False, use_osx=False):
        """Applies the patches to the src directory"""
        pathsrclk = {CLIENT: self.srcclient, SERVER: self.srcserver}
        if use_ff:
            patchlk = {CLIENT: self.ffpatchclient, SERVER: self.ffpatchserver}
        elif use_osx:
            patchlk = {CLIENT: self.patchclient_osx, SERVER: self.patchserver_osx}
        else:
            patchlk = {CLIENT: self.patchclient, SERVER: self.patchserver}

        if use_ff:
            if not self.has_ff_patch:
                self.logger.error('!! Missing ff patches. Aborting !!')
                sys.exit(1)
        elif use_osx:
            if not self.has_osx_patch:
                self.logger.warning('!! Missing osx patches. Aborting !!')
                return False
        else:
            if not self.has_jad_patch:
                self.logger.error('!! Missing jad patches. Aborting !!')
                sys.exit(1)

        # HINT: Here we transform the patches to match the directory separator of the specific platform
        # also normalise lineendings to platform default to keep patch happy
        normalisepatch(patchlk[side], self.patchtemp)
        patchfile = os.path.relpath(self.patchtemp, pathsrclk[side])
        forkcmd = self.cmdpatch.format(srcdir=pathsrclk[side], patchfile=patchfile)
        try:
            self.runcmd(forkcmd)
        except CalledProcessError as ex:
            self.logger.warning('')
            self.logger.warning('== ERRORS FOUND ==')
            if side == CLIENT and not use_ff:
                self.logger.warning('When decompiling with ModLoader a single hunk failure in RenderBlocks is expected '
                                    'and is not a problem')
            self.logger.warning('')
            for line in ex.output.splitlines():
                if 'saving rejects' in line:
                    self.logger.warning(line)
            self.logger.warning('==================')
            self.logger.warning('')

    def recompile(self, side):
        """Recompile the sources and produce the final bins"""
        cplk = {CLIENT: self.cpathclient, SERVER: self.cpathserver}
        pathbinlk = {CLIENT: self.binclient, SERVER: self.binserver}
        pathsrclk = {CLIENT: self.srcclient, SERVER: self.srcserver}
        pathlog = {CLIENT: self.clientrecomplog, SERVER: self.serverrecomplog}

        if not os.path.exists(pathbinlk[side]):
            os.makedirs(pathbinlk[side])

        # HINT: We create the list of source directories based on the list of packages
        # on windows we just pass wildcards, otherwise we pass the full file list
        if self.osname == 'win':
            all_files = False
            append_pattern = True
        else:
            all_files = True
            append_pattern = False
        pkglist = filterdirs(pathsrclk[side], '*.java', append_pattern=append_pattern, all_files=all_files)
        dirs = ' '.join(pkglist)
        classpath = os.pathsep.join(cplk[side])
        forkcmd = self.cmdrecomp.format(classpath=classpath, sourcepath=pathsrclk[side], outpath=pathbinlk[side],
                                        pkgs=dirs)
        try:
            self.runcmd(forkcmd, log_file=pathlog[side])
        except CalledProcessError as ex:
            self.logger.error('')
            self.logger.error('== ERRORS FOUND ==')
            self.logger.error('')
            for line in ex.output.splitlines():
                if line.strip():
                    if line[0] != '[' and line[0:4] != 'Note':
                        self.logger.error(line)
                        if '^' in line:
                            self.logger.error('')
            self.logger.error('==================')
            self.logger.error('')
            raise

    def startserver(self):
        classpath = [self.binserver] + self.cpathserver
        classpath = [os.path.join('..', p) for p in classpath]
        classpath = os.pathsep.join(classpath)
        os.chdir(self.dirjars)
        forkcmd = self.cmdstartsrv.format(classpath=classpath)
        self.runmc(forkcmd)

    def startclient(self):
        classpath = [self.binclient] + self.cpathclient
        classpath = [os.path.join('..', p) for p in classpath]
        classpath = os.pathsep.join(classpath)
        natives = os.path.join('..', self.dirnatives)
        os.chdir(self.dirjars)
        forkcmd = self.cmdstartclt.format(classpath=classpath, natives=natives)
        self.runmc(forkcmd)

    def runcmd(self, forkcmd, quiet=False, check_return=True, log_file=None):
        forklist = cmdsplit(forkcmd)
        if not quiet:
            self.logger.debug("runcmd: '%s'", truncate(forkcmd, 500))
            self.logger.debug("shlex: %s", truncate(str(forklist), 500))
        process = subprocess.Popen(forklist, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, bufsize=-1)
        output, _ = process.communicate()
        if log_file is not None:
            with open(log_file, 'w') as log:
                log.write(output)
        if not quiet:
            for line in output.splitlines():
                self.logger.debug(line)
        if process.returncode:
            if not quiet:
                self.logger.error("'%s' failed : %d", truncate(forkcmd, 100), process.returncode)
            if check_return:
                raise CalledProcessError(process.returncode, forkcmd, output)
        return output

    def runmc(self, forkcmd, quiet=False, check_return=True):
        forklist = cmdsplit(forkcmd)
        if not quiet:
            self.logger.debug("runmc: '%s'", truncate(forkcmd, 500))
            self.logger.debug("shlex: %s", truncate(str(forklist), 500))
        output = ''
        process = subprocess.Popen(forklist, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, bufsize=1)
        while process.poll() is None:
            line = process.stdout.readline()
            if line:
                line = line.rstrip()
                output += line
                if not quiet:
                    self.loggermc.debug(line)
        if process.returncode:
            if not quiet:
                self.logger.error("'%s' failed : %d", truncate(forkcmd, 100), process.returncode)
            if check_return:
                raise CalledProcessError(process.returncode, forkcmd, output)
        return output

    def extractjar(self, side):
        """Unzip the jar file to the bin directory defined in the config file"""
        pathbinlk = {CLIENT: self.binclienttmp, SERVER: self.binservertmp}
        jarlk = {CLIENT: self.xclientout, SERVER: self.xserverout}

        # HINT: We delete the specific side directory and recreate it
        reallyrmtree(pathbinlk[side])
        os.makedirs(pathbinlk[side])

        # HINT: We extract the jar to the right location
        with closing(zipfile.ZipFile(jarlk[side])) as zipjar:
            zipjar.extractall(pathbinlk[side])

    def copycls(self, side):
        """Copy the class files to the temp directory defined in the config file"""
        pathbinlk = {CLIENT: self.binclienttmp, SERVER: self.binservertmp}
        pathclslk = {CLIENT: self.clsclienttmp, SERVER: self.clsservertmp}

        # HINT: We delete the specific side directory and recreate it
        reallyrmtree(pathclslk[side])
        os.makedirs(pathclslk[side])

        ignore_dirs = [os.path.normpath(p) for p in self.ignorepkg]
        files = filterdirs(pathbinlk[side], '*.class', ignore_dirs=ignore_dirs, all_files=True)
        for src_file in files:
            sub_dir = os.path.relpath(os.path.dirname(src_file), pathbinlk[side])
            dest_file = os.path.join(pathclslk[side], sub_dir, os.path.basename(src_file))
            if not os.path.exists(os.path.dirname(dest_file)):
                os.makedirs(os.path.dirname(dest_file))
            shutil.copy(src_file, dest_file)

    def copysrc(self, side):
        """Copy the source files to the src directory defined in the config file"""
        pathsrctmplk = {CLIENT: self.srcclienttmp, SERVER: self.srcservertmp}
        pathsrclk = {CLIENT: self.srcclient, SERVER: self.srcserver}

        # HINT: We check if the top output directory exists. If not, we create it
        if not os.path.exists(pathsrclk[side]):
            os.makedirs(pathsrclk[side])

        # HINT: copy source to final dir, fixing line endings
        self.copyandfixsrc(pathsrctmplk[side], pathsrclk[side])

        # HINT: copy Start and soundfix to source dir
        if side == CLIENT:
            normaliselines(os.path.join(self.fixesclient, self.fixstart + '.java'),
                           os.path.join(pathsrclk[side], self.fixstart + '.java'))
            if self.fixsound:
                normaliselines(os.path.join(self.fixesclient, self.fixsound + '.java'),
                               os.path.join(pathsrclk[side], self.fixsound + '.java'))

    def copyandfixsrc(self, src_dir, dest_dir):
        src_dir = os.path.normpath(src_dir)
        dest_dir = os.path.normpath(dest_dir)
        ignore_dirs = [os.path.normpath(p) for p in self.ignorepkg]
        files = filterdirs(src_dir, '*.java', ignore_dirs=ignore_dirs, all_files=True)
        for src_file in files:
            sub_dir = os.path.relpath(os.path.dirname(src_file), src_dir)
            dest_file = os.path.join(dest_dir, sub_dir, os.path.basename(src_file))
            if not os.path.exists(os.path.dirname(dest_file)):
                os.makedirs(os.path.dirname(dest_file))
            # normalise lineendings to platform default, to keep patch happy
            normaliselines(src_file, dest_file)

    def process_rename(self, side):
        """Rename the sources using the CSV data"""
        pathsrclk = {CLIENT: self.srcclient, SERVER: self.srcserver}
        reoblk = {CLIENT: self.reobsrgclient, SERVER: self.reobsrgserver}

        if not self.has_name_csv:
            self.logger.warning('!! renaming disabled due to no csvs !!')
            return False

        # HINT: We read the relevant CSVs
        names = {'methods': {}, 'fields': {}, 'params': {}}
        with open(self.csvmethods, 'rb') as fh:
            methodsreader = csv.DictReader(fh)
            for row in methodsreader:
                if int(row['side']) == side:
                    if row['name'] != row['searge']:
                        names['methods'][row['searge']] = row['name']
        with open(self.csvfields, 'rb') as fh:
            fieldsreader = csv.DictReader(fh)
            for row in fieldsreader:
                if int(row['side']) == side:
                    if row['name'] != row['searge']:
                        names['fields'][row['searge']] = row['name']
        if self.has_param_csv:
            with open(self.csvparams, 'rb') as fh:
                paramsreader = csv.DictReader(fh)
                for row in paramsreader:
                    if int(row['side']) == side:
                        names['params'][row['param']] = row['name']

        regexps = {
            'methods': re.compile(r'func_[0-9]+_[a-zA-Z_]+'),
            'fields': re.compile(r'field_[0-9]+_[a-zA-Z_]+'),
            'params': re.compile(r'p_[\w]+_\d+_'),
        }

        def updatefile(src_file):
            tmp_file = src_file + '.tmp'
            with open(src_file, 'r') as fh:
                buf = fh.read()
            for group in ['methods', 'fields', 'params']:
                def mapname(match):
                    try:
                        return names[group][match.group(0)]
                    except KeyError:
                        pass
                    return match.group(0)
                buf = regexps[group].sub(mapname, buf)
            with open(tmp_file, 'w') as fh:
                fh.write(buf)
            shutil.move(tmp_file, src_file)

        # HINT: update reobf srg
        updatefile(reoblk[side])

        # HINT: We pathwalk the sources
        for path, _, filelist in os.walk(pathsrclk[side], followlinks=True):
            for cur_file in fnmatch.filter(filelist, '*.java'):
                updatefile(os.path.normpath(os.path.join(path, cur_file)))
        return True

    def process_renumber(self, side):
        """Renumber the sources using the CSV data"""
        pathsrclk = {CLIENT: self.srcclient, SERVER: self.srcserver}

        if not self.has_renumber_csv:
            self.logger.warning('!! renumbering disabled due to no csv !!')
            return False

        regexps = {
            'methods': re.compile(r'func_([0-9]+)_[a-zA-Z_]+'),
            'fields': re.compile(r'field_([0-9]+)_[a-zA-Z_]+'),
            'params': re.compile(r'p_[\d]+_'),
        }

        # HINT: We read the relevant CSVs
        mapping = {'methods': {}, 'fields': {}, 'params': {}}
        with open(self.csvnewids, 'rb') as fh:
            in_csv = csv.DictReader(fh)
            for line in in_csv:
                in_id = None
                if side == CLIENT:
                    if line['client'] != '*':
                        in_id = line['client']
                else:
                    if line['server'] != '*':
                        in_id = line['server']
                if in_id:
                    method_match = regexps['methods'].match(in_id)
                    if method_match is not None:
                        if in_id not in mapping['methods']:
                            mapping['methods'][in_id] = line['newid']
                            in_param = 'p_' + method_match.group(1) + '_'
                            method_match = regexps['methods'].match(line['newid'])
                            if method_match is not None:
                                out_param = 'p_' + method_match.group(1) + '_'
                                mapping['params'][in_param] = out_param
                    field_match = regexps['fields'].match(in_id)
                    if field_match is not None:
                        if in_id not in mapping['fields']:
                            mapping['fields'][in_id] = line['newid']

        def updatefile(src_file):
            tmp_file = src_file + '.tmp'
            with open(src_file, 'r') as fh:
                buf = fh.read()
            for group in mapping.keys():
                def mapname(match):
                    try:
                        return mapping[group][match.group(0)]
                    except KeyError:
                        pass
                    return match.group(0)
                buf = regexps[group].sub(mapname, buf)
            with open(tmp_file, 'w') as fh:
                fh.write(buf)
            shutil.move(tmp_file, src_file)

        # HINT: We pathwalk the sources
        for path, _, filelist in os.walk(pathsrclk[side], followlinks=True):
            for cur_file in fnmatch.filter(filelist, '*.java'):
                updatefile(os.path.normpath(os.path.join(path, cur_file)))
        return True

    def process_annotate(self, side):
        """Annotate OpenGL constants"""
        pathsrclk = {CLIENT: self.srcclient, SERVER: self.srcserver}

        # HINT: We pathwalk the sources
        for path, _, filelist in os.walk(pathsrclk[side], followlinks=True):
            for cur_file in fnmatch.filter(filelist, '*.java'):
                src_file = os.path.normpath(os.path.join(path, cur_file))
                annotate_file(src_file)

    def process_comments(self, side):
        """Removes all C/C++/Java-style comments from files"""
        pathsrclk = {CLIENT: self.srcclient, SERVER: self.srcserver}

        strip_comments(pathsrclk[side])

    def process_cleanup(self, side):
        """Do lots of random cleanups including stripping comments, trailing whitespace and extra blank lines"""
        pathsrclk = {CLIENT: self.srcclient, SERVER: self.srcserver}

        src_cleanup(pathsrclk[side], fix_imports=True, fix_unicode=True, fix_charval=True, fix_pi=True, fix_round=False)

    def process_javadoc(self, side):
        """Add CSV descriptions to methods and fields as javadoc"""
        pathsrclk = {CLIENT: self.srcclient, SERVER: self.srcserver}

        if not self.has_doc_csv:
            self.logger.warning('!! javadoc disabled due to no csvs !!')
            return False

        #HINT: We read the relevant CSVs
        methodsreader = csv.DictReader(open(self.csvmethods, 'r'))
        fieldsreader = csv.DictReader(open(self.csvfields, 'r'))

        methods = {}
        for row in methodsreader:
            #HINT: Only include methods that have a non-empty description
            if int(row['side']) == side and row['desc']:
                methods[row['searge']] = row['desc'].replace('*/', '* /')

        fields = {}
        for row in fieldsreader:
            #HINT: Only include fields that have a non-empty description
            if int(row['side']) == side and row['desc']:
                fields[row['searge']] = row['desc'].replace('*/', '* /')

        regexps = {
            'field': re.compile(r'^(?P<indent> {4}|\t)(?:[\w$.[\]]+ )*(?P<name>field_[0-9]+_[a-zA-Z_]+) *(?:=|;)'),
            'method': re.compile(r'^(?P<indent> {4}|\t)(?:[\w$.[\]]+ )*(?P<name>func_[0-9]+_[a-zA-Z_]+)\('),
        }
        wrapper = TextWrapper(width=120)

        # HINT: We pathwalk the sources
        for path, _, filelist in os.walk(pathsrclk[side], followlinks=True):
            for cur_file in fnmatch.filter(filelist, '*.java'):
                src_file = os.path.normpath(os.path.join(path, cur_file))
                tmp_file = src_file + '.tmp'
                with open(src_file, 'r') as fh:
                    buf_in = fh.readlines()

                buf_out = []
                #HINT: Look for method/field declarations in this file
                for line in buf_in:
                    fielddecl = regexps['field'].match(line)
                    methoddecl = regexps['method'].match(line)
                    if fielddecl:
                        prev_line = buf_out[-1].strip()
                        indent = fielddecl.group('indent')
                        name = fielddecl.group('name')
                        if name in fields:
                            desc = fields[name]
                            if len(desc) < 70:
                                if prev_line != '' and prev_line != '{':
                                    buf_out.append('\n')
                                buf_out.append(indent + '/** ')
                                buf_out.append(desc)
                                buf_out.append(' */\n')
                            else:
                                wrapper.initial_indent = indent + ' * '
                                wrapper.subsequent_indent = indent + ' * '
                                if prev_line != '' and prev_line != '{':
                                    buf_out.append('\n')
                                buf_out.append(indent + '/**\n')
                                buf_out.append(wrapper.fill(desc) + '\n')
                                buf_out.append(indent + ' */\n')
                    elif methoddecl:
                        prev_line = buf_out[-1].strip()
                        indent = methoddecl.group('indent')
                        name = methoddecl.group('name')
                        if name in methods:
                            desc = methods[name]
                            wrapper.initial_indent = indent + ' * '
                            wrapper.subsequent_indent = indent + ' * '
                            if prev_line != '' and prev_line != '{':
                                buf_out.append('\n')
                            buf_out.append(indent + '/**\n')
                            buf_out.append(wrapper.fill(desc) + '\n')
                            buf_out.append(indent + ' */\n')
                    buf_out.append(line)

                with open(tmp_file, 'w') as fh:
                    fh.writelines(buf_out)
                shutil.move(tmp_file, src_file)
        return True

    def applyastyle(self, side):
        """Recompile the sources and produce the final bins"""
        pathsrclk = {CLIENT: self.srcclient, SERVER: self.srcserver}

        if not self.has_astyle_cfg:
            self.logger.warning('!! reformatting disabled due to no config !!')
            return False

        # HINT: We create the list of source directories based on the list of packages
        pkglist = filterdirs(pathsrclk[side], '*.java', append_pattern=True)
        dirs = ' '.join(pkglist)
        forkcmd = self.cmdastyle.format(classes=dirs, conffile=self.astyleconf)
        self.runcmd(forkcmd)
        return True

    def gathermd5s(self, side, reobf=False):
        if not reobf:
            md5lk = {CLIENT: self.md5client, SERVER: self.md5server}
        else:
            md5lk = {CLIENT: self.md5reobfclient, SERVER: self.md5reobfserver}
        pathbinlk = {CLIENT: self.binclient, SERVER: self.binserver}

        with open(md5lk[side], 'w') as md5file:
            # HINT: We pathwalk the recompiled classes
            for path, _, filelist in os.walk(pathbinlk[side]):
                class_path = os.path.relpath(path, pathbinlk[side]).replace(os.sep, '/')
                if class_path == '.':
                    class_path = ''
                else:
                    class_path += '/'
                for class_file in fnmatch.filter(filelist, '*.class'):
                    class_name = class_path + os.path.splitext(class_file)[0]
                    bin_file = os.path.normpath(os.path.join(path, class_file))
                    with open(bin_file, 'rb') as fh:
                        class_md5 = md5(fh.read()).hexdigest()
                    md5file.write('%s %s\n' % (class_name, class_md5))

    def packbin(self, side):
        jarlk = {CLIENT: self.cmpjarclient, SERVER: self.cmpjarserver}
        pathbinlk = {CLIENT: self.binclient, SERVER: self.binserver}
        pathtmpbinlk = {CLIENT: self.binclienttmp, SERVER: self.binservertmp}

        ignore_files = []
        if side == CLIENT:
            ignore_files.append(self.fixstart + '.class')
            if self.fixsound:
                ignore_files.append(self.fixsound + '.class')

        # HINT: We create the zipfile and add all the files from the bin directory
        with closing(zipfile.ZipFile(jarlk[side], 'w')) as zipjar:
            for path, _, filelist in os.walk(pathbinlk[side]):
                class_path = os.path.relpath(path, pathbinlk[side]).replace(os.sep, '/')
                if class_path == '.':
                    class_path = ''
                else:
                    class_path += '/'
                for class_file in fnmatch.filter(filelist, '*.class'):
                    class_name = class_path + class_file
                    bin_file = os.path.normpath(os.path.join(path, class_file))
                    if class_name not in ignore_files:
                        zipjar.write(bin_file, class_name)
            for pkg in self.ignorepkg:
                curpath = os.path.join(pathtmpbinlk[side], os.path.normpath(pkg))
                for path, _, filelist in os.walk(curpath):
                    class_path = os.path.relpath(path, pathtmpbinlk[side]).replace(os.sep, '/')
                    if class_path == '.':
                        class_path = ''
                    else:
                        class_path += '/'
                    for class_file in fnmatch.filter(filelist, '*.class'):
                        class_name = class_path + class_file
                        bin_file = os.path.join(path, class_file)
                        zipjar.write(bin_file, class_name)

    def unpackreobfclasses(self, side, reobf_all=False):
        jarlk = {CLIENT: self.reobfjarclient, SERVER: self.reobfjarserver}
        md5lk = {CLIENT: self.md5client, SERVER: self.md5server}
        md5reoblk = {CLIENT: self.md5reobfclient, SERVER: self.md5reobfserver}
        outpathlk = {CLIENT: self.dirreobfclt, SERVER: self.dirreobfsrv}
        srglk = {CLIENT: self.srgsclient, SERVER: self.srgsserver}

        # HINT: We need a table for the old md5 and the new ones
        md5table = {}
        with open(md5lk[side], 'r') as fh:
            for row in fh:
                row = row.strip().split()
                if len(row) == 2:
                    md5table[row[0]] = row[1]
        md5reobtable = {}
        with open(md5reoblk[side], 'r') as fh:
            for row in fh:
                row = row.strip().split()
                if len(row) == 2:
                    md5reobtable[row[0]] = row[1]
        ignore_classes = []
        if side == CLIENT:
            ignore_classes.append(self.fixstart)
            if self.fixsound:
                ignore_classes.append(self.fixsound)
        trgclasses = []
        for key in md5reobtable.keys():
            if key in ignore_classes:
                continue
            if key not in md5table:
                trgclasses.append(key)
                if '$' in key:
                    self.logger.info('> New inner class found: %s', key)
                else:
                    self.logger.info('> New class found      : %s', key)
            elif md5table[key] != md5reobtable[key]:
                trgclasses.append(key)
                self.logger.info('> Modified class found : %s', key)
            elif reobf_all:
                trgclasses.append(key)
                self.logger.info('> Unchanged class found: %s', key)
        classes = {}
        srg_data = parse_srg(srglk[side])
        for row in srg_data['CL']:
            classes[row['deobf_name']] = row['obf_name']

        if not os.path.exists(outpathlk[side]):
            os.makedirs(outpathlk[side])

        # HINT: We extract the modified class files
        with closing(zipfile.ZipFile(jarlk[side])) as zipjar:
            for in_class in trgclasses:
                parent_class, sep, inner_class = in_class.partition('$')
                if in_class in classes:
                    out_class = classes[in_class] + '.class'
                elif parent_class in classes:
                    out_class = classes[parent_class] + sep + inner_class + '.class'
                else:
                    out_class = in_class + '.class'
                    out_class = out_class.replace(self.nullpkg, '')
                    if out_class[0] == '/':
                        out_class = out_class[1:]
                try:
                    zipjar.extract(out_class, outpathlk[side])
                    self.logger.info('> Outputted %s to %s as %s', in_class.ljust(35), outpathlk[side], out_class)
                except KeyError:
                    self.logger.error('* File %s not found for %s', out_class, in_class)
                except IOError:
                    self.logger.error('* File %s failed extracting for %s', out_class, in_class)

    def downloadupdates(self, force=False):
        if not self.updateurl:
            self.logger.error('Updates disabled.')
            return

        newfiles = self.checkupdates(silent=True)
        if not newfiles:
            self.logger.info('No new updates found.')
            return

        for entry in newfiles:
            if entry[3] == 'U':
                self.logger.info('New version found for : %s', entry[0])
            elif entry[3] == 'D':
                self.logger.info('File tagged for deletion : %s', entry[0])

        if 'CHANGELOG' in [i[0] for i in newfiles]:
            print ''
            self.logger.info('== CHANGELOG ==')
            changelog_url = self.updateurl + 'mcp/CHANGELOG'
            changelog = urllib.urlopen(changelog_url).readlines()
            for line in changelog:
                self.logger.info(line.strip())
                if not line.strip():
                    break
            print ''
            print ''

        if not force:
            print 'WARNING:'
            print 'You are going to update MCP'
            print 'Are you sure you want to continue ?'
            answer = raw_input('If you really want to update, enter "Yes" ')
            if answer.lower() not in ['yes', 'y']:
                print 'You have not entered "Yes", aborting the update process'
                sys.exit(1)

        for entry in newfiles:
            if entry[3] == 'U':
                self.logger.info('Retrieving file from server : %s', entry[0])
                cur_file = os.path.normpath(entry[0])
                path = os.path.dirname(cur_file)
                if not os.path.isdir(path):
                    try:
                        os.makedirs(path)
                    except OSError:
                        pass
                file_url = self.updateurl + 'mcp/' + entry[0]
                urllib.urlretrieve(file_url, cur_file)
            elif entry[3] == 'D':
                self.logger.info('Removing file from local install : %s', entry[0])
                # Remove file here

    def unpackmodifiedclasses(self, side):
        md5lk = {CLIENT: self.md5client, SERVER: self.md5server}
        md5reoblk = {CLIENT: self.md5reobfclient, SERVER: self.md5reobfserver}
        outpathlk = {CLIENT: self.srcmodclient, SERVER: self.srcmodserver}
        src = {CLIENT: self.srcclient, SERVER: self.srcserver}

        # HINT: We need a table for the old md5 and the new ones
        md5table = {}
        with open(md5lk[side], 'r') as fh:
            for row in fh:
                row = row.strip().split()
                if len(row) == 2:
                    md5table[row[0]] = row[1]
        md5reobtable = {}
        with open(md5reoblk[side], 'r') as fh:
            for row in fh:
                row = row.strip().split()
                if len(row) == 2:
                    md5reobtable[row[0]] = row[1]
        trgclasses = []
        for key in md5reobtable.keys():
            if key not in md5table:
                trgclasses.append(key)
                if '$' in key:
                    self.logger.info('> New inner class found: %s', key)
                else:
                    self.logger.info('> New class found      : %s', key)
            elif md5table[key] != md5reobtable[key]:
                trgclasses.append(key)
                self.logger.info('> Modified class found : %s', key)

        if not os.path.exists(outpathlk[side]):
            os.makedirs(outpathlk[side])

        # HINT: We extract the source files for the modified class files
        for in_class in trgclasses:
            src_file = os.path.normpath(os.path.join(src[side], in_class + '.java'))
            dest_file = os.path.normpath(os.path.join(outpathlk[side], in_class + '.java'))
            if os.path.isfile(src_file):
                if not os.path.exists(os.path.dirname(dest_file)):
                    os.makedirs(os.path.dirname(dest_file))
                try:
                    shutil.copyfile(src_file, dest_file)
                    self.logger.info('> Outputted %s to %s', in_class.ljust(35), outpathlk[side])
                except IOError:
                    self.logger.error('* File %s copy failed', in_class)

