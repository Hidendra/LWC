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

package org.getlwc.forge.asm;

import org.getlwc.forge.LWC;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
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

import static org.objectweb.asm.Opcodes.*;

public class ExplosionTransformer extends org.getlwc.forge.asm.AbstractSingleClassTransformer {

    /**
     * The class we are targeting
     */
    private static final String TARGET_CLASS = "Explosion";

    public ExplosionTransformer() {
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

            if (method.desc.equals("()V") && method.name.equals(getMethodName("Explosion", "doExplosionA"))) {
                // new label for the end of our code
                LabelNode lmm1Node = new LabelNode(new Label());

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
                instructions.add(new FieldInsnNode(GETFIELD, getJavaClassName("Explosion"), getFieldName("Explosion", "exploder"), "L" + getJavaClassName("Entity") + ";"));
                instructions.add(new MethodInsnNode(INVOKESTATIC, getJavaClassName("ForgeEventHelper"), getMethodName("ForgeEventHelper", "onExplosion"), "(L" + getJavaClassName("World") + ";DDDIL" + getJavaClassName("Entity") + ";)Z"));

                // return from onExplosion()
                LabelNode cancel = new LabelNode(new Label());
                instructions.add(new JumpInsnNode(IFNE, cancel));
                instructions.add(new JumpInsnNode(GOTO, lmm1Node));
                instructions.add(cancel);
                instructions.add(new InsnNode(RETURN));
                // end

                instructions.add(lmm1Node);
                // finished instruction list

                // inject the instructions
                method.instructions.insert(instructions);

                LWC.instance.getEngine().getConsoleSender().sendMessage("[ASM] Injected " + TARGET_CLASS);

                break;
            }

        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

}