# -*- coding: utf-8 -*-
"""
Created on Fri Apr  8 16:54:36 2011

@author: ProfMobius
@version: v1.1
"""

import sys
import logging
from optparse import OptionParser

from commands import Commands, CLIENT, SERVER
from mcp import reobfuscate_side


def main():
    parser = OptionParser(version='MCP %s' % Commands.fullversion())
    parser.add_option('--client', dest='only_client', action='store_true', help='only process client', default=False)
    parser.add_option('--server', dest='only_server', action='store_true', help='only process server', default=False)
    parser.add_option('-a', '--all', action='store_true', dest='reobf_all', help='output all classes', default=False)
    parser.add_option('-n', '--nolvt', dest='keep_lvt', action='store_false', help='strip local variable table',
                      default=True)
    parser.add_option('-g', '--generics', dest='keep_generics', action='store_true',
                      help='preserve generics as well as local variables', default=False)
    parser.add_option('-c', '--config', dest='config', help='additional configuration file')
    options, _ = parser.parse_args()
    reobfuscate(options.config, options.reobf_all, options.keep_lvt, options.keep_generics, options.only_client,
                options.only_server)


def reobfuscate(conffile, reobf_all, keep_lvt, keep_generics, only_client, only_server):
    try:
        commands = Commands(conffile, verify=True)

        if keep_generics:
            keep_lvt = True

        # client or server
        process_client = True
        process_server = True
        if only_client and not only_server:
            process_server = False
        if only_server and not only_client:
            process_client = False

        commands.logger.info('> Creating Retroguard config files')
        commands.creatergcfg(reobf=True, keep_lvt=keep_lvt, keep_generics=keep_generics)

        if process_client:
            reobfuscate_side(commands, CLIENT, reobf_all=reobf_all)
        if process_server:
            reobfuscate_side(commands, SERVER, reobf_all=reobf_all)
    except Exception:  # pylint: disable-msg=W0703
        logging.exception('FATAL ERROR')
        sys.exit(1)


if __name__ == '__main__':
    main()
