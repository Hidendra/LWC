package org.getlwc.forge.asm.transformers.misc;

import org.getlwc.forge.asm.AbstractSingleClassTransformer;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

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
    public void transform() {
        if (visitMethod("updateClientInfo")) {
            addInstruction(new VarInsnNode(ALOAD, 0));
            addInstruction(new VarInsnNode(ALOAD, 1));
            addInstruction(new MethodInsnNode(INVOKESTATIC, getJavaClassName("ForgeEventHelper"), getMethodName("ForgeEventHelper", "onUpdateClientInfo"), "(L" + getJavaClassName("EntityPlayerMP") + ";L" + getJavaClassName("C15PacketClientSettings") + ";)V"));

            injectMethod();
        }
    }

}
