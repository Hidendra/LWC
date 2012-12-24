# -*- coding: utf-8 -*-
"""
Created on Thu Jan  19 16:29:03 2012

@author: Fesh0r
@version: v0.1
"""

import sys
import os
import fnmatch
import shutil
import re
from optparse import OptionParser


_MODIFIERS = r'public|protected|private|static|abstract|final|native|synchronized|transient|volatile|strictfp'

_REGEXP = {
    # Remove trailing whitespace
    'trailing': re.compile(r'[ \t]+$', re.MULTILINE),

    # Remove repeated blank lines
    'newlines': re.compile(r'^\n{2,}', re.MULTILINE),

    'modifiers': re.compile(r'(' + _MODIFIERS + ') '),
    'list': re.compile(r', '),

    'enum_class': re.compile(r'^(?P<modifiers>(?:(?:' + _MODIFIERS + r') )*)(?P<type>enum) (?P<name>[\w$]+)(?: implements (?P<implements>[\w$.]+(?:, [\w$.]+)*))? \{\n(?P<body>(?:.*?\n)*?)(?P<end>\}\n+)', re.MULTILINE),

    'enum_entries': re.compile(r'^ {3}(?P<name>[\w$]+)\("(?P=name)", [0-9]+(?:, (?P<body>.*?))?\)(?P<end>(?:;|,)\n+)', re.MULTILINE),

    'empty_super': re.compile(r'^ +super\(\);\n', re.MULTILINE),

    # strip trailing 0 from doubles and floats to fix decompile differences on OSX
    # 0.0010D => 0.001D
    'trailingzero': re.compile(r'(?P<value>[0-9]+\.[0-9]*[1-9])0+(?P<type>[DdFfEe])'),
}

_REGEXP_STR = {
    'constructor': r'^ {3}(?P<modifiers>(?:(?:' + _MODIFIERS + r') )*)%s\((?P<parameters>.*?)\)(?: throws (?P<throws>[\w$.]+(?:, [\w$.]+)*))? \{(?:(?P<empty>\}\n+)|(?:(?P<body>\n(?:.*?\n)*?)(?P<end> {3}\}\n+)))',

    'enum_values': r'^ {3}// \$FF: synthetic field\n {3}private static final %s\[\] [\w$]+ = new %s\[\]\{.*?\};\n',
}


class Error(Exception):
    pass


class ParseError(Error):
    pass


def fffix(srcdir):
    for path, _, filelist in os.walk(srcdir, followlinks=True):
        for cur_file in fnmatch.filter(filelist, '*.java'):
            src_file = os.path.normpath(os.path.join(path, cur_file))
            _process_file(src_file)


def _process_enum(class_name, class_type, modifiers, implements, body, end):
    def _enum_entries_match(match):
        entry_body = ''
        if match.group('body'):
            entry_body = '(' + match.group('body') + ')'
        new_entry = '   ' + match.group('name') + entry_body + match.group('end')
        return new_entry
    body = _REGEXP['enum_entries'].sub(_enum_entries_match, body)

    values_regex = re.compile(_REGEXP_STR['enum_values'] % (re.escape(class_name), re.escape(class_name)), re.MULTILINE)
    body = values_regex.sub(r'', body)

    # process constructors
    def constructor_match(match):
        modifiers = _REGEXP['modifiers'].findall(match.group('modifiers'))
        if match.group('modifiers') and not modifiers:
            raise ParseError('no modifiers match in %s \'%s\'' % (match.group('name'), match.group('modifiers')))
        parameters = []
        if match.group('parameters'):
            parameters = _REGEXP['list'].split(match.group('parameters'))
        throws = []
        if match.group('throws'):
            throws = _REGEXP['list'].split(match.group('throws'))
        if match.group('empty') is not None:
            method_body = ''
            method_end = match.group('empty')
        else:
            method_body = match.group('body')
            method_end = match.group('end')
        return _process_constructor(class_name, modifiers, parameters, throws, method_body, method_end)
    constructor_regex = re.compile(_REGEXP_STR['constructor'] % re.escape(class_name), re.MULTILINE)
    body = constructor_regex.sub(constructor_match, body)

    # rebuild enum
    out = ''
    if modifiers:
        out += ' '.join(modifiers) + ' '
    out += class_type + ' ' + class_name
    if implements:
        out += ' implements ' + ', '.join(implements)
    out += ' {\n' + body + end
    return out


def _process_constructor(class_name, modifiers, parameters, throws, body, end):
    if len(parameters) >= 2:
        if parameters[0].startswith('String ') and parameters[1].startswith('int '):
            parameters = parameters[2:]
            # empty constructor
            if body == '' and len(parameters) == 0:
                return ''
        else:
            raise ParseError('invalid initial parameters in enum %s: %s' % (class_name, str(parameters)))
    else:
        raise ParseError('not enough parameters in enum %s: %s' % (class_name, str(parameters)))

    # rebuild constructor
    out = '   '
    if modifiers:
        out += ' '.join(modifiers) + ' '
    out += class_name + '(' + ', '.join(parameters) + ')'
    if throws:
        out += ' throws ' + ', '.join(throws)
    out += ' {' + body + end
    return out


def _process_file(src_file):
    class_name = os.path.splitext(os.path.basename(src_file))[0]
    tmp_file = src_file + '.tmp'
    with open(src_file, 'r') as fh:
        buf = fh.read()

    buf = _REGEXP['trailing'].sub(r'', buf)

    def enum_match(match):
        if class_name != match.group('name'):
            raise ParseError("file name and class name differ: '%s' '%s" % (class_name, match.group('name')))
        modifiers = _REGEXP['modifiers'].findall(match.group('modifiers'))
        if match.group('modifiers') and not modifiers:
            raise ParseError("no modifiers match in %s '%s'" % (match.group('name'), match.group('modifiers')))
        implements = []
        if match.group('implements'):
            implements = _REGEXP['list'].split(match.group('implements'))
        return _process_enum(match.group('name'), match.group('type'), modifiers, implements, match.group('body'),
                              match.group('end'))
    buf = _REGEXP['enum_class'].sub(enum_match, buf)

    buf = _REGEXP['empty_super'].sub(r'', buf)

    buf = _REGEXP['trailingzero'].sub(r'\g<value>\g<type>', buf)

    buf = _REGEXP['newlines'].sub(r'\n', buf)

    with open(tmp_file, 'w') as fh:
        fh.write(buf)
    shutil.move(tmp_file, src_file)


def main():
    usage = 'usage: %prog [options] src_dir'
    version = '%prog 6.0'
    parser = OptionParser(version=version, usage=usage)
    options, args = parser.parse_args()
    if len(args) != 1:
        print >> sys.stderr, 'src_dir required'
        sys.exit(1)
    fffix(args[0])


if __name__ == '__main__':
    main()
