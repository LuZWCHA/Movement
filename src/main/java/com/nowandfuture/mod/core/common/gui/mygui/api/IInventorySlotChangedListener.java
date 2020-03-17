package com.nowandfuture.mod.core.common.gui.mygui.api;

import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemStack;

public interface IInventorySlotChangedListener extends IInventoryChangedListener {
    void slotRemoved(long id,ItemStack itemStack,boolean forced);
    void slotAdded(long id,ItemStack itemStack,boolean forced);
}
