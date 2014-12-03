package org.getlwc.forge.asm.transformers.events;

import org.getlwc.forge.asm.AbstractSingleClassTransformer;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;

public class HopperSuckItemTransformer extends AbstractSingleClassTransformer {

    public HopperSuckItemTransformer() {
        super("TileEntityHopper");
    }

    @Override
    public void transform() {
        if (visitMethod("func_145891_a")) { // suckItemsIntoHopper
            LabelNode end = new LabelNode(new Label());

            addInstruction(new VarInsnNode(ALOAD, 0));
            addInstruction(new InsnNode(ICONST_1)); // isPullingItems = true
            addInstruction(new MethodInsnNode(INVOKESTATIC, getJavaClassName("ForgeEventHelper"), getMethodName("ForgeEventHelper", "onInventoryMoveItem"), "(L" + getJavaClassName("IHopper") + ";Z)Z"));

            addInstruction(new JumpInsnNode(IFEQ, end));
            addInstruction(new InsnNode(ICONST_0));
            addInstruction(new InsnNode(IRETURN));
            addInstruction(end);

            injectMethod();
        }
    }

}