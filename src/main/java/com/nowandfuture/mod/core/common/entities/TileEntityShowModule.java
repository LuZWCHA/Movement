package com.nowandfuture.mod.core.common.entities;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.gui.ContainerModule;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.prefab.EmptyPrefab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class TileEntityShowModule extends TileEntityModule {

    private NonNullList<ItemStack> moduleItemStacks =
            NonNullList.withSize(2, ItemStack.EMPTY);

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return super.getRenderBoundingBox();
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return super.getMaxRenderDistanceSquared();
    }

    @Override
    public List<ItemStack> getStacks() {
        return moduleItemStacks;
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
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    protected void slotChanged(int index, ItemStack stack) {
        if(index == 0){//prefab changed
            if(getStackInSlot(index).isEmpty()){
                this.setEmptyPrefab();
            }else{
                AbstractPrefab prefab = new EmptyPrefab();
                NBTTagCompound compound = getStackInSlot(0).getTagCompound();
                if(compound != null) {
                    prefab.readFromNBT(compound,world);
                }
                setPrefab(prefab);
                getModuleBase().enable();
            }
        }else if(index == 1){//timeline changed
            if(getStackInSlot(index).isEmpty()){
                this.getLine().reset();
                this.getLine().resetTick();
            }else{
                NBTTagCompound compound = getStackInSlot(1).getTagCompound();
                if(compound != null) {
                    this.getLine().deserializeNBT(compound);
                }
            }
        }
    }

    public void offsetPrefab(int x,int y,int z){
        getModuleBase().getPrefab().setBaseLocation(getModuleBase().getModulePos().add(x,y,z));
    }

    @Override
    public void clear() {
        getStacks().clear();
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerModule(playerInventory,this);
    }

    @Override
    public String getGuiID() {
        return "module_gui";
    }


}
