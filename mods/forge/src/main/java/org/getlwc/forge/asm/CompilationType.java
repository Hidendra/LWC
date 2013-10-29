package org.getlwc.forge.asm;

public enum CompilationType {

    /**
     * Unobfuscated (MCP) name
     */
    UNOBFUSCATED,

    /**
     * Obfuscated name used in the Minecraft jar.
     */
    OBFUSCATED,

    /**
     * Forge encoded names. Likely to stay the same between minor updates.
     */
    SRG

}
