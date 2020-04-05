package com.nowandfuture.mod.core.movecontrol;

import com.nowandfuture.mod.core.common.Items.PrefabItem;
import com.nowandfuture.mod.core.common.Items.TimelineItem;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import com.nowandfuture.mod.core.common.gui.mygui.DynamicInventory;
import com.nowandfuture.mod.core.common.gui.mygui.api.IDynInventoryHolder;
import com.nowandfuture.mod.core.common.gui.mygui.api.IDynamicInventory;
import com.nowandfuture.mod.core.common.gui.mygui.api.IInventorySlotChangedListener;
import com.nowandfuture.mod.core.common.gui.mygui.api.SerializeWrapper;
import com.nowandfuture.mod.core.prefab.ModuleNodeMap;
import com.nowandfuture.mod.core.prefab.ModuleUtils;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ModuleNode extends TileEntityModule implements IDynInventoryHolder<DynamicInventory, SerializeWrapper.BlockPosWrap>, IInventorySlotChangedListener, ModuleNodeMap.ModuleMapChangedListener {
    public final static String NBT_OFFSET_X = "OffsetX";
    public final static String NBT_OFFSET_Y = "OffsetY";
    public final static String NBT_OFFSET_Z = "OffsetZ";
    public final static int INVENTORY_CHANGED_PACKET = 0x16;
    public final static int OFFSET_PACKET = 0x15;

    private ModuleNode parent;
    private int depth;
    protected BlockPos offset = new BlockPos(0,0,0);
    private long prefabId;
    private long timelineId;
    protected DynamicInventory dynamicInventory = new DynamicInventory();
    private ModuleNodeMap map;
    private static String NBT_PREFAB_ID = "PrefabId";
    private static String NBT_TIMELINE_ID = "TimelineId";

    //temp value
    private Matrix4f matrix4f;

    public ModuleNode(){
        super();
        map = new ModuleNodeMap();
        matrix4f = new Matrix4f();
        dynamicInventory.setCreator(new IDynamicInventory.SlotCreator() {
            @Override
            public AbstractContainer.ProxySlot create(IDynamicInventory inventory, long index, int type) {
                if(type == 0) {
                    return new PrefabSlot(inventory, (int) index,type);
                } else {
                    return new TimelineSlot(inventory, (int) index,type);
                }
            }
        });
        dynamicInventory.addInventoryChangeListener(this);
        map.addListener(this);
    }

    public long getPrefabId() {
        return prefabId;
    }

    public void setPrefabId(long prefabId) {
        this.prefabId = prefabId;
    }

    public long getTimelineId() {
        return timelineId;
    }

    public void setTimelineId(long timelineId) {
        this.timelineId = timelineId;
    }

    @Override
    protected void setWorldCreate(World worldIn) {
        super.setWorldCreate(worldIn);
        for (ModuleNode node:
                map.getModules()) {
            node.setWorldCreate(worldIn);
        }
    }

    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        for (ModuleNode node:
                map.getModules()) {
            node.setWorld(worldIn);
        }
    }

    @Override
    public BlockPos getPos() {
        if(parent != null) return parent.getPos();
        return super.getPos();
    }

    @Override
    public void doTransform(double p, Matrix4f parentMatrix) {

        Matrix4f matrix = new Matrix4f();
        super.doTransform(p, matrix);
        //copy result
        Matrix4f.mul(parentMatrix.translate(new Vector3f(offset)),matrix,this.matrix4f);

        if(isPrefabRenderEnable())
            for (ModuleNode node:
                    map.getModules()) {
                node.doTransform(p, new Matrix4f(this.matrix4f));
                node.setModulePos(getModulePos());
            }
    }

    protected boolean isPrefabRenderEnable(){
        if(getPrefab() != null && getPrefab().isLocalWorldInit()){
            return getPrefab().isReady();
        }
        return false;
    }

    public void setTick(long tick){
        getLine().setTick(tick);
        for (ModuleNode node:
                map.getModules()) {
            node.setTick(tick);
        }
    }

    public void setTimelineEnable(boolean enable){
        getLine().setEnable(enable);
        for (ModuleNode node:
                map.getModules()) {
            node.getLine().setEnable(enable);
        }
    }

    @Override
    public void update() {
        moduleBase.updateLine();
        moduleBase.update();

        for (ModuleNode node:
                map.getModules()) {
            node.getLine().setEnable(getLine().isEnable());
            node.update();
        }
    }

    @Override
    public void invalidate() {
        dynamicInventory.removeInventoryChangeListener(this);
        map.removeListener(this);

        if(world!= null && !world.isRemote)
            IDynamicInventory.dropInventoryItems(world,getPos(),dynamicInventory);

        for (ModuleNode node:
                map.getModules()) {
            node.invalidate();
        }

        super.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        for (ModuleNode node:
                map.getModules()) {
            node.onLoad();
        }
    }

    @Override
    public void validate() {
        super.validate();

        for (ModuleNode node:
                map.getModules()) {
            node.validate();
        }
    }

    @Override
    public void disable() {
        super.disable();
        for (ModuleNode node:
                map.getModules()) {
            node.disable();
        }
    }

    @Override
    public void enable() {
        super.enable();
        for (ModuleNode node:
                map.getModules()) {
            node.enable();
        }
    }

    @Override
    public void setEnableCollision(boolean enableCollision) {
        super.setEnableCollision(enableCollision);
        for (ModuleNode node:
                map.getModules()) {
            node.setEnableCollision(enableCollision);
        }
    }

    public void setOffset(BlockPos offset) {
        this.offset = offset;
    }

    public BlockPos getOffset() {
        return offset;
    }

    public void handleInventoryTag(NBTTagCompound nbtTagCompound){
        dynamicInventory.readFromNBT(nbtTagCompound,false);
    }

    public SPacketUpdateTileEntity getInventoryPacket(){
        return new SPacketUpdateTileEntity(getPos(),INVENTORY_CHANGED_PACKET,getInventoryTag());
    }

    public NBTTagCompound getInventoryTag(){
        return dynamicInventory.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound nbtGet = pkt.getNbtCompound();
        if(pkt.getTileEntityType() == 1) {
            this.readFromNBT(nbtGet);
        }else if(pkt.getTileEntityType() == TIMELINE_UPDATE_PACKET){
            this.setTimelineEnable(nbtGet.getBoolean(NBT_ENABLE));
            this.setTick(nbtGet.getLong(NBT_TICK));
        }else if(pkt.getTileEntityType() == TIMELINE_MODIFY_PACKET){
            this.getLine().deserializeNBT(nbtGet);
        }else if(pkt.getTileEntityType() == ENABLE_COLLISION_PACKET){
            this.setEnableCollision(nbtGet.getBoolean(NBT_ENABLE_COLLISION));
        }else if(pkt.getTileEntityType() == INVENTORY_CHANGED_PACKET){
            this.handleInventoryTag(nbtGet);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setLong(NBT_PREFAB_ID, prefabId);
        compound.setLong(NBT_TIMELINE_ID,timelineId);
        compound.setInteger(NBT_OFFSET_X,offset.getX());
        compound.setInteger(NBT_OFFSET_Y,offset.getY());
        compound.setInteger(NBT_OFFSET_Z,offset.getZ());
        writeModuleToNBT(compound);
        dynamicInventory.writeToNBT(compound);

        return map.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        dynamicInventory.readFromNBT(compound,false);

        prefabId = compound.getLong(NBT_PREFAB_ID);
        timelineId = compound.getLong(NBT_TIMELINE_ID);
        int x = compound.getInteger(NBT_OFFSET_X);
        int y = compound.getInteger(NBT_OFFSET_Y);
        int z = compound.getInteger(NBT_OFFSET_Z);
        offset = new BlockPos(x,y,z);
        this.readModuleFromNBT(compound);

        if(world == null) world = parent.getWorld();
        map.readFromNBT(compound,world);
    }

    @Override
    public void slotRemoved(long id, ItemStack itemStack, boolean forced) {
        if(!world.isRemote && !itemStack.isEmpty()){
            IDynamicInventory.spawnItemStack(world,getPos().getX(),getPos().getY(),getPos().getZ(),itemStack);
        }
    }

    @Override
    public void slotAdded(long id, ItemStack itemStack,boolean forced) {
    }

    @Override
    public void onSlotContentChanged(long id, ItemStack stack) {
        int type = getSlotType(id);
        if(type < 0) return;

        ModuleNode node;
        if(stack.isEmpty()){//take out
            if(type == 0){
                node = map.prefabToModule(id);
                ModuleUtils.removePrefab(node);
            }else{
                node = map.timelineToModule(id);
                ModuleUtils.removeTimeline(node);
            }
        }else{//put in
            if(type == 0){
                node = map.prefabToModule(id);
                if(node != null) {
                    ModuleUtils.setPrefab(world, node, stack);
                }
            }else{
                node = map.timelineToModule(id);
                if(node != null) {
                    ModuleUtils.setTimeline(world, node, stack);
                }
            }
        }
    }

    private int getSlotType(long id){
        int type;
        AbstractContainer.ProxySlot slot = dynamicInventory.getSlots().get(id);
        if(slot == null) return -1;
        else{
            type = slot.getType();
        }
        return type;
    }

    @Override
    public void onInventoryChanged(IInventory invBasic) {

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
    public String getInventoryId() {
        return getId();
    }

    @Override
    public NBTTagCompound getFullUpdateTag() {
        return getUpdateTag();
    }

    @Override
    public void onModuleAdded(ModuleNode moduleNode) {
        moduleNode.setDepth(getDepth() + 1);
        moduleNode.setParent(this);
    }

    @Override
    public void onModuleRemoved(int index, ModuleNode moduleNode) {
        moduleNode.invalidate();
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
        for (ModuleNode node:
                map.getModules()) {
            node.setDepth(depth + 1);
        }
    }

    public ModuleNode getParent() {
        return parent;
    }

    public void setParent(ModuleNode parent) {
        this.parent = parent;
    }

    public Matrix4f getMatrix4f() {
        return matrix4f;
    }

    public static class PrefabSlot extends AbstractContainer.ProxySlot{

        public PrefabSlot(IDynamicInventory inventoryIn, int index, int type) {
            super(inventoryIn, index, type);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack.getItem() instanceof PrefabItem;
        }
    }

    public static class TimelineSlot extends AbstractContainer.ProxySlot{

        public TimelineSlot(IDynamicInventory inventoryIn, int index, int type) {
            super(inventoryIn, index, type);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack.getItem() instanceof TimelineItem;
        }
    }

    public ModuleNodeMap getModuleMap() {
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleNode node = (ModuleNode) o;
        return prefabId == node.prefabId &&
                timelineId == node.timelineId;
    }

   public String getId(){
        if(parent != null){
            return parent.getId() + "," + offset.toLong();
        }
        return String.valueOf(offset.toLong());
   }

   public static List<Long> decodeId(String id){
       String[] pathList = id.split(",");

       List<Long> posList = new ArrayList<>();
       for (String path :
               pathList) {
           posList.add(Long.parseLong(path));
       }

       return posList;
   }

}
