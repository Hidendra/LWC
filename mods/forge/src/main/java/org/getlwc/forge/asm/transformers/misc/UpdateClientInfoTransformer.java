package org.getlwc.forge.asm.transformers.misc;

import org.getlwc.forge.LWC;
import org.getlwc.forge.asm.AbstractSingleClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Iterator;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class UpdateClientInfoTransformer extends AbstractSingleClassTransformer {

    /**
     * The class we are targeting
     */
    private static final String TARGET_CLASS = "EntityPlayerMP";

    public UpdateClientInfoTransformer() {
        super(TARGET_CLASS);
    }

    @Override
    public byte[] transform(String className, byte[] bytes) {

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(classNode, 0);

        // find method to inject into
        Iterator iter = classNode.methods.iterator();

        while (iter.hasNext()) {
            MethodNode method = (MethodNode) iter.next();

            if (methodEquals(method, "EntityPlayerMP", "updateClientInfo")) {
                // instructions to inject
                InsnList instructions = new InsnList();

                // construct instruction nodes for list
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new VarInsnNode(ALOAD, 1));
                instructions.add(new MethodInsnNode(INVOKESTATIC, getJavaClassName("ForgeEventHelper"), getMethodName("ForgeEventHelper", "onUpdateClientInfo"), "(L" + getJavaClassName("EntityPlayerMP") + ";L" + getJavaClassName("Packet204ClientInfo") + ";)V"));
                // finished instruction list

                // inject the instructions
                method.instructions.insert(instructions);

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
