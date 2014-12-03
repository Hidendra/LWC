package org.getlwc.forge.asm.transformers.events;

import org.getlwc.forge.asm.AbstractMultiClassTransformer;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

public class RedstoneTransformer extends AbstractMultiClassTransformer {

    /**
     * The classes we are targeting
     */
    private static final String[] TARGET_CLASSES = new String[] { "BlockDoor", "BlockTrapDoor" };

    public RedstoneTransformer() {
        super(TARGET_CLASSES);
    }

    @Override
    public void transform() {
        if (visitMethod("func_150014_a")) { // func_150014_a = onPoweredBlockChange
            LabelNode end = new LabelNode(new Label());

            addInstruction(new VarInsnNode(ALOAD, 1));
            addInstruction(new VarInsnNode(ILOAD, 2));
            addInstruction(new VarInsnNode(ILOAD, 3));
            addInstruction(new VarInsnNode(ILOAD, 4));
            addInstruction(new VarInsnNode(ILOAD, 5));
            addInstruction(new MethodInsnNode(INVOKESTATIC, getJavaClassName("ForgeEventHelper"), getMethodName("ForgeEventHelper", "onRedstoneChange"), "(L" + getJavaClassName("World") + ";IIIZ)Z"));

            addInstruction(new JumpInsnNode(IFEQ, end));
            addInstruction(new InsnNode(RETURN));
            addInstruction(end);

            injectMethod();
        }
    }

}
