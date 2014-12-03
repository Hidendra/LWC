package org.getlwc.forge.asm.mappings;

import java.util.HashSet;
import java.util.Set;

public class MappedClass {

    /**
     * Name of the class
     */
    private String canonicalName;

    /**
     * Obfuscated name of the class
     */
    private String obfuscatedName;

    /**
     * Methods in the class
     */
    private final Set<MappedMethod> methods = new HashSet<>();

    /**
     * Fields in the class
     */
    private final Set<MappedField> fields = new HashSet<>();

    public MappedClass(String canonicalName, String obfuscatedName) {
        this.canonicalName = canonicalName;
        this.obfuscatedName = obfuscatedName;
    }

    @Override
    public String toString() {
        return String.format("MappedClass('%s')", canonicalName);
    }

    /**
     * Add a method to the class
     *
     * @param method
     */
    public void addMethod(MappedMethod method) {
        methods.add(method);
    }

    /**
     * Adds a field to the class
     *
     * @param field
     */
    public void addField(MappedField field) {
        fields.add(field);
    }

    /**
     * Gets a method in the class
     *
     * @param name
     * @return
     */
    public MappedMethod getMethod(String name) {
        for (MappedMethod method : methods) {
            if (method.getName().equals(name) || method.getObfuscatedName().equals(name)) {
                return method;
            }
        }

        return null;
    }

    /**
     * Gets a field in the class
     *
     * @param name
     * @return
     */
    public MappedField getField(String name) {
        for (MappedField field : fields) {
            if (field.getName().equals(name) || field.getObfuscatedName().equals(name)) {
                return field;
            }
        }

        return null;
    }

    /**
     * Returns the simple name for the class (no package)
     *
     * @return
     */
    public String getSimpleName() {
        int lastIndex = canonicalName.lastIndexOf('.');

        if (lastIndex == -1) {
            return canonicalName;
        } else {
            return canonicalName.substring(lastIndex + 1);
        }
    }

    /**
     * Returns the canonical name for the class (includes package)
     *
     * @return
     */
    public String getCanonicalName() {
        return canonicalName;
    }

    /**
     * Returns the MC obfuscated name for the class
     *
     * @return
     */
    public String getObfuscatedName() {
        return obfuscatedName;
    }

}
