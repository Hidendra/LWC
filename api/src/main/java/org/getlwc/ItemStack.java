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
