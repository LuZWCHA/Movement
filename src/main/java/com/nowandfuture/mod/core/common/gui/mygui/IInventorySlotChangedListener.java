package com.nowandfuture.mod.core.common.gui.mygui;

import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemStack;

public interface IInventorySlotChangedListener extends IInventoryChangedListener {
    void slotRemoved(ItemStack itemStack);
    void slotAdded(ItemStack itemStack);
}
