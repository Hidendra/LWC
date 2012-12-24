This is a very minimal MCP + Forge install. This is the bare minimum required to build LWC.

When updating Forge, you should only need to copy these folders/files from a full MCP + Forge install to this folder:

conf
runtime
temp
lib/asm-all-4.0.jar
lib/guava-12.0.1.jar
lib/forge-full.jar

You can also clean out some files that are not needed for reduced disk space. From the root mcp folder:

rm -rf temp/src temp/cls temp/*.jar runtime/bin/python

NOTE: forge-full.jar

forge-full.jar is created using a full recompile with a full MCP + Forge install.

NOTE: FreeBSD support

Apply the freebsd patch: # patch -p0 < freebsd-support.patch

To create forge-full.jar cd into bin/minecraft/ and run jar cf forge-full.jar ./

After all of this LWC should build successfully on the new Minecraft version.