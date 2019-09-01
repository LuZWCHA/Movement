package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.core.common.gui.mygui.slots.ListenableSlot;
import com.nowandfuture.mod.core.common.gui.slots.PrefabOnlySlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;


public class ContainerConstructor extends Container {

    private final IInventory tileConstructor;
    private PrefabOnlySlot slot;

    private float progress;

    public ContainerConstructor(InventoryPlayer playerInventory, IInventory tileConstructor) {
        this.tileConstructor = tileConstructor;

        //add prefab create slot
        slot = new PrefabOnlySlot(playerInventory.player,tileConstructor,
                0,257,57);
        this.addListenableSlotToContainer(slot);

        //add slots from playerInventory
        int i;
        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 117 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(playerInventory, i, 117 + i * 18, 142));
        }
    }
    @Override
    public void updateProgressBar(int id, int data) {
        this.tileConstructor.setField(id, data);
    }

    protected Slot addListenableSlotToContainer(ListenableSlot slot){
        return addSlotToContainer(setListenerForSlot(slot));
    }

    protected ListenableSlot setListenerForSlot(final ListenableSlot slot){
        slot.setChangeListener(new Consumer<ItemStack>() {
            @Override
            public void accept(ItemStack stack) {
                for (int j = 0; j < ContainerConstructor.this.listeners.size(); ++j)
                {
                    ContainerConstructor.this.listeners.get(j).
                            sendSlotContents(ContainerConstructor.this,
                                    slot.slotNumber, slot.getStack());
                }
            }
        });
        return slot;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.tileConstructor.isUsableByPlayer(playerIn);
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
