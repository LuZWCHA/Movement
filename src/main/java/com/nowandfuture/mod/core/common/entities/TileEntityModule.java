package com.nowandfuture.mod.core.common.entities;

import com.nowandfuture.mod.api.IModule;
import com.nowandfuture.mod.core.movecontrol.ModuleBase;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.prefab.EmptyPrefab;
import com.nowandfuture.mod.core.transformers.AbstractTransformNode;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.handler.CollisionHandler;
import com.nowandfuture.mod.network.NetworkHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TileEntityModule extends TileEntityLockable implements IInventory,IModule,ITickable {

    protected final static int TIMELINE_UPDATE_PACKET = 0x11;
    protected final static int TIMELINE_MODIFY_PACKET = 0x12;
    protected final static int ENABLE_COLLISION_PACKET = 0x13;

    private final static String NBT_TICK = "Tick";
    private final static String NBT_ENABLE = "Enable";
    private final static String NBT_ENABLE_COLLISION = "EnableCollision";

    private final static int FORCE_UPDATE_TIME = 20;
    private int tick = 0;
    private boolean enableCollision = false;

    protected ModuleBase moduleBase;

    public TileEntityModule(){
        moduleBase = new ModuleBase();
        moduleBase.setModuleWorld(world);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return (Minecraft.getMinecraft().gameSettings.renderDistanceChunks *
                Minecraft.getMinecraft().gameSettings.renderDistanceChunks << 8);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        moduleBase.createDefaultTransformer();
        CollisionHandler.modules.add(this);

    }

    public void setModuleBase(@Nonnull ModuleBase moduleBase) {
        moduleBase.setModuleWorld(world);
        //fix pos
        moduleBase.setModulePos(getPos());
        this.moduleBase = moduleBase;
    }

    public ModuleBase getModuleBase() {
        return moduleBase;
    }

    public void setPrefab(@Nonnull AbstractPrefab prefab) {
        //fix pos
        if(prefab.isLocalWorldInit())
            prefab.setBaseLocation(getPos());
        moduleBase.setPrefab(prefab);
    }

    public AbstractPrefab getPrefab(){
        return moduleBase.getPrefab();
    }

    public void setEmptyPrefab(){
        moduleBase.setPrefab(new EmptyPrefab());
    }

    public void setModulePos(BlockPos posIn) {
        moduleBase.setModulePos(posIn);
    }

    public void setAuthor(String author) {
        moduleBase.setAuthor(author);
    }

    public void setName(String name) {
        moduleBase.setName(name);
    }

    public void enable() {
        moduleBase.enable();
    }

    public void disable() {
        moduleBase.disable();
    }

    public boolean isEnable() {
        return moduleBase.isEnable() && !isInvalid();
    }

    public BlockPos getModulePos() {
        return moduleBase.getModulePos();
    }

    public void setTransformNode(@Nonnull AbstractTransformNode part) {
        moduleBase.setTransformNode(part);
    }

    public void removePartIfExit() {
        moduleBase.removePartIfExit();
    }

    public AxisAlignedBB getMinAABB(){
        return moduleBase.getMinAABB();
    }

    public void update() {

        if(moduleBase.updateLine()){
            if(!world.isRemote){//sync with client every FORCE_UPDATE_TIME tick
                if(tick ++ % FORCE_UPDATE_TIME == 0) {
                    NetworkHandler.syncToTrackingClients(world, this,
                            getTimelineUpdatePacket(moduleBase.getLine().getTick(), moduleBase.getLine().isEnable()));
                }
            }
        }
        moduleBase.update();

    }

    @Override
    public void invalidate() {
        super.invalidate();
        if(world.isRemote)
            moduleBase.invalid();
//        if(world.isRemote)
        CollisionHandler.modules.remove(this);
    }

    @Override
    protected void setWorldCreate(World worldIn) {
        world = worldIn;
        moduleBase.setModuleWorld(worldIn);
    }

    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        moduleBase.setModuleWorld(worldIn);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        moduleBase.readFromNBT(compound);
        enableCollision = compound.getBoolean(NBT_ENABLE_COLLISION);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean(NBT_ENABLE_COLLISION,enableCollision);

        return moduleBase.writeToNBT(compound);
    }

    public void readModuleFromNBT(NBTTagCompound compound) {
        moduleBase.readModuleFromNBT(compound);
    }

    public NBTTagCompound writeModuleToNBT(NBTTagCompound compound) {
        return moduleBase.writeModuleToNBT(compound);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        return writeToNBT(compound);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(),1,getUpdateTag());
    }

    public SPacketUpdateTileEntity getTimelineUpdatePacket(long tick,boolean enable){
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setLong(NBT_TICK,tick);
        nbtTagCompound.setBoolean(NBT_ENABLE,enable);

        return new SPacketUpdateTileEntity(getPos(),TIMELINE_UPDATE_PACKET,nbtTagCompound);
    }

    public SPacketUpdateTileEntity getTimelineModifyPacket(){
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        getLine().serializeNBT(nbtTagCompound);

        return new SPacketUpdateTileEntity(getPos(),TIMELINE_MODIFY_PACKET,nbtTagCompound);
    }

    public SPacketUpdateTileEntity getCollisionEnablePacket(){
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setBoolean(NBT_ENABLE_COLLISION,enableCollision);

        return new SPacketUpdateTileEntity(getPos(),ENABLE_COLLISION_PACKET,nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound nbtGet = pkt.getNbtCompound();
        if(pkt.getTileEntityType() == 1) {
            this.readFromNBT(nbtGet);
        }else if(pkt.getTileEntityType() == TIMELINE_UPDATE_PACKET){
            getLine().setEnable(nbtGet.getBoolean(NBT_ENABLE));
            getLine().update(nbtGet.getLong(NBT_TICK));
        }else if(pkt.getTileEntityType() == TIMELINE_MODIFY_PACKET){
            getModuleBase().getLine().deserializeNBT(nbtGet);
        }else if(pkt.getTileEntityType() == ENABLE_COLLISION_PACKET){
            enableCollision = nbtGet.getBoolean(NBT_ENABLE_COLLISION);
        }
    }

    public KeyFrameLine getLine() {
        return moduleBase.getLine();
    }

    @Override
    public int getSizeInventory() {
        return getStacks().size();
    }

    @Override
    public boolean isEmpty() {
        return getStacks().isEmpty();
    }

    public List<ItemStack> getStacks(){
        return new ArrayList<>();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return getStacks().get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(getStacks(), index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(getStacks(), index);
    }

    public void setStackForSlot(int index, ItemStack stack){
        getStacks().set(index, stack);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack itemstack = getStackInSlot(index);
        boolean flag = !stack.isEmpty() &&
                stack.isItemEqual(itemstack) &&
                ItemStack.areItemStackTagsEqual(stack, itemstack);
        setStackForSlot(index, stack);

        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        //not same
        if (!flag)
        {
            slotChanged(index, stack);
            this.markDirty();
        }
    }

    protected void slotChanged(int index, ItemStack stack){

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
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

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
        return null;
    }

    @Override
    public String getGuiID() {
        return null;
    }

    public boolean isEnableCollision() {
        return enableCollision;
    }

    public void setEnableCollision(boolean enableCollision) {
        this.enableCollision = enableCollision;
    }
}
