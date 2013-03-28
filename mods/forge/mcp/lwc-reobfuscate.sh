#!/bin/sh

echo " >>> Extracting Forge over bin"
unzip -qo lib/forge-full.jar -d bin/minecraft/

echo " >>> Reobfuscating"
# sh reobfuscate.sh --srgnames
python runtime/reobfuscate.py --srgnames

echo " >>> Proceeding to final phase"

cd reobf/minecraft/
LWC_RELATIVE_JAR=`find ../../../target/LWC-*.jar | head -n1`
LWC_JAR=`basename "$LWC_RELATIVE_JAR"`

echo "Matched distribution jar file: \"$LWC_JAR\""

echo " >>> Updating jar file"

jar uf "$LWC_RELATIVE_JAR" ./
cd ../../
