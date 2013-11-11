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
    public void transform() {
        if (visitMethod("tryPlaceItemIntoWorld")) {
            int offset = findMethodOpcode(ALOAD);

            if (offset == -1) {
                LWC.instance.getEngine().getConsoleSender().sendMessage(getClass().getSimpleName() + ": No ALOAD instr found");
                return;
            }

            LabelNode end = new LabelNode(new Label());

            addInstruction(new VarInsnNode(ALOAD, 0));
            addInstruction(new VarInsnNode(ALOAD, 1));
            addInstruction(new VarInsnNode(ALOAD, 2));
            addInstruction(new VarInsnNode(ILOAD, 3));
            addInstruction(new VarInsnNode(ILOAD, 4));
            addInstruction(new VarInsnNode(ILOAD, 5));
            addInstruction(new VarInsnNode(ILOAD, 6));
            addInstruction(new VarInsnNode(FLOAD, 7));
            addInstruction(new VarInsnNode(FLOAD, 8));
            addInstruction(new VarInsnNode(FLOAD, 9));
            addInstruction(new MethodInsnNode(INVOKESTATIC, getJavaClassName("ForgeEventHelper"), getMethodName("ForgeEventHelper", "onBlockPlace"), "(L" + getJavaClassName("ItemStack") + ";L" + getJavaClassName("EntityPlayer") + ";L" + getJavaClassName("World") + ";IIIIFFF)Z"));

            addInstruction(new JumpInsnNode(IFEQ, end));
            addInstruction(new InsnNode(ICONST_0));
            addInstruction(new InsnNode(IRETURN));
            addInstruction(end);

            injectMethodBefore(offset);
        }
    }

}