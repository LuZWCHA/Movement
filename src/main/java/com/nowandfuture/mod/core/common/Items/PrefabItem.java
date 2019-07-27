package com.nowandfuture.mod.core.common.Items;

import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import joptsimple.internal.Strings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class PrefabItem extends Item {

    public PrefabItem() {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    public static String getPrefabName(ItemStack stack){
        NBTTagCompound compound = stack.getTagCompound();
        if(compound != null && compound.hasKey(AbstractPrefab.NBT_PREFAB_NAME)){
            return compound.getString(AbstractPrefab.NBT_PREFAB_NAME);
        }
        return Strings.EMPTY;
    }

    public static void setPrefabName(ItemStack stack,String name){

        if(stack.getTagCompound() == null) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString(AbstractPrefab.NBT_PREFAB_NAME, name);
            stack.setTagCompound(compound);
        }else {
            stack.getTagCompound().setString(AbstractPrefab.NBT_PREFAB_NAME, name);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String name = getPrefabName(stack);
        return name;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, 0);
    }

    @Override
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        return false;
    }
}
