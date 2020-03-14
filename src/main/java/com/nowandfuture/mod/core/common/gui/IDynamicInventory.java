package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

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

    Random RANDOM = new Random();

    static void dropInventoryItems(World worldIn, BlockPos pos, IDynamicInventory inventory)
    {
        dropInventoryItems(worldIn, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), inventory);
    }

    static void dropInventoryItems(World worldIn, Entity entityAt, IDynamicInventory inventory)
    {
        dropInventoryItems(worldIn, entityAt.posX, entityAt.posY, entityAt.posZ, inventory);
    }

    static void dropInventoryItems(World worldIn, double x, double y, double z, IDynamicInventory inventory)
    {
        inventory.getSlots().forEach(new BiConsumer<Long, AbstractContainer.ProxySlot>() {
            @Override
            public void accept(Long aLong, AbstractContainer.ProxySlot proxySlot) {
                ItemStack itemstack = inventory.getStackInSlot(Math.toIntExact(aLong));

                if (!itemstack.isEmpty())
                {
                    spawnItemStack(worldIn, x, y, z, itemstack);
                }
            }
        });

    }

    static void spawnItemStack(World worldIn, double x, double y, double z, ItemStack stack)
    {
        float f = RANDOM.nextFloat() * 0.8F + 0.1F;
        float f1 = RANDOM.nextFloat() * 0.8F + 0.1F;
        float f2 = RANDOM.nextFloat() * 0.8F + 0.1F;

        while (!stack.isEmpty())
        {
            EntityItem entityitem = new EntityItem(worldIn, x + (double)f, y + (double)f1, z + (double)f2, stack.splitStack(RANDOM.nextInt(21) + 10));
            float f3 = 0.05F;
            entityitem.motionX = RANDOM.nextGaussian() * 0.05000000074505806D;
            entityitem.motionY = RANDOM.nextGaussian() * 0.05000000074505806D + 0.20000000298023224D;
            entityitem.motionZ = RANDOM.nextGaussian() * 0.05000000074505806D;
            worldIn.spawnEntity(entityitem);
        }
    }
}
