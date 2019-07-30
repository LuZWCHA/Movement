package com.nowandfuture.mod.core.common.gui.slots;

import com.nowandfuture.mod.core.common.Items.TimelineItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class TimelineOutputSlot extends Slot {

    public TimelineOutputSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof TimelineItem;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
