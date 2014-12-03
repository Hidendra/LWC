package org.getlwc.forge.asm.transformers.events;

import org.getlwc.forge.asm.AbstractSingleClassTransformer;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

public class SignUpdateTransformer extends AbstractSingleClassTransformer {

    /**
     * The class we are targeting
     */
    private static final String TARGET_CLASS = "NetHandlerPlayServer";

    public SignUpdateTransformer() {
        super(TARGET_CLASS);
    }

    @Override
    public void transform() {
        if (visitMethod("processUpdateSign")) {
            LabelNode end = new LabelNode(new Label());

            addInstruction(new VarInsnNode(ALOAD, 0));
            addInstruction(new FieldInsnNode(GETFIELD, getJavaClassName("NetHandlerPlayServer"), getFieldName("NetHandlerPlayServer", "playerEntity"), "L" + getJavaClassName("EntityPlayerMP") + ";"));
            addInstruction(new VarInsnNode(ALOAD, 1));
            addInstruction(new MethodInsnNode(INVOKESTATIC, getJavaClassName("ForgeEventHelper"), getMethodName("ForgeEventHelper", "onUpdateSign"), "(L" + getJavaClassName("EntityPlayerMP") + ";L" + getJavaClassName("C12PacketUpdateSign") + ";)Z"));

            addInstruction(new JumpInsnNode(IFEQ, end));
            addInstruction(new InsnNode(RETURN));
            addInstruction(end);

            injectMethod();
        }
    }

}