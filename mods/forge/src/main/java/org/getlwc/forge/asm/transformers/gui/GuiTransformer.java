package org.getlwc.forge.asm.transformers.gui;

import org.getlwc.forge.LWC;
import org.getlwc.forge.asm.AbstractMultiClassTransformer;
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

public class GuiTransformer extends AbstractMultiClassTransformer {

    /**
     * The class we are targeting
     */
    private static final String[] TARGET_CLASSES = new String[] {
            "GuiChest", "GuiFurnace", "GuiDispenser"
    };

    public GuiTransformer() {
        super(TARGET_CLASSES);
    }

    @Override
    public byte[] transform(String className, byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(classNode, 0);

        Iterator iter = classNode.methods.iterator();

        while (iter.hasNext()) {
            MethodNode method = (MethodNode) iter.next();

            if (methodEquals(method, "GuiContainer", "drawGuiContainerForegroundLayer")) {
                InsnList instructions = new InsnList();

                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new MethodInsnNode(INVOKESTATIC, getJavaClassName("GuiHelper"), getMethodName("GuiHelper", "drawGuiContainerForegroundLayer"), "(L" + getJavaClassName("GuiContainer") + ";)V"));

                method.instructions.insert(method.instructions.getFirst(), instructions);
                break;
            }

        }

        try {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            LWC.instance.getEngine().getConsoleSender().sendTranslatedMessage("[ASM] Patched {0} ({1}) successfully!", getClassName(className, false), getClassName(className));
            return writer.toByteArray();
        } catch (Exception e) {
            LWC.instance.getEngine().getConsoleSender().sendTranslatedMessage("[ASM] Failed to patch {0} ({1})", getClassName(className, false), getClassName(className));
            e.printStackTrace();
            return bytes;
        }
    }

}