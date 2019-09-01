package com.nowandfuture.mod.core.common.gui.mygui.slots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

@Deprecated
public class ListenableSlot extends Slot {

    private final EntityPlayer player;
    private Consumer<ItemStack> changeListener;

    public ListenableSlot(EntityPlayer player, IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
        this.player = player;
    }

    public void setChangeListener(Consumer<ItemStack> changeListener) {
        this.changeListener = changeListener;
    }
}
