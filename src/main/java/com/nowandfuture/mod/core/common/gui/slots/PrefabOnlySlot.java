package com.nowandfuture.mod.core.common.gui.slots;

import com.nowandfuture.mod.core.common.Items.PrefabItem;
import com.nowandfuture.mod.core.common.gui.mygui.slots.ListenableSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class PrefabOnlySlot extends ListenableSlot {

    public PrefabOnlySlot(EntityPlayer player, IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(player, inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof PrefabItem;
    }
}
