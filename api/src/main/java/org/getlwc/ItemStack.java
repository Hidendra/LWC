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
package org.getlwc;

import java.util.HashMap;
import java.util.Map;

public final class ItemStack {

    /**
     * The item id
     */
    private final int id;

    /**
     * The amount of the item in the stack
     */
    private final int amount;

    /**
     * The item's durability
     */
    private final short durability;

    /**
     * The max stack size for this item, -1 if unknown
     */
    private final int maxStackSize;

    /**
     * The enchantments on the ItemStack
     */
    private final Map<Integer, Integer> enchantments = new HashMap<Integer, Integer>();

    public ItemStack(int id, int amount, short durability, int maxStackSize, Map<Integer, Integer> enchantments) {
        this.id = id;
        this.amount = amount;
        this.durability = durability;
        this.maxStackSize = maxStackSize;
        this.enchantments.putAll(enchantments);
    }

    public int getType() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public short getDurability() {
        return durability;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public Map<Integer, Integer> getEnchantments() {
        return enchantments;
    }
}
