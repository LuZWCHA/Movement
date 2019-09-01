package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.core.common.entities.TileEntityTimelineEditor;
import com.nowandfuture.mod.core.common.gui.mygui.slots.ListenableSlot;
import com.nowandfuture.mod.core.common.gui.slots.PrefabOnlySlot;
import com.nowandfuture.mod.core.common.gui.slots.TimelineOutputSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

public class ContainerAnmEditor extends Container {

    private TileEntityTimelineEditor tileEntityTimelineEditor;
    private PrefabOnlySlot slot;
    private TimelineOutputSlot outputSlot;
    private EntityPlayer player;

    public ContainerAnmEditor(InventoryPlayer playerInventory, TileEntityTimelineEditor tileEntityTimelineEditor){
        super();
        this.tileEntityTimelineEditor = tileEntityTimelineEditor;


        player = playerInventory.player;

        slot = new PrefabOnlySlot(player, tileEntityTimelineEditor,0, 199,57);
        outputSlot = new TimelineOutputSlot(tileEntityTimelineEditor,1,242,57);

        this.addListenableSlotToContainer(slot);
        this.addSlotToContainer(outputSlot);

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(playerInventory, 9 + j + i * 9, 102+j*18, 84+i*18));
            }
        }
        for (int i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(playerInventory, i, 102 + i * 18, 142));
        }
    }

    protected Slot addListenableSlotToContainer(ListenableSlot slot){
        return addSlotToContainer(setListenerForSlot(slot));
    }

    protected ListenableSlot setListenerForSlot(final ListenableSlot slot){
        slot.setChangeListener(new Consumer<ItemStack>() {
            @Override
            public void accept(ItemStack stack) {
                for (int j = 0; j < ContainerAnmEditor.this.listeners.size(); ++j)
                {
                    ContainerAnmEditor.this.listeners.get(j).
                            sendSlotContents(ContainerAnmEditor.this,
                                    slot.slotNumber, slot.getStack());

                }
            }
        });
        return slot;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tileEntityTimelineEditor.isUsableByPlayer(playerIn);
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
