package org.getlwc.forge.asm.mappings;

public class MappedField {

    /**
     * Human name of the field
     */
    private String name;

    /**
     * Obfuscated name of the field
     */
    private String obfuscatedName;

    public MappedField(String name, String obfuscatedName) {
        this.name = name;
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

        MappedField field = (MappedField) o;

        if (name != null ? !name.equals(field.name) : field.name != null) return false;
        if (obfuscatedName != null ? !obfuscatedName.equals(field.obfuscatedName) : field.obfuscatedName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (obfuscatedName != null ? obfuscatedName.hashCode() : 0);
        return result;
    }

    public String getName() {
        return name;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

}
