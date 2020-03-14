package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.LinkedHashMap;
import java.util.Map;

public interface IDynamicInventory extends IInventory {
    void removeSlot(long id);
    void createSlot(long id, AbstractContainer.ProxySlot stack);
    void createSlot(long id, ItemStack stack, SlotCreator creator,int type);
    Map<Long, AbstractContainer.ProxySlot> getSlots();
    Map.Entry<Long,ItemStack> getEntryByIndex(int index);

    abstract class SlotCreator{
        public abstract AbstractContainer.ProxySlot create(IDynamicInventory inventory, long index,int type);
    }

    static void createSlots(IDynamicInventory inventory,Map<Long,ItemStack> map,Map<Long,Integer> types,SlotCreator creator){
        for (Map.Entry<Long,ItemStack> entry:
             map.entrySet()) {
            inventory.createSlot(entry.getKey(),entry.getValue(),creator,types.get(entry.getKey()));
        }
    }

    static NBTTagCompound saveSlots(NBTTagCompound tag, IDynamicInventory inventory){
        NBTTagList nbttaglist = new NBTTagList();
        for (Map.Entry<Long, AbstractContainer.ProxySlot> entry:
                inventory.getSlots().entrySet()) {
            int type = entry.getValue().getType();

            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setLong("SlotId", entry.getKey());
            nbttagcompound.setInteger("SlotType",type);
            nbttaglist.appendTag(nbttagcompound);
        }

        if (!nbttaglist.isEmpty())
        {
            tag.setTag("SlotMap", nbttaglist);
        }

        return tag;
    }

    static void loadAllSlots(NBTTagCompound tag, DynamicInventory inventory)
    {
        loadAllSlots(tag, inventory,inventory.getCreator());
    }

    static void loadAllSlots(NBTTagCompound tag, IDynamicInventory inventory,SlotCreator creator)
    {
        NBTTagList nbttaglist = tag.getTagList("SlotMap", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            long j = nbttagcompound.getLong("SlotId");
            int type = nbttagcompound.getInteger("SlotType");
            inventory.getSlots().put(j, creator.create(inventory,j,type));
        }
    }
}
