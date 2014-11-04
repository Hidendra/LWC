#!/bin/sh

echo " >>> Cleaning gradle build classes"
rm -rf build/classes/*
rm -f build/libs/*
rm -rf tmp
mkdir -p src tmp
rsync -av --progress --delete ../src/. src/

echo " >>> Building with ForgeGradle"
bash gradlew -b build.gradle build

echo " >>> Proceeding to final phase"
unzip build/libs/lwc.jar -d tmp/
cd tmp
rm -rf META-INF

LWC_RELATIVE_JAR=`find ../../target/LWC-*.jar | head -n1`
LWC_JAR=`basename "$LWC_RELATIVE_JAR"`

echo "Matched distribution jar file: \"$LWC_JAR\""

echo " >>> Updating jar file"

jar uf "$LWC_RELATIVE_JAR" ./
cd ../../

# Final cleanup
rm -rf mcp/tmp mcp/src/* mcp/build/sources/* mcp/classes/* build/libs/*