/*
 * Copyright (c) 2011-2013 Tyler Blair
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR,
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package org.getlwc.forge.asm.transformers.events;

import org.getlwc.forge.LWC;
import org.getlwc.forge.asm.AbstractSingleClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Iterator;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

public class ExplosionTransformer extends AbstractSingleClassTransformer {

    /**
     * The class we are targeting
     */
    private static final String TARGET_CLASS = "Explosion";

    public ExplosionTransformer() {
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

            if (methodEquals(method, "Explosion", "doExplosionA")) {
                // find offset for h.addAll ( affectedBlockPositions )
                int offset = -1;

                for (int index = 0; index < method.instructions.size(); index++) {

                    if (method.instructions.get(index).getType() == AbstractInsnNode.METHOD_INSN) {
                        MethodInsnNode node = (MethodInsnNode) method.instructions.get(index);

                        if (node.owner.equals("java/util/List") && node.name.equals("addAll")) {
                            offset = index;
                            break;
                        }

                    }

                }

                if (offset == -1) {
                    System.out.println("Could not find addAll instruction point in Explosion");
                    break;
                }

                LabelNode end = new LabelNode(new Label());

                // instructions to inject
                InsnList instructions = new InsnList();

                // construct instruction nodes for list
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new FieldInsnNode(GETFIELD, getJavaClassName("Explosion"), getFieldName("Explosion", "worldObj"), "L" + getJavaClassName("World") + ";"));
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new FieldInsnNode(GETFIELD, getJavaClassName("Explosion"), getFieldName("Explosion", "explosionX"), "D"));
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new FieldInsnNode(GETFIELD, getJavaClassName("Explosion"), getFieldName("Explosion", "explosionY"), "D"));
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new FieldInsnNode(GETFIELD, getJavaClassName("Explosion"), getFieldName("Explosion", "explosionZ"), "D"));
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new FieldInsnNode(GETFIELD, getJavaClassName("Explosion"), getFieldName("Explosion", "field_77289_h"), "I"));
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new FieldInsnNode(GETFIELD, getJavaClassName("Explosion"), getFieldName("Explosion", "affectedBlockPositions"), "Ljava/util/List;"));
                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new FieldInsnNode(GETFIELD, getJavaClassName("Explosion"), getFieldName("Explosion", "exploder"), "L" + getJavaClassName("Entity") + ";"));
                instructions.add(new MethodInsnNode(INVOKESTATIC, getJavaClassName("ForgeEventHelper"), getMethodName("ForgeEventHelper", "onExplosion"), "(L" + getJavaClassName("World") + ";DDDILjava/util/List;L" + getJavaClassName("Entity") + ";)Z"));

                instructions.add(new JumpInsnNode(IFEQ, end));
                instructions.add(new InsnNode(RETURN));
                instructions.add(end);
                // finished instruction list

                // inject the instructions
                method.instructions.insert(method.instructions.get(offset), instructions);

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