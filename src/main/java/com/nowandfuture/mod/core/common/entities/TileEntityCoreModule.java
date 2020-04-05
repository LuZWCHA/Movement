package com.nowandfuture.mod.core.common.entities;

import com.nowandfuture.mod.core.common.gui.ContainerModule;
import com.nowandfuture.mod.core.common.gui.mygui.DynamicInventory;
import com.nowandfuture.mod.core.common.gui.mygui.api.IDynamicInventory;
import com.nowandfuture.mod.core.movecontrol.ModuleNode;
import com.nowandfuture.mod.core.prefab.ModuleUtils;
import com.nowandfuture.mod.utils.math.Matrix4f;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.*;

public class TileEntityCoreModule extends ModuleNode {

    private NonNullList<ItemStack> moduleItemStacks =
            NonNullList.withSize(2, ItemStack.EMPTY);

    public final static String NBT_SHOW_BLOCK = "ShowBlock";
    public final static int BLOCK_VISIBLE_PACKET = 0x14;
    private final static int NODE_NUMBER_LIMIT = 16;

    private boolean showBlock = true;

    private Stack<ModuleNode> nodeStack;

    public TileEntityCoreModule(){
        super();
        nodeStack = new Stack<>();
        nodeStack.push(this);
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
        readNBT(compound);
        super.readFromNBT(compound);
        restoreNodeStack(compound);

        setDepth(0);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        ItemStackHelper.saveAllItems(compound, moduleItemStacks);
        writeNBT(compound);
        super.writeToNBT(compound);

        return saveNodeStack(compound);
    }

    public NBTTagCompound writeNBT(NBTTagCompound compound){
        compound.setBoolean(NBT_SHOW_BLOCK,showBlock);
        return compound;
    }

    public void readNBT(NBTTagCompound compound){
        showBlock = compound.getBoolean(NBT_SHOW_BLOCK);
    }

    private final static String NBT_PATH = "Path";

    public NBTTagCompound saveNodeStack(NBTTagCompound compound){
        ModuleNode node = nodeStack.peek();
        compound.setString(NBT_PATH,node.getId());

        return compound;
    }

    //before restore NodeMap should be restored
    public void restoreNodeStack(NBTTagCompound compound){
        String pathString = compound.getString(NBT_PATH);
        Stack<ModuleNode> stack = new Stack<>();
        updateStackByPathString(pathString,stack);
        nodeStack = stack;
    }

    public boolean updateStackByPathString(String pathString,Stack<ModuleNode> newStack){
        if(newStack == null) newStack = new Stack<>();
        List<Long> pathList = decodeId(pathString);
        ModuleNode tempNode = this;
        newStack.push(tempNode);

        if(pathList.size() > 1)
            for (int i = 1; i < pathList.size();i++) {
                BlockPos pos = BlockPos.fromLong(pathList.get(i));
                tempNode = tempNode.getModuleMap().get(pos);

                if(tempNode == null) {
                    return false;
                }
                newStack.push(tempNode);
            }
        return true;
    }

    public ModuleNode getNodeByPathString(String pathString){
        Stack<ModuleNode> newStack = new Stack<>();
        updateStackByPathString(pathString,newStack);
        return newStack.peek();
    }

    @Override
    public void update() {
        boolean isUpdate = moduleBase.updateLine();
        moduleBase.update();
        if(isUpdate){
            setTick(getLine().getTick());
            syncToClients();
        }
    }

    public Stack<ModuleNode> getNodeStack() {
        return nodeStack;
    }

    public void setNodeStack(Stack<ModuleNode> nodeStack) {
        this.nodeStack = nodeStack;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return (oldState.getBlock() != newSate.getBlock());
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

    public void handleRemoveNodeTag(NBTTagCompound nbtTagCompound){
        String nodeId = nbtTagCompound.getString("moduleId");
        int index = nbtTagCompound.getInteger("index");
        ModuleNode node = getCurModuleNode();
        if(node.getId().equals(nodeId)){
            removeModuleNode(index);
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
            if(stack.isEmpty()){
                ModuleUtils.removePrefab(this);
            }else{
                ModuleUtils.setPrefab(world,this,stack);
            }
        }else if(index == 1){//timeline changed
            if(stack.isEmpty()){
                ModuleUtils.removeTimeline(this);
            }else{
                ModuleUtils.setTimeline(world,this,stack);
            }
        }
    }

    public void offsetPrefab(int x, int y, int z){
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

    @Override
    public void doTransform(double p, Matrix4f parentMatrix) {
        super.doTransform(p, parentMatrix);
    }

    public void createModuleNode(BlockPos pos){
        ModuleNode node = getCurModuleNode();
        if(node.getModuleMap().getModules().size() < NODE_NUMBER_LIMIT && !node.getModuleMap().contains(pos)) {
            long pid = node.getDynInventory().createSlot(ItemStack.EMPTY,0,false);
            long tid = node.getDynInventory().createSlot(ItemStack.EMPTY,1,false);
            ModuleNode moduleNode = ModuleUtils.buildEmptyModule(world,pid,tid);
            moduleNode.setParent(node);
            // TODO: 2020/3/25 ...
            moduleNode.setModulePos(node.getModulePos());
            moduleNode.setOffset(pos);
            node.getModuleMap().addModule(moduleNode);
        }
    }

    @Override
    public NBTTagCompound getFullUpdateTag() {
        return getUpdateTag();
    }

    @Override
    public String getInventoryId() {
        return getCurModuleNode().getId();
    }

    public List<IDynamicInventory> collectAllDynInventories(){
        List<IDynamicInventory> list = new LinkedList<>();
        Queue<ModuleNode> moduleNodes = new ArrayDeque<>();
        moduleNodes.offer(this);

        while (!moduleNodes.isEmpty()){
            ModuleNode temp = moduleNodes.remove();
            if(temp != this)
                list.add(temp.getDynInventory());
            else
                list.add(dynamicInventory);

            moduleNodes.addAll(temp.getModuleMap().getModules());
        }

        return list;
    }

    @Nonnull
    @Override
    public DynamicInventory getDynInventory() {
        ModuleNode node = getCurModuleNode();
        if(node == this) return dynamicInventory;
        else return node.getDynInventory();
    }

    public void removeModuleNode(int index){
        ModuleNode node = getCurModuleNode();
        if(node.getModuleMap().size() > index && index > -1) {
            long id = node.getDynInventory()
                    .getEntryByIndex(2 * index)
                    .getKey();

            node.getDynInventory().removeSlot(id, false);

            id = node.getDynInventory()
                    .getEntryByIndex(2 * index)
                    .getKey();

            node.getDynInventory().removeSlot(id, false);

            node.getModuleMap().removeByTimelineId(id);
        }
    }

    public ModuleNode getCurModuleNode(){
        return nodeStack.peek();
    }

    public void push(long prefabId, long timelineId){
        ModuleNode node = getCurModuleNode().getModuleMap().getNodeById(prefabId, timelineId);
        if(node != null){
            if(!isSame(node)){
                nodeStack.push(node);
            }
        }
    }

    public void push(ModuleNode curModuleNode) {
        ModuleNode top = getCurModuleNode();
        if(top != curModuleNode){
            nodeStack.push(curModuleNode);
        }
    }

    public void pop(){
        if(nodeStack.size() > 1){
            nodeStack.pop();
        }
    }

    public void resetNode(){
        while (nodeStack.size() > 1){
            nodeStack.pop();
        }
    }

    public boolean isSame(ModuleNode node){
        if(node == this) return true;
        if(node == null || node.getClass() != this.getClass()) return false;
        return node.getTimelineId() == getTimelineId() && node.getPrefabId() == getPrefabId();
    }


}
