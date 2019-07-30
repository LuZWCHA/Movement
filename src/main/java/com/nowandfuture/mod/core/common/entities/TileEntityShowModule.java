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
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class TileEntityShowModule extends TileEntityModule {

    private NonNullList<ItemStack> moduleItemStacks =
            NonNullList.withSize(2, ItemStack.EMPTY);

    public final static int BLOCK_VISIBLE_PACKET = 0x14;
    public final static int RENDER_OFFSET_PACKET = 0x15;

    public final static String NBT_SHOW_BLOCK = "ShowBlock";
    public final static String NBT_OFFSET_X = "OffsetX";
    public final static String NBT_OFFSET_Y = "OffsetY";
    public final static String NBT_OFFSET_Z = "OffsetZ";

    //not finished
    private BlockPos offset = new BlockPos(0,0,0);

    private boolean showBlock = true;

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
        readNBT(compound);
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        ItemStackHelper.saveAllItems(compound, moduleItemStacks);
        writeNBT(compound);
        return super.writeToNBT(compound);
    }

    public NBTTagCompound writeNBT(NBTTagCompound compound){
        compound.setBoolean(NBT_SHOW_BLOCK,showBlock);
        compound.setInteger(NBT_OFFSET_X,offset.getX());
        compound.setInteger(NBT_OFFSET_Y,offset.getY());
        compound.setInteger(NBT_OFFSET_Z,offset.getZ());

        return compound;
    }

    public void readNBT(NBTTagCompound compound){
        showBlock = compound.getBoolean(NBT_SHOW_BLOCK);
        int x = compound.getInteger(NBT_OFFSET_X);
        int y = compound.getInteger(NBT_OFFSET_Y);
        int z = compound.getInteger(NBT_OFFSET_Z);
        offset = new BlockPos(x,y,z);
    }

    public SPacketUpdateTileEntity getShowBlockPacket(){
        return new SPacketUpdateTileEntity(getPos(),BLOCK_VISIBLE_PACKET,writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        if(pkt.getTileEntityType() == BLOCK_VISIBLE_PACKET){
            NBTTagCompound nbtTagCompound = pkt.getNbtCompound();
            readNBT(nbtTagCompound);
        }
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


    public boolean isShowBlock() {
        return showBlock;
    }

    public void setShowBlock(boolean showBlock) {
        this.showBlock = showBlock;
    }
}
