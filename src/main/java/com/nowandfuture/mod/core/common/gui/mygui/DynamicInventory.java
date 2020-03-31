package com.nowandfuture.mod.core.common.gui.mygui;

import com.google.common.collect.Lists;
import com.nowandfuture.mod.core.common.gui.mygui.api.IDynInventoryHolder;
import com.nowandfuture.mod.core.common.gui.mygui.api.IDynamicInventory;
import com.nowandfuture.mod.core.common.gui.mygui.api.IInventorySlotChangedListener;
import com.nowandfuture.mod.core.common.gui.mygui.api.SerializeWrapper;
import com.nowandfuture.mod.core.common.gui.mygui.network.InventoryCMessage;
import com.nowandfuture.mod.core.common.gui.mygui.network.NetworkHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

public class DynamicInventory implements IDynamicInventory {
    private Map<Long, ItemStack> map;
    //slots create at Inventory,if it only be created by container,we have to notify both container and inventory
    //that cause many difficulties,all the slot will create when addSlot;
    private Map<Long, AbstractContainer.ProxySlot> slots;
    private List<IInventorySlotChangedListener> changeListeners;

    private String title;
    private boolean hasCustomName;
    private SlotCreator creator;

    public DynamicInventory(){
        this.map = Collections.synchronizedMap(new LinkedHashMap<>());
        this.slots = Collections.synchronizedMap(new LinkedHashMap<>());
        this.creator = AbstractContainer.CREATOR.defaultCreator();
    }

    public DynamicInventory(LinkedHashMap<Long, ItemStack> map){
        this.map = map;
    }

    public void setMap(LinkedHashMap<Long, ItemStack> map) {
        this.map = map;
    }

    public Map<Long, ItemStack> getMap() {
        return map;
    }

    public void addInventoryChangeListener(IInventorySlotChangedListener listener)
    {
        if (this.changeListeners == null)
        {
            this.changeListeners = Lists.newArrayList();
        }

        this.changeListeners.add(listener);
    }

    public void removeInventoryChangeListener(IInventorySlotChangedListener listener)
    {
        if(changeListeners != null)
            this.changeListeners.remove(listener);
    }

    @Override
    public int getSizeInventory() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        for (Map.Entry<Long,ItemStack> entry : map.entrySet())
        {
            if (entry.getValue() != null && !entry.getValue().isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int index) {
        ItemStack stack = map.get((long)index);
        return stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int index, int count) {
        if(map.containsKey((long)index)) {
            ItemStack itemstack = ItemStackMapHelper.getAndSplit(this.map, index, count);

            if (!itemstack.isEmpty()) {
                this.markDirty();
                this.markDirty3(index,getStackInSlot(index));
            }

            return itemstack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemstack = this.map.get((long)index);

        if (itemstack == null || itemstack.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            setInventorySlotContents(index,ItemStack.EMPTY);
            return itemstack;
        }
    }

    @Override
    public void setInventorySlotContents(int index,@Nonnull ItemStack stack) {
        ItemStack itemstack = map.get((long) index);
        boolean flag = itemstack != null && !stack.isEmpty() && stack.isItemEqual(itemstack) && ItemStack.areItemStackTagsEqual(stack, itemstack);
        flag |= stack.isEmpty() && itemstack != null && itemstack.isEmpty();

        this.map.put((long) index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit())
        {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();

        if(!flag && itemstack != null){
            this.markDirty3(index,stack);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        if (this.changeListeners != null)
        {
            for (IInventorySlotChangedListener changeListener : this.changeListeners) {
                changeListener.onInventoryChanged(this);
            }
        }
    }

    public void markDirty3(long id,ItemStack stack) {
        if (this.changeListeners != null)
        {
            for (IInventorySlotChangedListener changeListener : this.changeListeners) {
                changeListener.onSlotContentChanged(id, stack);
            }
        }
    }

    public void markDirty2(boolean remove,long id,ItemStack itemStack,boolean forced) {
        if (this.changeListeners != null)
        {
            for (IInventorySlotChangedListener changeListener : this.changeListeners) {
                if (remove)
                    changeListener.slotRemoved(id,itemStack,forced);
                else
                    changeListener.slotAdded(id,itemStack,forced);
            }
        }
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
        
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
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
        map.clear();
        slots.clear();
    }

    @Override
    @Nonnull
    public String getName() {
        return title;
    }

    public void setCustomName(String inventoryTitleIn)
    {
        this.hasCustomName = true;
        this.title = inventoryTitleIn;
    }

    @Override
    public boolean syncAs(Map<Long, ItemStack> toSync,boolean forced) {
        List<Long> removeList = new LinkedList<>();
        for (Map.Entry<Long, ItemStack> entry:
                map.entrySet()){
            if(!toSync.containsKey(entry.getKey())){
                removeList.add(entry.getKey());
            }
        }

        for (Long key :
                removeList) {
            removeSlot(key,forced);
        }

        return true;
    }

    @Override
    public boolean hasCustomName() {
        return this.hasCustomName;
    }

    @Override
    @Nonnull
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]);
    }

    @Override
    public void removeSlot(long id,boolean forced){
        ItemStack stack = map.remove(id);
        slots.remove(id);
        if(stack != null) {
            markDirty2(true,id,stack,forced);
        }
    }

    public void createSlot(AbstractContainer.ProxySlot slot,boolean forced){
        long max = 0;
        for(Long id : map.keySet()){
            if(id > max){
                max = id;
            }
        }
        createSlot(max + 1,slot,forced);
    }

    public long createSlot(ItemStack stack, int type, boolean forced){
        long value = 0;
        while (map.containsKey(value)){
            value ++;
        }
        createSlot(value,stack,getCreator(),type,forced);
        return value;
    }

    public long createSlot(ItemStack stack, SlotCreator creator,int type,boolean forced){
        long value = 0;
        while (map.containsKey(value)){
            value ++;
        }
        createSlot(value,stack,creator,type,forced);
        return value;
    }

    @Override
    public void createSlot(long id, AbstractContainer.ProxySlot slot,boolean forced){
        if(isExistedOf(id)) {
            if(!slots.containsKey(id)) {
                slots.put(id, slot);
                markDirty2(false, id, slot.getStack(),forced);
            }
        }
    }

    @Override
    public void createSlot(long id, ItemStack stack, SlotCreator creator,int type,boolean forced) {
        if(!isExistedOf(id)){
            map.put(id,stack);
            if(type != IDynamicInventory.NULL_SLOT) {
                AbstractContainer.ProxySlot slot = creator.create(this, id, type);
                slots.put(id, slot);
            }
            markDirty2(false,id,stack,forced);
        }
    }

    @Override
    public Map<Long, AbstractContainer.ProxySlot> getSlots() {
        return slots;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean isExistedOf(long id) {
        return map.containsKey(id);
    }

    @Override
    public Map.Entry<Long,ItemStack> getEntryByIndex(int index) {
        Iterator<Map.Entry<Long,ItemStack>> stacks = map.entrySet().iterator();
        int i = 0;
        while (stacks.hasNext()){
            Map.Entry<Long,ItemStack> stack = stacks.next();
            if(i == index)
                return stack;
            i++;
        }
        return null;
    }

    @Override
    public void foreach(Consumer<Packet> consumer) {
        Iterator<Map.Entry<Long,ItemStack>> stacks = map.entrySet().iterator();
        Packet packet = new Packet();

        while (stacks.hasNext()){
            Map.Entry<Long,ItemStack> stack = stacks.next();
            packet.id = stack.getKey();
            packet.itemStack = stack.getValue();
            AbstractContainer.ProxySlot slot = slots.get(packet.id);
            packet.slotType = slot == null ? NULL_SLOT : slot.type;

            consumer.accept(packet);
        }
    }

    public SlotCreator getCreator() {
        return creator;
    }

    public void setCreator(SlotCreator creator) {
        this.creator = creator;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        ItemStackMapHelper.saveAllItems(compound,map);
        IDynamicInventory.saveSlots(compound,this);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound,boolean forced) {
        ItemStackMapHelper.loadAllItems(compound, this,forced);
        IDynamicInventory.loadAllSlots(compound,this,forced);
    }

    @Deprecated
    @SideOnly(Side.CLIENT)
    public void sync(IDynInventoryHolder<DynamicInventory, SerializeWrapper.BlockPosWrap> holder){
        NetworkHandler.INSTANCE.sendMessageToServer(
                new InventoryCMessage((short) 0,holder.getHolderId(),holder.getDynInventory().writeToNBT(new NBTTagCompound()))
        );
    }

    public static int getIndexById(IDynamicInventory dynamicInventory,long id){
        Set<Long> ids = dynamicInventory.getSlots().keySet();
        int index = 0;
        for (Long i :
                ids) {
            if(id == i) return index;
        }
        return -1;
    }
}
