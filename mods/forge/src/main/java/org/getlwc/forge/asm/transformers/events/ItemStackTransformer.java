package org.getlwc.forge.asm.transformers.events;

import org.getlwc.forge.LWC;
import org.getlwc.forge.asm.AbstractSingleClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Iterator;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;

public class ItemStackTransformer extends AbstractSingleClassTransformer {

    /**
     * The class we are targeting
     */
    private static final String TARGET_CLASS = "ItemStack";

    public ItemStackTransformer() {
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

            if (methodEquals(method, "ItemStack", "tryPlaceItemIntoWorld")) {
                int offset = 0;

                while (method.instructions.get(offset).getOpcode() != ALOAD) {
                    offset++;
                }

                // System.out.println("Injecting to offset " + offset);

                LabelNode end = new LabelNode(new Label());

                // instructions to inject
                InsnList instructions = new InsnList();

                // construct instruction nodes for list
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new VarInsnNode(ALOAD, 1));
                instructions.add(new VarInsnNode(ALOAD, 2));
                instructions.add(new VarInsnNode(ILOAD, 3));
                instructions.add(new VarInsnNode(ILOAD, 4));
                instructions.add(new VarInsnNode(ILOAD, 5));
                instructions.add(new VarInsnNode(ILOAD, 6));
                instructions.add(new VarInsnNode(FLOAD, 7));
                instructions.add(new VarInsnNode(FLOAD, 8));
                instructions.add(new VarInsnNode(FLOAD, 9));
                instructions.add(new MethodInsnNode(INVOKESTATIC, getJavaClassName("ForgeEventHelper"), getMethodName("ForgeEventHelper", "onBlockPlace"), "(L" + getJavaClassName("ItemStack") + ";L" + getJavaClassName("EntityPlayer") + ";L" + getJavaClassName("World") + ";IIIIFFF)Z"));

                instructions.add(new JumpInsnNode(IFEQ, end));
                instructions.add(new InsnNode(ICONST_0));
                instructions.add(new InsnNode(IRETURN));
                instructions.add(end);
                // finished instruction list

                // inject the instructions
                method.instructions.insertBefore(method.instructions.get(offset), instructions);

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