package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.core.common.Items.TimelineItem;
import com.nowandfuture.mod.core.common.entities.TileEntityCoreModule;
import com.nowandfuture.mod.core.common.gui.slots.PrefabOnlySlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerModule extends Container {

    private TileEntityCoreModule tileEntityModule;
    private PrefabOnlySlot slot;

    public ContainerModule(InventoryPlayer playerInventory, TileEntityCoreModule tileEntityModule){
        super();
        this.tileEntityModule = tileEntityModule;

        slot = new PrefabOnlySlot(playerInventory.player,tileEntityModule,
                0,14,57);
        this.addSlotToContainer(slot);

        this.addSlotToContainer(new Slot(tileEntityModule,1,81,57){
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof TimelineItem;
            }
        });

        int i;
        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 9 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(playerInventory, i, 9 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < this.inventorySlots.size())
            {
                if (!this.mergeItemStack(itemstack1, this.inventorySlots.size(), this.inventorySlots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, this.inventorySlots.size(), false))
            {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }
}
