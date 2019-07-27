package com.nowandfuture.mod.core.common.gui.mygui.slots;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.Items.PrefabItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

public class ListenableSlot extends Slot {

    private final EntityPlayer player;
    private Consumer<ItemStack> changeListener;

    public ListenableSlot(EntityPlayer player, IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
        this.player = player;
    }

    @Override
    public void putStack(ItemStack stack) {
        super.putStack(stack);
//        Movement.logger.info("putStack");

    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
//        Movement.logger.info("onSlotChanged");
    }

    @Override
    public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
        super.onSlotChange(p_75220_1_, p_75220_2_);
//        Movement.logger.info("onSlotChange");

//        if(changeListener != null){
//            changeListener.accept(this.getStack());
//        }
    }

    @Override
    protected void onCrafting(ItemStack stack) {
        super.onCrafting(stack);
    }

    @Override
    protected void onCrafting(ItemStack stack, int amount) {
        super.onCrafting(stack, amount);
//        Movement.logger.info("onCrafting");

    }


    @Override
    public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
//        Movement.logger.info("onTake");
        return super.onTake(thePlayer, stack);

    }

    @Override
    protected void onSwapCraft(int p_190900_1_) {
//        Movement.logger.info("onSwapCraft");
        super.onSwapCraft(p_190900_1_);
    }

    public void setChangeListener(Consumer<ItemStack> changeListener) {
        this.changeListener = changeListener;
    }
}
