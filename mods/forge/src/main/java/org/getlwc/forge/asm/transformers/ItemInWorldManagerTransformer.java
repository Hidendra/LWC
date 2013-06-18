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

package org.getlwc.forge.asm.transformers;

import org.getlwc.forge.LWC;
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
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;

public class ItemInWorldManagerTransformer extends org.getlwc.forge.asm.AbstractSingleClassTransformer {

    /**
     * The class we are targeting
     */
    private static final String TARGET_CLASS = "ItemInWorldManager";

    public ItemInWorldManagerTransformer() {
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

            if (methodEquals(method, "ItemInWorldManager", "removeBlock")) {

                int blockIndex = 4;
                int mdIndex = 5;

                // find injection point in method (use IFNULL)
                for (int index = 0; index < method.instructions.size(); index++) {

                    // find local Block object node and from that, local object index
                    if (method.instructions.get(index).getType() == AbstractInsnNode.FIELD_INSN) {
                        FieldInsnNode blocksListNode = (FieldInsnNode) method.instructions.get(index);

                        if (blocksListNode.owner.equals(getJavaClassName("Block")) && blocksListNode.name.equals(getFieldName("Block", "blocksList"))) {
                            int offset = 1;

                            while (method.instructions.get(index + offset).getOpcode() != ASTORE) {
                                offset ++;
                            }

                            VarInsnNode blockNode = (VarInsnNode) method.instructions.get(index + offset);
                            blockIndex = blockNode.var;
                        }
                    }

                    // find local metadata variable node and from that, local variable index
                    if (method.instructions.get(index).getType() == AbstractInsnNode.METHOD_INSN) {
                        MethodInsnNode methodNode = (MethodInsnNode) method.instructions.get(index);

                        if (methodNode.owner.equals(getJavaClassName("World")) && methodNode.name.equals(getMethodName("World", "getBlockMetadata"))) {
                            int offset = 1;

                            while (method.instructions.get(index + offset).getOpcode() != ISTORE) {
                                offset ++;
                            }

                            VarInsnNode mdNode = (VarInsnNode) method.instructions.get(index + offset);
                            mdIndex = mdNode.var;
                        }
                    }

                    // inject our event
                    if (method.instructions.get(index).getOpcode() == IFNULL) {

                        int offset = 1;

                        while (method.instructions.get(index + offset).getOpcode() != ALOAD) {
                            offset ++;
                        }

                        LabelNode end = new LabelNode(new Label());

                        // instructions to inject
                        InsnList instructions = new InsnList();

                        // construct instruction nodes for list
                        instructions.add(new VarInsnNode(ALOAD, 0));
                        instructions.add(new FieldInsnNode(GETFIELD, getJavaClassName("ItemInWorldManager"), getFieldName("ItemInWorldManager", "theWorld"), "L" + getJavaClassName("World") + ";"));
                        instructions.add(new VarInsnNode(ILOAD, 1));
                        instructions.add(new VarInsnNode(ILOAD, 2));
                        instructions.add(new VarInsnNode(ILOAD, 3));
                        instructions.add(new VarInsnNode(ALOAD, blockIndex));
                        instructions.add(new VarInsnNode(ILOAD, mdIndex));
                        instructions.add(new VarInsnNode(ALOAD, 0));
                        instructions.add(new FieldInsnNode(GETFIELD, getJavaClassName("ItemInWorldManager"), getFieldName("ItemInWorldManager", "thisPlayerMP"), "L" + getJavaClassName("EntityPlayerMP") + ";"));
                        instructions.add(new MethodInsnNode(INVOKESTATIC, getJavaClassName("ForgeEventHelper"), getMethodName("ForgeEventHelper", "onBlockHarvested"), "(L" + getJavaClassName("World") + ";IIIL" + getJavaClassName("Block") + ";IL" + getJavaClassName("EntityPlayer") + ";)Z"));

                        instructions.add(new JumpInsnNode(IFEQ, end));
                        instructions.add(new InsnNode(ICONST_1));
                        instructions.add(new InsnNode(IRETURN));
                        instructions.add(end);
                        // finished instruction list

                        // inject the instructions
                        method.instructions.insertBefore(method.instructions.get(index + offset), instructions);

                        break;
                    }

                }

            }

        }

        try {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            LWC.instance.getEngine().getConsoleSender().sendMessage("[ASM] Patched " + TARGET_CLASS + " (" + getClassName(TARGET_CLASS) + ") successfully!");
            return writer.toByteArray();
        } catch (Exception e) {
            LWC.instance.getEngine().getConsoleSender().sendMessage("[ASM] Failed to patch " + TARGET_CLASS + " (" + getClassName(TARGET_CLASS) + ")");
            e.printStackTrace();
            return bytes;
        }
    }

}
