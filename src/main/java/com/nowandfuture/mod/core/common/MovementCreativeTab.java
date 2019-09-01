package com.nowandfuture.mod.core.common;

import com.nowandfuture.mod.handler.RegisterHandler;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class MovementCreativeTab extends CreativeTabs {

    public MovementCreativeTab(String label) {
        super(label);
    }

    public MovementCreativeTab(int index, String label) {
        super(index, label);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(RegisterHandler.constructorBlock);
    }
}
