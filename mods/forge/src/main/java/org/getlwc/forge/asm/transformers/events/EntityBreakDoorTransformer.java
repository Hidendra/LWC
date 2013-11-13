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

public class EntityBreakDoorTransformer extends AbstractSingleClassTransformer {

    public EntityBreakDoorTransformer() {
        super("EntityAIBreakDoor");
    }

    @Override
    public void transform() {
        if (visitMethod("updateTask")) {
            LabelNode end = new LabelNode(new Label());

            addInstruction(new VarInsnNode(ALOAD, 0));
            addInstruction(new FieldInsnNode(GETFIELD, getJavaClassName("EntityAIDoorInteract"), getFieldName("EntityAIDoorInteract", "theEntity"), "L" + getJavaClassName("EntityLiving") + ";"));
            addInstruction(new VarInsnNode(ALOAD, 0));
            addInstruction(new FieldInsnNode(GETFIELD, getJavaClassName("EntityAIDoorInteract"), getFieldName("EntityAIDoorInteract", "entityPosX"), "I"));
            addInstruction(new VarInsnNode(ALOAD, 0));
            addInstruction(new FieldInsnNode(GETFIELD, getJavaClassName("EntityAIDoorInteract"), getFieldName("EntityAIDoorInteract", "entityPosY"), "I"));
            addInstruction(new VarInsnNode(ALOAD, 0));
            addInstruction(new FieldInsnNode(GETFIELD, getJavaClassName("EntityAIDoorInteract"), getFieldName("EntityAIDoorInteract", "entityPosZ"), "I"));
            addInstruction(new MethodInsnNode(INVOKESTATIC, getJavaClassName("ForgeEventHelper"), getMethodName("ForgeEventHelper", "onEntityBreakDoor"), "(L" + getJavaClassName("EntityLiving") + ";III)Z"));

            addInstruction(new JumpInsnNode(IFEQ, end));
            addInstruction(new InsnNode(RETURN));
            addInstruction(end);

            injectMethod();
        }
    }

}
