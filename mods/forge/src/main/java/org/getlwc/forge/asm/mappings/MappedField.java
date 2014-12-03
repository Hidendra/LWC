package org.getlwc.forge.asm.mappings;

public class MappedField {

    /**
     * Human name of the field
     */
    private String name;

    /**
     * MCP SRG name
     */
    private String srgName;

    /**
     * Obfuscated name of the field
     */
    private String obfuscatedName;

    public MappedField(String name, String srgName, String obfuscatedName) {
        this.name = name;
        this.srgName = srgName;
        this.obfuscatedName = obfuscatedName;
    }

    @Override
    public String toString() {
        return String.format("Field('%s')", name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MappedField that = (MappedField) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (obfuscatedName != null ? !obfuscatedName.equals(that.obfuscatedName) : that.obfuscatedName != null)
            return false;
        if (srgName != null ? !srgName.equals(that.srgName) : that.srgName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (srgName != null ? srgName.hashCode() : 0);
        result = 31 * result + (obfuscatedName != null ? obfuscatedName.hashCode() : 0);
        return result;
    }

    public String getSrgName() {
        return srgName;
    }

    public String getName() {
        return name;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

}
