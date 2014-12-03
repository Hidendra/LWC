package org.getlwc.forge.asm.mappings;

public class MappedMethod {

    /**
     * Human name of the method
     */
    private String name;

    /**
     * Obfuscated name of the method
     */
    private String obfuscatedName;

    /**
     * Signature of the method (using obf. class names)
     */
    private String obfuscatedSignature;

    public MappedMethod(String name, String obfuscatedName, String obfuscatedSignature) {
        this.name = name;
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

        MappedMethod method = (MappedMethod) o;

        if (name != null ? !name.equals(method.name) : method.name != null) return false;
        if (obfuscatedName != null ? !obfuscatedName.equals(method.obfuscatedName) : method.obfuscatedName != null)
            return false;
        if (obfuscatedSignature != null ? !obfuscatedSignature.equals(method.obfuscatedSignature) : method.obfuscatedSignature != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (obfuscatedName != null ? obfuscatedName.hashCode() : 0);
        result = 31 * result + (obfuscatedSignature != null ? obfuscatedSignature.hashCode() : 0);
        return result;
    }

    public String getName() {
        return name;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    public String getObfuscatedSignature() {
        return obfuscatedSignature;
    }

}
