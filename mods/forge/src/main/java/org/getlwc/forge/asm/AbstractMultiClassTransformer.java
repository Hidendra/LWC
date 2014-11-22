package org.getlwc.forge.asm;

import org.getlwc.forge.LWC;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;

public abstract class AbstractMultiClassTransformer extends AbstractTransformer {

    /**
     * The class name (simple, not canonical / full) of the desired class we want to transform
     */
    private final String[] classNames;

    /**
     * If the classes are detected to be obfuscated or not
     */
    private boolean obfuscated = false;

    /**
     * The class node we are operating on
     */
    private ClassNode classNode = new ClassNode();

    /**
     * The list of instructions to be injected into a method
     */
    private InsnList instructions = new InsnList();

    /**
     * The method we are currently visiting
     */
    private MethodNode currentMethod = null;

    /**
     * The class we are currently rewriting
     */
    protected String targetClass = null;

    /**
     * If the class was matched and attempted transformations but was not changed at all
     */
    private boolean changed = false;

    public AbstractMultiClassTransformer(String[] classNames) {
        this.classNames = classNames;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {

        if (mappings == null) {
            // System.out.println("mappings null, name = " + name);
            return bytes;
        }

        boolean transformed = false;
        targetClass = null;

        try {

            for (String className : classNames) {
                if (name.equals(getClassName(className, false))) {
                    obfuscated = false;
                    targetClass = className;
                } else if (name.equals(getClassName(className, true))) {
                    obfuscated = true;
                    targetClass = className;
                }

                if (targetClass != null) {
                    LWC.instance.ensurePostLoaded();

                    instructions = new InsnList();
                    classNode = new ClassNode();

                    ClassReader reader = new ClassReader(bytes);
                    reader.accept(classNode, 0);

                    transform();
                    transformed = true;
                    break;
                }
            }

            if (transformed) {
                if (changed) {
                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    classNode.accept(writer);
                    LWC.instance.getEngine().getConsoleSender().sendFormattedMessage("[ASM] Patched {0} ({1}) successfully!", getClass().getSimpleName() + "::" + targetClass, getClassName(targetClass));
                    return writer.toByteArray();
                } else {
                    LWC.instance.getEngine().getConsoleSender().sendFormattedMessage("[ASM] {0} ({1}) was not changed during transformations", getClass().getSimpleName() + "::" + targetClass, getClassName(targetClass));
                    return bytes;
                }
            }
        } catch (Exception e) {
            LWC.instance.getEngine().getConsoleSender().sendFormattedMessage("[ASM] Failed to patch {0} ({1})", getClass().getSimpleName() + "::" + targetClass, getClassName(targetClass));
            e.printStackTrace();
            return bytes;
        }

        return bytes;
    }

    /**
     * Transform the given matched class
     *
     * @return
     */
    public abstract void transform();

    /**
     * Visit a method and allow writing starting at the beginning of the method
     *
     * @param methodName
     */
    public boolean visitMethod(String methodName) {
        Iterator iter = classNode.methods.iterator();

        while (iter.hasNext()) {
            MethodNode method = (MethodNode) iter.next();

            if (methodEquals(method, targetClass, methodName)) {
                currentMethod = method;
                return true;
            }
        }

        return false;
    }

    /**
     * Add an instruction to the list of instructions
     *
     * @param insn
     */
    public void addInstruction(AbstractInsnNode insn) {
        instructions.add(insn);
    }

    /**
     * Finds the index of the given local variable using its class type
     *
     * @param className the name of the class of the variable's type
     * @return the index of the variable if found otherwise -1
     */
    public int findMethodLocalVariable(String className) {
        for (Object o : currentMethod.localVariables) {
            LocalVariableNode var = (LocalVariableNode) o;

            if (variableMatchesClass(var, className)) {
                return var.index;
            }
        }

        return -1;
    }

    /**
     * Finds the index of a method call in the method
     *
     * @param className
     * @param methodName
     * @return the index of the method call otherwise -1
     */
    public int findMethodCall(String className, String methodName) {
        for (int index = 0; index < currentMethod.instructions.size(); index++) {
            if (currentMethod.instructions.get(index).getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode node = (MethodInsnNode) currentMethod.instructions.get(index);

                if (node.owner.equals(className) && node.name.equals(methodName)) {
                    return index;
                }

            }
        }

        return -1;
    }

    /**
     * Finds the first instance of the given opcode in the function
     *
     * @param opcode
     * @return the offset in the function the opcode is located at otherwise -1 if not found
     */
    public int findMethodOpcode(int opcode) {
        int offset = 0;

        if (getMethodOpcode(offset) == opcode) {
            return offset;
        }

        int max = currentMethod.instructions.size() - 1;
        while (offset <= max && getMethodOpcode(offset) != opcode) {
            offset++;
        }

        return offset == 0 ? -1 : offset;
    }

    /**
     * Finds the last instance of the given opcode in the function
     *
     * @param opcode
     * @return the offset in the function the opcode is located at otherwise -1 if not found
     */
    public int findMethodLastOpcode(int opcode) {
        int offset = currentMethod.instructions.size() - 1;

        if (getMethodOpcode(offset) == opcode) {
            return offset;
        }

        while (offset >= 0 && getMethodOpcode(offset) != opcode) {
            offset--;
        }

        return offset == 0 ? -1 : offset;
    }

    /**
     * Inject the instruction list to the beginning of the method
     */
    public void injectMethod() {
        currentMethod.instructions.insert(instructions);
        changed = true;
    }

    /**
     * Inject the instruction list at the given offset in the method
     *
     * @param offset
     */
    public void injectMethod(int offset) {
        currentMethod.instructions.insert(currentMethod.instructions.get(offset), instructions);
        changed = true;
    }

    /**
     * Inject the instruction list before the given offset in the method
     *
     * @param offset
     */
    public void injectMethodBefore(int offset) {
        currentMethod.instructions.insertBefore(currentMethod.instructions.get(offset), instructions);
        changed = true;
    }

    /**
     * Get the opcode at the given offset in the method
     *
     * @param offset
     * @return
     */
    public int getMethodOpcode(int offset) {
        return currentMethod.instructions.get(offset).getOpcode();
    }

    /**
     * Check if a given {@link org.objectweb.asm.tree.MethodNode} equals the method in the given method
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
     * Get the method name to use in the given class
     *
     * @param className
     * @param methodName
     * @param type
     * @return
     */
    public static String getMethodName(String className, String methodName, CompilationType type) {
        switch (type) {
            case UNOBFUSCATED:
                return methodName;
            case OBFUSCATED:
                return mappings.getString("methods." + className + "." + methodName + ".obf");
            case SRG:
                return mappings.getString("methods." + className + "." + methodName + ".srg");
            default:
                throw new UnsupportedClassVersionError("Unknown CompilationType " + type);
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
