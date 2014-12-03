package org.getlwc.forge.asm.mappings;

public class MappedMethod {

    /**
     * Human name of the method
     */
    private String name;

    /**
     * MCP SRG name of the method
     */
    private String srgName;

    /**
     * MCP SRG signature of the method
     */
    private String srgSignature;

    /**
     * Obfuscated name of the method
     */
    private String obfuscatedName;

    /**
     * Signature of the method (using obf. class names)
     */
    private String obfuscatedSignature;

    public MappedMethod(String name, String srgName, String srgSignature, String obfuscatedName, String obfuscatedSignature) {
        this.name = name;
        this.srgName = srgName;
        this.srgSignature = srgSignature;
        this.obfuscatedName = obfuscatedName;
        this.obfuscatedSignature = obfuscatedSignature;
    }

    @Override
    public String toString() {
        return String.format("Method('%s')", name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MappedMethod that = (MappedMethod) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (obfuscatedName != null ? !obfuscatedName.equals(that.obfuscatedName) : that.obfuscatedName != null)
            return false;
        if (obfuscatedSignature != null ? !obfuscatedSignature.equals(that.obfuscatedSignature) : that.obfuscatedSignature != null)
            return false;
        if (srgName != null ? !srgName.equals(that.srgName) : that.srgName != null) return false;
        if (srgSignature != null ? !srgSignature.equals(that.srgSignature) : that.srgSignature != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (srgName != null ? srgName.hashCode() : 0);
        result = 31 * result + (srgSignature != null ? srgSignature.hashCode() : 0);
        result = 31 * result + (obfuscatedName != null ? obfuscatedName.hashCode() : 0);
        result = 31 * result + (obfuscatedSignature != null ? obfuscatedSignature.hashCode() : 0);
        return result;
    }

    public String getName() {
        return name;
    }

    public String getSrgName() {
        return srgName;
    }

    public String getSrgSignature() {
        return srgSignature;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    public String getObfuscatedSignature() {
        return obfuscatedSignature;
    }

}
