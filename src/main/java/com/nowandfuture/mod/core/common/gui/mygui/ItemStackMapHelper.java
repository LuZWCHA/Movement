package com.nowandfuture.mod.core.common.gui.mygui;

import com.nowandfuture.mod.core.common.gui.mygui.api.IDynamicInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.LinkedHashMap;
import java.util.Map;

public class ItemStackMapHelper {
    public static ItemStack getAndSplit(Map<Long,ItemStack> stackMap, int index, int amount)
    {
        ItemStack stack = stackMap.get((long)index);
        return stack != null && !stack.isEmpty() && amount > 0 ? stack.splitStack(amount) : ItemStack.EMPTY;
    }

    public static ItemStack getAndRemove(Map<Long,ItemStack> stackMap, long id)
    {
        ItemStack stack = stackMap.get(id);
        return stack != null ? stackMap.replace(id, ItemStack.EMPTY) : ItemStack.EMPTY;
    }

    public static NBTTagCompound saveAllItems(NBTTagCompound tag, Map<Long,ItemStack> stackMap)
    {
        return saveAllItems(tag, stackMap, true);
    }

    public static NBTTagCompound saveAllItems(NBTTagCompound tag, Map<Long,ItemStack> stackMap, boolean saveEmpty)
    {
        NBTTagList nbttaglist = new NBTTagList();
        for (Map.Entry<Long,ItemStack> entry:
             stackMap.entrySet()) {
            ItemStack itemstack = entry.getValue();

            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setLong("Slot", entry.getKey());
            itemstack.writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }

        if (!nbttaglist.isEmpty() || saveEmpty)
        {
            tag.setTag("ItemMap", nbttaglist);
        }

        return tag;
    }

    public static void loadAllItems(NBTTagCompound tag, IDynamicInventory inventory,boolean canBeListened)
    {
        NBTTagList nbttaglist = tag.getTagList("ItemMap", 10);

        Map<Long,ItemStack> stackMap = new LinkedHashMap<>();
        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            long j = nbttagcompound.getLong("Slot");
            ItemStack stack = new ItemStack(nbttagcompound);
            stackMap.put(j,stack);
            inventory.setInventorySlotContents((int)j,stack);
        }

        inventory.syncAs(stackMap,canBeListened);
    }
}
