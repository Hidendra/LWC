package org.getlwc.forge.asm.transformers;

import org.getlwc.forge.LWC;
import org.getlwc.forge.asm.AbstractSingleClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Iterator;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

public class SignUpdateTransformer extends AbstractSingleClassTransformer {

    /**
     * The class we are targeting
     */
    private static final String TARGET_CLASS = "NetServerHandler";

    public SignUpdateTransformer() {
        super(TARGET_CLASS);
    }

    @Override
    public byte[] transform(byte[] bytes) {

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(classNode, 0);

        // find method to inject into
        Iterator iter = classNode.methods.iterator();

        while (iter.hasNext()) {
            MethodNode method = (MethodNode) iter.next();

            if (methodEquals(method, "NetServerHandler", "handleUpdateSign")) {
                int signIndex = -1;

                for (Object o : method.localVariables) {
                    LocalVariableNode variable = (LocalVariableNode) o;

                    if (variableMatchesClass(variable, "TileEntitySign")) {
                        signIndex = variable.index;
                    }

                    // System.out.println("index=" + variable.index + " desc=" + variable.desc + " name=" + variable.name);
                }

                // System.out.println("sign index = " +signIndex);

                if (signIndex == -1) {
                    System.out.println("Failed to find TileEntitySign in NetServerHandler");
                    break;
                }

                // Look for INSTANCEOF in reverse (we don't want the first one)
                for (int index = method.instructions.size() - 1; index >= 0; index--) {

                    if (method.instructions.get(index).getOpcode() == Opcodes.INSTANCEOF) {

                        int offset = index;

                        // skip to ASTORE instruction for the TileEntitySign
                        while (true) {
                            if (method.instructions.get(offset).getOpcode() == ASTORE) {
                                VarInsnNode node = (VarInsnNode) method.instructions.get(offset);

                                if (node.var == signIndex) {
                                    break;
                                }
                            }

                            offset++;
                        }

                        // System.out.println("Injecting to offset " + offset);

                        LabelNode end = new LabelNode(new Label());

                        // instructions to inject
                        InsnList instructions = new InsnList();

                        // construct instruction nodes for list
                        instructions.add(new VarInsnNode(ALOAD, 0));
                        instructions.add(new FieldInsnNode(GETFIELD, getJavaClassName("NetServerHandler"), getFieldName("NetServerHandler", "playerEntity"), "L" + getJavaClassName("EntityPlayerMP") + ";"));
                        instructions.add(new VarInsnNode(ALOAD, 1));
                        instructions.add(new VarInsnNode(ALOAD, signIndex)); // jbe - 7
                        instructions.add(new MethodInsnNode(INVOKESTATIC, getJavaClassName("ForgeEventHelper"), getMethodName("ForgeEventHelper", "onUpdateSign"), "(L" + getJavaClassName("EntityPlayerMP") + ";L" + getJavaClassName("Packet130UpdateSign") + ";L" + getJavaClassName("TileEntitySign") + ";)Z"));

                        instructions.add(new JumpInsnNode(IFEQ, end));
                        instructions.add(new InsnNode(RETURN));
                        instructions.add(end);
                        // finished instruction list

                        // inject the instructions
                        method.instructions.insert(method.instructions.get(offset), instructions);

                        break;
                    }

                }

                break;
            }

        }

        try {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            LWC.instance.getEngine().getConsoleSender().sendTranslatedMessage("[ASM] Patched {0} ({1}) successfully!", getClassName(TARGET_CLASS, false), getClassName(TARGET_CLASS));
            return writer.toByteArray();
        } catch (Exception e) {
            LWC.instance.getEngine().getConsoleSender().sendTranslatedMessage("[ASM] Failed to patch {0} ({1})", getClassName(TARGET_CLASS, false), getClassName(TARGET_CLASS));
            e.printStackTrace();
            return bytes;
        }
    }

}