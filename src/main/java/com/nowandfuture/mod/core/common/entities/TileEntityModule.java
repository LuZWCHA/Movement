package com.nowandfuture.mod.core.common.entities;

import com.nowandfuture.mod.api.IModule;
import com.nowandfuture.mod.core.movecontrol.ModuleBase;
import com.nowandfuture.mod.core.movecontrol.ModuleNode;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.core.transformers.AbstractTransformNode;
import com.nowandfuture.mod.core.transformers.animation.Timeline;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TileEntityModule extends TileEntityLockable implements IInventory,IModule{

    protected final static int TIMELINE_UPDATE_PACKET = 0x11;
    protected final static int TIMELINE_MODIFY_PACKET = 0x12;
    protected final static int ENABLE_COLLISION_PACKET = 0x13;

    public final static String NBT_TICK = "Tick";
    public final static String NBT_ENABLE = "Enable";
    public final static String NBT_ENABLE_COLLISION = "EnableCollision";
    private final static String NBT_RENDER_REALTIME = "RenderRealtime";
    private final static String NBT_NODE_TYPES = "NodeTypes";

    private final static int FORCE_UPDATE_TIME = 20;
    private int tick = 0;
    protected boolean enableCollision = false;
    //render light realtime
    //0:disable
    //>0:re-render with a probability of 1/(1+renderRealtime)
    private int renderRealtime = -1;

    protected ModuleBase moduleBase;

    //----------------------------------------CLIENT_DEBUG------------------------------------
    private Vector3f impactAxis;
    private OBBox renderBox;

    public TileEntityModule(){
        moduleBase = new ModuleBase();
        moduleBase.setModuleWorld(world);
        moduleBase.createDefaultTransformer();
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
    }

    public void setModuleBase(@Nonnull ModuleBase newModuleBase) {
        newModuleBase.setModuleWorld(world);
        //fix pos
        newModuleBase.setModulePos(getPos());
        moduleBase = newModuleBase;
        if(moduleBase.getTransformerHead() == null)
            moduleBase.createDefaultTransformer();
    }

    public void setPrefab(@Nonnull AbstractPrefab prefab) {
        prefab.setBaseLocation(getPos());
        moduleBase.setPrefab(prefab);
    }

    public AbstractPrefab getPrefab(){
        return moduleBase.getPrefab();
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

    @Override
    public void doTransform(double p, Matrix4f matrix4f) {
        moduleBase.doTransform(p, matrix4f);
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

    @Override
    public void update() {
        if(moduleBase.updateLine()){
            syncToClients();
        }
        moduleBase.update();
    }

    protected void syncToClients(){
        if(!world.isRemote){//sync with client every FORCE_UPDATE_TIME tick
            if(tick ++ % FORCE_UPDATE_TIME == 0) {
                NetworkHandler.syncToTrackingClients(world, this,
                        getTimelineUpdatePacket(getLine().getTick(), getLine().isEnable()));
            }
        }
    }

    @Override
    public void invalidate() {
        if(world != null && world.isRemote)
            moduleBase.clearGLResource();
        super.invalidate();
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
        //moduleNode is not a tileEntity in mc world
        if(getClass() != ModuleNode.class)
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

    // TODO: 2020/2/27
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
            getLine().setTick(nbtGet.getLong(NBT_TICK));
        }else if(pkt.getTileEntityType() == TIMELINE_MODIFY_PACKET){
            getLine().deserializeNBT(nbtGet);
        }else if(pkt.getTileEntityType() == ENABLE_COLLISION_PACKET){
            enableCollision = nbtGet.getBoolean(NBT_ENABLE_COLLISION);
        }
    }

    public Timeline getLine() {
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

    public Vector3f getImpactAxis() {
        return impactAxis;
    }

    public void setImpactAxis(Vector3f impactAxis) {
        this.impactAxis = impactAxis;
    }

    @SideOnly(Side.CLIENT)
    public OBBox getRenderBox() {
        return renderBox;
    }

    @SideOnly(Side.CLIENT)
    public void setRenderBox(OBBox renderBox) {
        this.renderBox = renderBox;
    }

    @SideOnly(Side.CLIENT)
    public int getRenderRealtime() {
        return renderRealtime;
    }

    @SideOnly(Side.CLIENT)
    public void setRenderRealtime(int renderRealtime) {
        this.renderRealtime = renderRealtime;
    }
}
