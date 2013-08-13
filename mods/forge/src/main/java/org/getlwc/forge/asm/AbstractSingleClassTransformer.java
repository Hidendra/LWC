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

import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class AbstractSingleClassTransformer extends AbstractTransformer {

    /**
     * The class name (simple, not canonical / full) of the desired class we want to transform
     */
    private final String className;

    /**
     * If the classes are detected to be obfuscated or not
     */
    private boolean obfuscated = false;

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
     * Check if a given {@link MethodNode} equals the method in the given method
     *
     * @param method
     * @param className
     * @param methodName
     * @return
     */
    public boolean methodEquals(MethodNode method, String className, String methodName) {
        return method.desc.equals(getMethodSignature(className, methodName)) && method.name.equals(getMethodName(className, methodName));
    }

    /**
     * Check if the variable matches the given class
     *
     * @param variable
     * @param className
     * @return
     */
    public boolean variableMatchesClass(LocalVariableNode variable, String className) {
        String signature = "L" + getJavaClassName(className) + ";";

        return variable.desc.equals(signature);
    }

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
    public static String getClassName(String className, boolean obfuscated) {
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
    public static String getJavaClassName(String className, boolean obfuscated) {
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
    public static String getMethodName(String className, String methodName, boolean obfuscated) {
        if (obfuscated) {
            return mappings.getString("methods." + className + "." + methodName + ".obf");
        } else {
            return methodName;
        }
    }

    /**
     * Get the signature of a method
     *
     * @param className
     * @param methodName
     * @return
     */
    public String getMethodSignature(String className, String methodName) {
        return getMethodSignature(className, methodName, obfuscated);
    }

    /**
     * Get the signature of a method
     *
     * @param className
     * @param methodName
     * @param obfuscated
     * @return
     */
    public static String getMethodSignature(String className, String methodName, boolean obfuscated) {
        String signature = mappings.getString("methods." + className + "." + methodName + ".signature");

        // replace all #ClassName resolvers
        while (signature.contains("#")) {
            String matchedClass = "";
            int index = signature.indexOf('#') + 1;
            char chr;

            while ((chr = signature.charAt(index)) != ';') {
                matchedClass += chr;
                index++;

                if (index >= signature.length()) {
                    break;
                }

                if (index > 1000) {
                    System.out.println("Invalid signature detected for class " + className + " method " + methodName + " signature " + signature);
                    return "";
                }
            }

            signature = signature.replaceAll("#" + matchedClass, getJavaClassName(matchedClass, obfuscated));
        }

        return signature;
    }

    /**
     * Get the field name to use in the given class
     *
     * @param className
     * @param fieldName
     * @return
     */
    public String getFieldName(String className, String fieldName) {
        return getFieldName(className, fieldName, CompilationType.OBFUSCATED);
    }

    /**
     * Get the field name to use in the given class
     *
     * @param className
     * @param fieldName
     * @param type
     * @return
     */
    public static String getFieldName(String className, String fieldName, CompilationType type) {
        switch (type) {
            case UNOBFUSCATED:
                return fieldName;
            case OBFUSCATED:
                return mappings.getString("fields." + className + "." + fieldName + ".obf");
            case SRG:
                return mappings.getString("fields." + className + "." + fieldName + ".srg");
            default:
                throw new UnsupportedClassVersionError("Unknown CompilationType " + type);
        }
    }

    /**
     * Note that it is only known if the class is obfuscated or not only AFTER transform() has been successfully called
     *
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
    private static String getObfuscatedModifier(boolean obfuscated) {
        return obfuscated ? "obf" : "mcp";
    }

}
