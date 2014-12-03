import abc

class MappingLoader:


    @abc.abstractmethod
    def load_classes(self, base_path):
        """
        Loads all mappings from the given base_path. The array
        of found classes is returned.
        :param base_path:
        :return: Class[]
        """
        return []