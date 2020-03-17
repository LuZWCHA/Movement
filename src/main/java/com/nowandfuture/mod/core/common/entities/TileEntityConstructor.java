package com.nowandfuture.mod.core.common.entities;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.Items.PrefabItem;
import com.nowandfuture.mod.core.common.gui.ContainerConstructor;
import com.nowandfuture.mod.core.common.gui.mygui.api.IChangeListener;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.prefab.NormalPrefab;
import com.nowandfuture.mod.core.prefab.MultiThreadPrefabWrapper;
import com.nowandfuture.mod.core.selection.AABBSelectArea;
import com.nowandfuture.mod.network.BigNBTTagSplitPacketTool;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.network.message.DivBytesMessage;
import com.nowandfuture.mod.network.message.LMessage;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class TileEntityConstructor extends TileEntityLockable implements ITickable, ISidedInventory, MultiThreadPrefabWrapper.ConstructListener {

    private static final int[] SLOTS_TOP = new int[] {0};
    private static final int[] SLOTS_BOTTOM = new int[] {0};
    private static final int[] SLOTS_SIDES = new int[] {0};

    public final static String NBT_IS_LOCK = "IsLock";
    public final static String NBT_LICK_USER = "LockUser";
    public final static String NBT_PROGRESS = "Progress";


    //-------------only for client-------------------------
    private final MultiThreadPrefabWrapper wrapper;
    private BigNBTTagSplitPacketTool bigNBTTagSplitPacket;

    public void setAreaSizeChanged(IChangeListener areaSizeChanged) {
        this.areaSizeChanged = areaSizeChanged;
    }

    private IChangeListener lockChanged;
    private IChangeListener constructChanged;
    private IChangeListener areaSizeChanged;
    private IChangeListener slotChanged;

    //--------------only for server-------------------------
    private int lastRevIndex = -1;
    private boolean revTag = false;

    //---------------------------------------- Server and Client----------------------------------------------
    //progress: -1,0,1 for client means [construct finished/idle] ,[constructing] and [complete constructing](send tag to server but not
    //rev tag)
    private float constructProgress= -1;

    private NonNullList<ItemStack> constructorItemStacks =
            NonNullList.withSize(1, ItemStack.EMPTY);

    private final AABBSelectArea aabbSelectArea;
    private boolean isLock = false;
    private String lockUserName;

    //construct time out
    private final static int TIMEOUT_TICKS = 600;//about 10 seconds
    private int timeout = 0;

    public TileEntityConstructor(){
        wrapper = new MultiThreadPrefabWrapper();
        aabbSelectArea = new AABBSelectArea();
    }

    //aways build
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    public String getPrefabName(){
        if(constructorItemStacks.isEmpty()){
            return Strings.EMPTY;
        }
        if(!constructorItemStacks.get(0).isEmpty()){
            Item item = constructorItemStacks.get(0).getItem();
            if(item instanceof PrefabItem){
                ItemStack itemStack = constructorItemStacks.get(0);
                return PrefabItem.getPrefabName(itemStack);
            }
        }
        return Strings.EMPTY;
    }

    public void setPrefabName(String name){
        if(!constructorItemStacks.isEmpty() &&
                !constructorItemStacks.get(0).isEmpty()){
            Item item = constructorItemStacks.get(0).getItem();
            if(item instanceof PrefabItem){
                ItemStack itemStack = constructorItemStacks.get(0);
                PrefabItem.setPrefabName(itemStack,name);
            }
        }
    }

    public AABBSelectArea getAABBSelectArea() {
        return aabbSelectArea;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (side == EnumFacing.DOWN)
        {
            return SLOTS_BOTTOM;
        }
        else
        {
            return side == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES;
        }
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return isItemValidForSlot(index,itemStackIn) && index == 0 && isEmpty();
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        if (direction == EnumFacing.DOWN && index == 0)
        {
            Item item = stack.getItem();
            return item instanceof PrefabItem && !isEmpty();
        }

        return false;
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.constructorItemStacks)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.constructorItemStacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.constructorItemStacks, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.constructorItemStacks, index);
    }



    @Override
    public void setInventorySlotContents(int index,@Nonnull ItemStack stack) {
        ItemStack itemstack = this.constructorItemStacks.get(index);
        boolean flag = !stack.isEmpty() &&
                stack.isItemEqual(itemstack) &&
                ItemStack.areItemStackTagsEqual(stack, itemstack);
        this.constructorItemStacks.set(index, stack);

        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        //not same
        if (index == 0 && !flag)
        {
            if(slotChanged != null)
                slotChanged.changed();
            wrapper.tryStopConstruct();
            this.markDirty();
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
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem() instanceof PrefabItem;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        aabbSelectArea.writeToNBT(compound);
        compound.setBoolean(NBT_IS_LOCK,isLock);
        ItemStackHelper.saveAllItems(compound,constructorItemStacks);

        if(isLock)
            compound.setString(NBT_LICK_USER,lockUserName);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        aabbSelectArea.readFromNBT(compound);
        ItemStackHelper.loadAllItems(compound, constructorItemStacks);

        //care for trigger the lockChange listener ,setName first!!
        boolean lock = compound.getBoolean(NBT_IS_LOCK);
        if(lock)
            lockUserName = compound.getString(NBT_LICK_USER);

        //to trigger the listener for gui
        setLock(lock);
    }

    @Override
    public int getField(int id) {
        return (int) (constructProgress * 100);
    }

    @Override
    public void setField(int id, int value) {
        constructProgress = ((float)value)/100;
    }

    @Override
    public int getFieldCount() {
        return 1;
    }

    @Override
    public void clear() {
        constructorItemStacks.clear();
    }

    @Override
    public void update() {

        //client construct timeout is about 100 ticks(contain sending messages)
        if(world.isRemote) {

            if(isConstructing()) {

                if(timeout % 10 == 0){
                    LMessage.FloatDataSyncMessage message =
                            new LMessage.FloatDataSyncMessage(LMessage.FloatDataSyncMessage.PROGRESS_FLAG,
                                    (float) getProgress());
                    message.setPos(getPos());
                    NetworkHandler.INSTANCE.sendMessageToServer(message);
                }

                timeout++;

                if (timeout > TIMEOUT_TICKS) {
                    wrapper.tryStopConstruct();
                    Movement.logger.warn("client timeout!");
                    timeout = 0;
                }

            }else{
                //refresh
                if(constructProgress != -1) {
                    setConstructProgress(-1);
                }
                timeout = 0;
            }

        }else{
            //server rev timeout is 100 ticks
            if(revTag){
                timeout ++;
                if(timeout > TIMEOUT_TICKS){
                    //server timeout will end rev
                    Movement.logger.warn("server timeout!");
                    revTag = false;
                    timeout = 0;
                }
            }
        }
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerConstructor(playerInventory,this);
    }

    @Override
    public String getGuiID() {
        return "constructor_gui";
    }

    @Override
    public String getName() {
        return Movement.MODID + "_constructor";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void askForConstruct(){
        String name = Minecraft.getMinecraft().player.getName();
        LMessage.StringDataSyncMessage message =
                new LMessage.StringDataSyncMessage(LMessage.StringDataSyncMessage.CONSTRUCT_LOCK_FLAG, name);
        message.setPos(getPos());
        NetworkHandler.INSTANCE.sendMessageToServer(message);
    }

    @SideOnly(Side.CLIENT)
    public void constructTest(World worldIn, BlockPos pos){
        if(!wrapper.isConstructing() &&
                !getStackInSlot(0).isEmpty()&&
                isLock &&
                lockUserName.equals(Minecraft.getMinecraft().player.getName())){
            NormalPrefab prefab = new NormalPrefab(worldIn,pos.add(1,0,0),
                    new Vec3i(aabbSelectArea.getXLength(),
                            aabbSelectArea.getYLength(),
                            aabbSelectArea.getZLength()));
            prefab.setName(getPrefabName());
            wrapper.set(prefab);
            wrapper.setConstructListener(this);

            wrapper.constructLocalWoldFromActrualWorld();
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isConstructing(){
        return wrapper.isConstructing();
    }

    //for proxy client actual progress
    @SideOnly(Side.CLIENT)
    public double getProgress(){
        return wrapper.getProgress();
    }

    //for clients and server progress
    public float getConstructProgress(){
        return constructProgress;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(),1,getUpdateTag());
    }

    public SPacketUpdateTileEntity getProgressUpdatePacket(){
        return new SPacketUpdateTileEntity(getPos(),2,getProgressUpdateTag());
    }

    public SPacketUpdateTileEntity getResizeUpdatePacket(){
        return new SPacketUpdateTileEntity(getPos(),3,getResizeUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound nbtGet = pkt.getNbtCompound();

        if(pkt.getTileEntityType() == 2){
            float progress = nbtGet.getFloat(NBT_PROGRESS);
            //from -1 to 0...
            setConstructProgress(progress);
        }else if(pkt.getTileEntityType() == 1){
            this.readFromNBT(nbtGet);
        } else if(pkt.getTileEntityType() == 3){
            aabbSelectArea.readFromNBT(nbtGet);
            if(areaSizeChanged != null)
                areaSizeChanged.changed();
        }
    }

    public NBTTagCompound getResizeUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        aabbSelectArea.writeToNBT(compound);
        return compound;
    }

    public NBTTagCompound getProgressUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setFloat(NBT_PROGRESS,constructProgress);
        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        return compound;
    }

    //-----------------------------------------Client Construct---------------------------------------

    @Override
    public void onError(Exception e) {
        LMessage.FloatDataSyncMessage message =
                new LMessage.FloatDataSyncMessage(LMessage.FloatDataSyncMessage.PROGRESS_FLAG, -1);
        message.setPos(getPos());
        NetworkHandler.INSTANCE.sendMessageToServer(message);
    }

    @Override
    public void onStart() {
        LMessage.FloatDataSyncMessage message =
                new LMessage.FloatDataSyncMessage(LMessage.FloatDataSyncMessage.PROGRESS_FLAG, 0);
        message.setPos(getPos());
        NetworkHandler.INSTANCE.sendMessageToServer(message);
    }

    @Override
    public void onCompleted(AbstractPrefab prefab) {
        NetworkHandler.INSTANCE.sendClientCommandMessage("complete to client construct" + getPrefabName());
        prefab.setName(getPrefabName());
        NBTTagCompound compound = prefab.writeToNBT(new NBTTagCompound());

        try {
            //send nbt byte-array to server
            BigNBTTagSplitPacketTool.forEachByteArray(compound, 1024 * 10,
                    new DivBytesMessage.DivBytesGetter() {
                        @Override
                        public void get(int index, int divNum, byte[] bytes){
                            if(bytes == null) return;
                            DivBytesMessage divBytesMessage = new DivBytesMessage(bytes, index, divNum);
                            divBytesMessage.setPos(getPos());
                            NetworkHandler.INSTANCE.sendMessageToServer(divBytesMessage);
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
            LMessage.FloatDataSyncMessage message =
                    new LMessage.FloatDataSyncMessage(LMessage.FloatDataSyncMessage.PROGRESS_FLAG, -1);
            message.setPos(getPos());
            NetworkHandler.INSTANCE.sendMessageToServer(message);
        }

    }

    @Override
    public void invalidate() {
        super.invalidate();
        if(world.isRemote){
            wrapper.tryStopConstruct();
        }
    }

    //-------------------------------------Update ItemStack to finish construct------------------------------

    public boolean revItemDivBytes(int index, int divNum, byte[] bytes){
        if(bigNBTTagSplitPacket == null) bigNBTTagSplitPacket = new BigNBTTagSplitPacketTool();
        boolean completed;
        try {
            completed = bigNBTTagSplitPacket.putBytes(index, divNum, bytes);
            if(completed) {
                getStackInSlot(0).setTagCompound(bigNBTTagSplitPacket.getNBT());
                bigNBTTagSplitPacket.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
            completed = false;
        }

        if(completed) {
            setConstructProgress(-1);
            markDirty();
        }

        return completed;
    }

    public void setConstructProgress(float constructProgress) {
        float temp = this.constructProgress;
        this.constructProgress = constructProgress;
        if(this.constructChanged != null){
            if((constructProgress == -1 || temp == -1) && constructProgress != temp)
                constructChanged.changed();
        }
    }


    //--------------------------------------for server---------------------------------------

    public int getLastRevIndex() {
        return lastRevIndex;
    }

    public void setLastRevIndex(int lastRevIndex) {
        this.lastRevIndex = lastRevIndex;
    }

    public boolean isRevTag() {
        return revTag;
    }

    public void setRevTag(boolean revTag) {
        this.revTag = revTag;
    }

    public String getLockUserName() {
        return lockUserName;
    }

    public void setLockUserName(String lockUserName) {
        this.lockUserName = lockUserName;
    }

    public void setLock(boolean lock) {
        isLock = lock;
        if(lockChanged != null){
            lockChanged.changed();
        }
    }

    public boolean isLock() {
        return isLock;
    }

    public void setLockChanged(IChangeListener lockChanged) {
        this.lockChanged = lockChanged;
    }

    public void setConstructChanged(IChangeListener constructChanged) {
        this.constructChanged = constructChanged;
    }

    public void setSlotChanged(IChangeListener slotChanged) {
        this.slotChanged = slotChanged;
    }
}
