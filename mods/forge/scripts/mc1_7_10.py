import mappingloader
from mapping import *

def split_class_and_item(value):
    """
    Splits a class and value pair and returns a tuple.
    e.g. com/test/Value -> ('com/test', 'Value')
    :param value:
    :return:
    """

    try:
        boundary = value.rindex('/')
    except ValueError:
        return None

    return value[:boundary], value[boundary + 1:]

def class_to_java_notation(name):
    """
    Converts a class of the notation com/test/Class to com.test.Class
    :param name:
    :return:
    """

    return name.replace('/', '.')

def parse_class(value):
    """
    Parse a class from the given value and returns the result
    :param value:
    :return:
    """

    class_canonical_name = class_to_java_notation(value)
    return Class(class_canonical_name)

def get_class(classmap, value):
    """
    Attempts to retrieve a class from the given classmap using a value,
    which can be fairly lenient (com.package.Class, com/package/Class, or
    a Class object).
    If the class does not exist in the map it will be initialized.
    :param classmap:
    :param value:
    :return:
    """

    if isinstance(value, Class):
        clazz = value
    else:
        clazz = parse_class(value)

    if clazz.simple_name not in classmap:
        classmap[clazz.simple_name] = clazz

    return classmap[clazz.simple_name]

def load_mcp_srg(classmap, base_path):
    """
    Loads MCP-SRG names. Most importantly: method names which are of the form
    func_xxxxxx_y which are mapped to be independent of the Minecraft version.
    :param classmap:
    :param base_path:
    :return: class{}
    """

    with open('%s/%s' % (base_path, 'mcp-srg.srg')) as f:
        for line in f:
            split = line.strip().split(' ')
            value_type = split[0]

            if value_type == 'CL:':
                get_class(classmap, split[1])
            elif value_type == 'FD:':
                source = split_class_and_item(split[1])
                dest = split_class_and_item(split[2])

                clazz = get_class(classmap, source[0])

                field = clazz.get_field(source[1])

                if field is None:
                    field = Field(source[1])
                    clazz.fields.append(field)

                field.srg_name = dest[1]

            elif value_type == 'MD:':
                source = split_class_and_item(split[1])
                source_signature = split[2]
                dest = split_class_and_item(split[3])
                dest_signature = split[4]

                clazz = get_class(classmap, source[0])
                method = clazz.get_method(source[1])

                if method is None:
                    method = Method(source[1])
                    clazz.methods.append(method)

                method.srg_name = dest[1]
                method.srg_signature = dest_signature

def load_mcp_notch(classmap, base_path):
    """
    Loads MCP-Notch names. These are typically just the obfuscated name.
    The only interested obfuscated names are the class ones, as method
    and field names have more appropriate SRG names.

    :param classmap:
    :param base_path:
    :return:
    """

    with open('%s/%s' % (base_path, 'mcp-notch.srg')) as f:
        for line in f:
            split = line.strip().split(' ')
            value_type = split[0]

            if value_type == 'CL:':
                clazz = get_class(classmap, split[1])

                clazz.obfuscated_name = split[2]
            elif value_type == 'MD:':
                source = split_class_and_item(split[1])
                dest = split_class_and_item(split[3])
                dest_signature = split[4]

                clazz = get_class(classmap, source[0])
                method = clazz.get_method(source[1])

                if method is not None:
                    method.obfuscated_name = dest[1]
                    method.obfuscated_signature = dest_signature
            elif value_type == 'FD:':
                source = split_class_and_item(split[1])
                dest = split_class_and_item(split[2])

                clazz = get_class(classmap, source[0])
                field = clazz.get_field(source[1])

                if field is not None:
                    field.obfuscated_name = dest[1]

class SimpleMappingLoader(mappingloader.MappingLoader):

    def load_classes(self, base_path):
        classmap = {}
        load_mcp_srg(classmap, base_path)
        load_mcp_notch(classmap, base_path)

        return classmap.values()