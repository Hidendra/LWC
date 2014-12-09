/**
 * Copyright (c) 2011-2014 Tyler Blair
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
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
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

import org.getlwc.forge.ForgeMod;
import org.getlwc.forge.asm.AbstractSingleClassTransformer;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;

public class BlockPlaceTransformer extends AbstractSingleClassTransformer {

    /**
     * The class we are targeting
     */
    private static final String TARGET_CLASS = "ItemStack";

    public BlockPlaceTransformer() {
        super(TARGET_CLASS);
    }

    @Override
    public void transform() {
        if (visitMethod("tryPlaceItemIntoWorld")) {
            int offset = findMethodOpcode(ALOAD);

            if (offset == -1) {
                ForgeMod.instance.getEngine().getConsoleSender().sendMessage(getClass().getSimpleName() + ": No ALOAD instr found");
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