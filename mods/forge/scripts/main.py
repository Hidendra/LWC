from mapping import *
import mc1_7_10
import json

MINECRAFT_VERSION = '1.7.10'
base_path = '/Users/hidendra/.gradle//caches/minecraft/net/minecraftforge/forge/1.7.10-10.13.2.1230/srgs/'
OUTPUT_DIR = '../src/main/resources/'

loader = mc1_7_10.SimpleMappingLoader()
classes = loader.load_classes(base_path)

class MyEncoder(json.JSONEncoder):

    def default(self, o):
        if isinstance(o, Class):
            return o.__dict__
        elif isinstance(o, Method):
            return o.__dict__
        elif isinstance(o, Field):
            return o.__dict__
        else:
            return super(MyEncoder, self).default(o)

outputmap = {
    'version': MINECRAFT_VERSION,
    'classes': {}
}

for clazz in classes:
    print 'Loaded class: %s with %d methods, %d fields' % (clazz, len(clazz.methods), len(clazz.fields))
    outputmap['classes'][clazz.simple_name] = clazz


with open('%s/mappings/%s.json' % (OUTPUT_DIR, MINECRAFT_VERSION), 'w') as f:
    json.dump(outputmap, f, cls=MyEncoder, indent=4, separators=(',', ': '))

with open('%s/mappings/latest.json' % OUTPUT_DIR, 'w') as f:
    json.dump(outputmap, f, cls=MyEncoder, indent=4, separators=(',', ': '))
