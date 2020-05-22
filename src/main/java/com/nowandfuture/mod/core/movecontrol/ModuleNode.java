package com.nowandfuture.mod.core.movecontrol;

import com.google.common.base.Objects;
import com.nowandfuture.mod.api.Unstable;
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
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ModuleNode extends TileEntityModule implements IDynInventoryHolder<DynamicInventory, SerializeWrapper.BlockPosWrap>, IInventorySlotChangedListener, ModuleNodeMap.ModuleMapChangedListener {
    private final static String NBT_OFFSET_X = "OffsetX";
    private final static String NBT_OFFSET_Y = "OffsetY";
    private final static String NBT_OFFSET_Z = "OffsetZ";
    private final static int INVENTORY_CHANGED_PACKET = 0x16;
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

    public ModuleNode(){
        super();
        map = new ModuleNodeMap();
//        matrix4f = new Matrix4f();
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
        Matrix4f.mul(parentMatrix.translate(new Vector3f(offset)),matrix,getMatrix4f());

        if(isPrefabRenderEnable())
            for (ModuleNode node:
                    map.getModules()) {
                node.doTransform(p, new Matrix4f(getMatrix4f()));
                node.setModulePos(getModulePos());
            }

    }

    protected boolean isPrefabRenderEnable(){
        if(getPrefab() != null && getPrefab().isLocalWorldInit()){
            return getPrefab().isReady();
        }
        return false;
    }

    public void driveLine(long tick){
        getLine().driveLine(tick);

        for (ModuleNode node:
                map.getModules()) {
            node.getLine().setEnable(getLine().isEnable());

            node.driveLine(tick);
        }
    }

    public void debugTraversal(Consumer<ModuleNode> consumer){

        consumer.accept(this);

        for (ModuleNode node:
                map.getModules()) {
            node.debugTraversal(consumer);
        }
    }

//    public void setChildrenStep(boolean positive){
//        System.out.println(getLine().getStep());
//
//        for (ModuleNode node:
//                map.getModules()) {
//            if(!positive)
//                node.getLine().reverse();
//            node.setChildrenStep(node.getLine().getStep() > 0);
//        }
//    }

    public void setTimelineEnable(boolean enable){
        getLine().setEnable(enable);
        for (ModuleNode node:
                map.getModules()) {
            node.setTimelineEnable(enable);
        }
    }

    @Override
    public void update() {
//        moduleBase.updateLine();
//        moduleBase.update();
        moduleBase.updateEntities();

        for (ModuleNode node:
                map.getModules()) {
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
            this.driveLine(nbtGet.getLong(NBT_TICK));
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
        return moduleBase.getTransMatrix();
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

    @Unstable(description = "may cause list bug")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleNode node = (ModuleNode) o;
        return offset.equals(node.offset) &&
                parent == node.parent;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(parent, offset);
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

   public void collectOBBoxs(@Nonnull List<OBBox> list){
       AxisAlignedBB aabb = getMinAABB();

       if(aabb != null) {
           OBBox obBox = new OBBox(aabb);
           Matrix4f matrix4f = getMatrix4f();
           obBox.mulMatrix(matrix4f);

           obBox.translate(getModulePos());
           list.add(obBox);
       }

       for (ModuleNode node:
               map.getModules()) {
           node.collectOBBoxs(list);
       }
   }

   @Unstable
   public void collectAABBs(@Nonnull List<AxisAlignedBB> list,AxisAlignedBB area){
       AxisAlignedBB aabb = getMinAABB();
       if(aabb != Block.NULL_AABB) {

           Matrix4f invertMatrix = Matrix4f.invert(getMatrix4f(), new Matrix4f());
           List<AxisAlignedBB> moduleAABBs = new LinkedList<>();
           AxisAlignedBB laabb = area.offset(-getModulePos().getX(), -getModulePos().getY(), -getModulePos().getZ());
           OBBox transOBB = new OBBox(laabb).transform(invertMatrix);

           //if transformed AABB is still a AABB
           if (transOBB.isAxisAlignedBB()) {
               AxisAlignedBB transAABB = transOBB.asAxisAlignedBB();
               if (aabb.intersects(transAABB)) {
                   AxisAlignedBB intersectArea = aabb.intersect(transAABB);
                   moduleBase.collectAABBsWithin(moduleAABBs, intersectArea);
                   for (AxisAlignedBB module :
                           moduleAABBs) {
                       OBBox obBox = new OBBox(module).transform(getMatrix4f());
//                   if(obBox.isAxisAlignedBB()) {
                       AxisAlignedBB temp = obBox.asAxisAlignedBB();
                       if (temp.intersects(laabb))
                           list.add(temp.offset(getModulePos()));
//                   }
                   }

               }
           }
       }

       for (ModuleNode node:
               map.getModules()) {
           node.collectAABBs(list,area);
       }
   }

}
