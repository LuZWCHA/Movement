package com.nowandfuture.mod.core.common.entities;

import com.nowandfuture.mod.core.common.Items.PrefabItem;
import com.nowandfuture.mod.core.common.gui.ContainerAnmEditor;
import com.nowandfuture.mod.core.common.gui.mygui.api.IChangeListener;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.common.modulebase.ModuleUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import java.util.List;

public class TileEntityTimelineEditor extends TileEntityModule {
    private IChangeListener slotChanged;

    private NonNullList<ItemStack> moduleItemStacks =
            NonNullList.withSize(2, ItemStack.EMPTY);

    public TileEntityTimelineEditor(){
        super();
    }

    @Override
    public void setPrefab(@Nonnull AbstractPrefab prefab) {
        prefab.setBaseLocation(getPos().add(1,0,0));
        moduleBase.setPrefab(prefab);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        ItemStackHelper.loadAllItems(compound, moduleItemStacks);
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        ItemStackHelper.saveAllItems(compound, moduleItemStacks);
        return super.writeToNBT(compound);
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : getStacks()) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<ItemStack> getStacks() {
        return moduleItemStacks;
    }

    @Override
    protected void slotChanged(int index, ItemStack stack) {
        if(index == 0) {
            if(stack.isEmpty() || stack.getTagCompound() == null){
                ModuleUtils.removePrefab(this);
            }else{
                if(!getPrefab().isLocalWorldInit()) {
//                    NormalPrefab prefab = new NormalPrefab();
//                    NBTTagCompound tagCompound = stack.getTagCompound();
//                    prefab.readFromNBT(tagCompound, getWorld());
//                    setPrefab(prefab);
//                    enable();
                }
                ModuleUtils.setPrefab(world,this,stack);

            }
        }

        if(slotChanged != null)
            slotChanged.changed(index);
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 1 && stack.getItem() instanceof PrefabItem;
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        getStacks().clear();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerAnmEditor(playerInventory,this);
    }

    @Override
    public String getGuiID() {
        return "module_gui";
    }

    public void setSlotChanged(IChangeListener.IChangeEvent slotChanged) {
        this.slotChanged = slotChanged;
    }
}
