/*
 * Copyright (c) 2011-2013 Tyler Blair
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
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR,
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

package org.getlwc.forge.asm;

public abstract class AbstractSingleClassTransformer extends AbstractTransformer {

    /**
     * The class name (simple, not canonical / full) of the desired class we want to transform
     */
    private final String className;

    /**
     * If the classes are detected to be obfuscated or not
     */
    private boolean obfuscated = false;

    public AbstractSingleClassTransformer(String className) {
        this.className = className;
    }

    /**
     * @inheritDoc
     */
    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {

        if (mappings == null) {
            // System.out.println("mappings null, name = " + name);
            return bytes;
        }

        if (name.equals(getClassName(className, false))) {
            obfuscated = false;
            return transform(bytes);
        } else if (name.equals(getClassName(className, true))) {
            obfuscated = true;
            return transform(bytes);
        }

        return bytes;
    }

    /**
     * Transform the given matched class
     *
     * @param bytes
     * @return
     */
    public abstract byte[] transform(byte[] bytes);

    /**
     * Get a class name
     *
     * @param className
     */
    public String getClassName(String className) {
        return getClassName(className, obfuscated);
    }

    /**
     * Get a class name
     *
     * @param className
     * @param obfuscated
     * @return
     */
    public String getClassName(String className, boolean obfuscated) {
        return mappings.getString("classes." + className + "." + getObfuscatedModifier(obfuscated));
    }

    /**
     * Get the Java bytecode class name for the given class (replaces . with /)
     *
     * @param className
     * @return
     */
    public String getJavaClassName(String className) {
        return getJavaClassName(className, obfuscated);
    }

    /**
     * Get the Java bytecode class name for the given class (replaces . with /)
     *
     * @param className
     * @param obfuscated
     * @return
     */
    public String getJavaClassName(String className, boolean obfuscated) {
        return getClassName(className, obfuscated).replaceAll("\\.", "/");
    }

    /**
     * Get the method name to use in the given class
     *
     * @param className
     * @param methodName
     * @return
     */
    public String getMethodName(String className, String methodName) {
        return getMethodName(className, methodName, obfuscated);
    }

    /**
     * Get the method name to use in the given class
     *
     * @param className
     * @param methodName
     * @param obfuscated
     * @return
     */
    public String getMethodName(String className, String methodName, boolean obfuscated) {
        if (obfuscated) {
            return mappings.getString("methods." + className + "." + methodName + ".obf");
        } else {
            return methodName;
        }
    }

    /**
     * Get the field name to use in the given class
     *
     * @param className
     * @param fieldName
     * @return
     */
    public String getFieldName(String className, String fieldName) {
        return getFieldName(className, fieldName, obfuscated);
    }

    /**
     * Get the field name to use in the given class
     *
     * @param className
     * @param fieldName
     * @param obfuscated
     * @return
     */
    public String getFieldName(String className, String fieldName, boolean obfuscated) {
        if (obfuscated) {
            return mappings.getString("fields." + className + "." + fieldName);
        } else {
            return fieldName;
        }
    }

    /**
     * Note that it is only known if the class is obfuscated or not only AFTER transform() has been successfully called
     * @return true if the class is known to be obfuscated
     */
    public boolean isObfuscated() {
        return obfuscated;
    }

    /**
     * Get the modifier used in the config file to denote obfuscated or unobfuscated
     *
     * @param obfuscated
     * @return
     */
    private String getObfuscatedModifier(boolean obfuscated) {
        return obfuscated ? "obf" : "mcp";
    }

}
