class Field:

    def __init__(self, name, obfuscated_name):
        self.name = name
        self.obfuscated_name = obfuscated_name

    def __str__(self):
        return 'Field(name = %s, obf = %s)' % (self.name, self.obfuscated_name)

class Method:

    def __init__(self, name, obfuscated_name, signature):
        self.name = name
        self.obfuscated_name = obfuscated_name
        self.signature = signature

    def __str__(self):
        return 'Method(name = %s, obf = %s, signature = %s)' % (self.name, self.obfuscated_name, self.signature)

class Class:

    def __init__(self, canonical_name):
        self.canonical_name = canonical_name
        self.obfuscated_name = None
        self.methods = []
        self.fields = []

    def __str__(self):
        return 'Class(name = %s, obf = %s)' % (self.canonical_name, self.obfuscated_name)

    def get_method(self, name):
        """
        Gets a method inside this class
        :param name:
        :return:
        """

        for method in self.methods:
            if method.name == name or method.obfuscated_name == name:
                return method

        return None

    def get_field(self, name):
        """
        Gets a field inside this class

        :param name:
        :return:
        """

        for field in self.fields:
            if field.name == name or field.obfuscated_name == name:
                return field;

        return None

    @property
    def simple_name(self):
        """
        Returns the simple name for the class
        :return:
        """

        boundary = self.canonical_name.rindex('.')
        return self.canonical_name[boundary + 1:]

    @property
    def package(self):
        """
        Returns the package the class is in
        :return:
        """

        boundary = self.canonical_name.rindex('.')
        return self.canonical_name[:boundary];