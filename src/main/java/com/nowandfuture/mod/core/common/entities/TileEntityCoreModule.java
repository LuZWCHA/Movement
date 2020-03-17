package com.nowandfuture.mod.core.common.entities;

import com.nowandfuture.mod.core.common.gui.ContainerModule;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import com.nowandfuture.mod.core.common.gui.mygui.DynamicInventory;
import com.nowandfuture.mod.core.common.gui.mygui.api.IDynInventoryHolder;
import com.nowandfuture.mod.core.common.gui.mygui.api.IDynamicInventory;
import com.nowandfuture.mod.core.common.gui.mygui.api.IInventorySlotChangedListener;
import com.nowandfuture.mod.core.common.gui.mygui.api.SerializeWrapper;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.prefab.AnchorList;
import com.nowandfuture.mod.core.prefab.NormalPrefab;
import com.nowandfuture.mod.handler.RegisterHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class TileEntityCoreModule extends TileEntityModule implements IDynInventoryHolder<DynamicInventory, SerializeWrapper.BlockPosWrap>, IInventorySlotChangedListener {

    private NonNullList<ItemStack> moduleItemStacks =
            NonNullList.withSize(2, ItemStack.EMPTY);

    private DynamicInventory dynamicInventory = new DynamicInventory();

    public final static int BLOCK_VISIBLE_PACKET = 0x14;
    public final static int RENDER_OFFSET_PACKET = 0x15;
    public final static int INVENTORY_CHANGED_PACKET = 0x16;

    public final static String NBT_SHOW_BLOCK = "ShowBlock";
    public final static String NBT_OFFSET_X = "OffsetX";
    public final static String NBT_OFFSET_Y = "OffsetY";
    public final static String NBT_OFFSET_Z = "OffsetZ";

    //not finished
    private BlockPos offset = new BlockPos(0,0,0);

    private boolean showBlock = true;

    AnchorList anchorList;

    public TileEntityCoreModule(){
        super();
        dynamicInventory.setCreator(new IDynamicInventory.SlotCreator() {
            @Override
            public AbstractContainer.ProxySlot create(IDynamicInventory inventory, long index, int type) {
                if(type == 0) {
                    return new AbstractContainer.ProxySlot(inventory, (int) index,type) {
                        @Override
                        public boolean isItemValid(ItemStack stack) {
                            return stack.getItem() == RegisterHandler.prefabItem;
                        }
                    };
                } else {
                    return new AbstractContainer.ProxySlot(inventory, (int) index,type) {
                        @Override
                        public boolean isItemValid(ItemStack stack) {
                            return stack.getItem() == RegisterHandler.timelineItem;
                        }
                    };
                }
            }
        });

        dynamicInventory.addInventoryChangeListener(this);
    }

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
        //if client receive nbt from server,these changes should not back to server
        //else server should send the changes to its clients
        dynamicInventory.readFromNBT(compound,world != null && !world.isRemote);
        readNBT(compound);
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        ItemStackHelper.saveAllItems(compound, moduleItemStacks);
        dynamicInventory.writeToNBT(compound);
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

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return (oldState.getBlock() != newSate.getBlock());
    }

    public SPacketUpdateTileEntity getShowBlockPacket(){
        return new SPacketUpdateTileEntity(getPos(),BLOCK_VISIBLE_PACKET,writeToNBT(new NBTTagCompound()));
    }

    public NBTTagCompound getInventoryTag(){
        return dynamicInventory.writeToNBT(new NBTTagCompound());
    }

    public void handleInventoryTag(NBTTagCompound nbtTagCompound){
        dynamicInventory.readFromNBT(nbtTagCompound,false);
    }

    public SPacketUpdateTileEntity getInventoryPacket(){
        return new SPacketUpdateTileEntity(getPos(),BLOCK_VISIBLE_PACKET,dynamicInventory.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        if(pkt.getTileEntityType() == BLOCK_VISIBLE_PACKET){
            NBTTagCompound nbtTagCompound = pkt.getNbtCompound();
            readNBT(nbtTagCompound);
        }else if(pkt.getTileEntityType() == INVENTORY_CHANGED_PACKET){
            NBTTagCompound nbtTagCompound = pkt.getNbtCompound();
            handleInventoryTag(nbtTagCompound);
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
                AbstractPrefab prefab = new NormalPrefab();
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

    @Nonnull
    @Override
    public DynamicInventory getDynInventory() {
        return dynamicInventory;
    }

    @Override
    public SerializeWrapper.BlockPosWrap getHolderId() {
        return new SerializeWrapper.BlockPosWrap(getPos());
    }


    @Override
    public void slotRemoved(long id, ItemStack itemStack,boolean forced) {
        if(forced){
            if(world != null && world.isRemote)
                dynamicInventory.sync(this);

            if(world != null && !world.isRemote && !itemStack.isEmpty()) {
                IDynamicInventory.spawnItemStack(world, getPos().getX(),
                        getPos().getY(), getPos().getZ(), itemStack.copy());
            }
        }
    }

    @Override
    public void slotAdded(long id, ItemStack itemStack,boolean forced) {
        if(forced) {
            if (world != null && world.isRemote)
                dynamicInventory.sync(this);
        }
    }

    @Override
    public void onInventoryChanged(IInventory invBasic) {

    }
}
