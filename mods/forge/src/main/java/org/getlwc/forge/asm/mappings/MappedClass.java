/**
 * Copyright (c) 2011-2014 Tyler Blair
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */
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
            if (method.getName().equals(name) || method.getSrgName().equals(name) || method.getObfuscatedName().equals(name)) {
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
            if (field.getName().equals(name) || field.getSrgName().equals(name) || field.getObfuscatedName().equals(name)) {
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
