from mapping import *
import mc1_7_10
import json
import os

MINECRAFT_VERSION = '1.7.10'
FORGE_VERSION = '1.7.10-10.13.2.1230'

BASE_PATH = os.path.expanduser('~/.gradle/caches/minecraft/net/minecraftforge/forge/%s/srgs/' % FORGE_VERSION)
OUTPUT_DIR = '../src/main/resources/'

if not os.path.exists(BASE_PATH):
    print 'Unable to found Forge directory at %s' % BASE_PATH
    print 'Please make sure Forge %s is installed and built using Gradle' % FORGE_VERSION
    exit()

loader = mc1_7_10.SimpleMappingLoader()
classes = loader.load_classes(BASE_PATH)

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
