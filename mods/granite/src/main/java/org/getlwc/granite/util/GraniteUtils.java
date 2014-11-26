package org.getlwc.granite.util;

import org.getlwc.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class GraniteUtils {

    private GraniteUtils() {
    }

    /**
     * Casts a native item stack into the internal representation
     *
     * @param itemStackHandle
     * @return
     */
    public static ItemStack castItemStack(org.granitemc.granite.api.item.ItemStack itemStackHandle) {
        // TODO get enchantments from the item
        Map<Integer, Integer> enchantments = new HashMap<>();

        return new ItemStack(itemStackHandle.getType().getNumericId(), itemStackHandle.getStackSize(), (short) itemStackHandle.getItemDamage(), itemStackHandle.getMaxDamage(), enchantments);
    }

}
