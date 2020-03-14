package com.nowandfuture.mod.core.common.gui;

import com.google.common.collect.Lists;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import com.nowandfuture.mod.core.common.gui.mygui.IInventorySlotChangedListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.*;

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
        return map.get((long)index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if(map.containsKey((long)index)) {
            ItemStack itemstack = ItemStackMapHelper.getAndSplit(this.map, index, count);

            if (!itemstack.isEmpty()) {
                this.markDirty();
            }

            return itemstack;
        }
        return null;
    }

    @Override
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
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.map.replace((long) index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit())
        {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        if (this.changeListeners != null)
        {
            for (int i = 0; i < this.changeListeners.size(); ++i)
            {
                this.changeListeners.get(i).onInventoryChanged(this);
            }
        }
    }

    public void markDirty2(boolean remove,ItemStack itemStack) {
        if (this.changeListeners != null)
        {
            for (int i = 0; i < this.changeListeners.size(); ++i)
            {
                if(remove)
                    this.changeListeners.get(i).slotRemoved(itemStack);
                else
                    this.changeListeners.get(i).slotAdded(itemStack);
            }
        }
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
        map.clear();
        slots.clear();
    }

    @Override
    public String getName() {
        return title;
    }

    public void setCustomName(String inventoryTitleIn)
    {
        this.hasCustomName = true;
        this.title = inventoryTitleIn;
    }

    @Override
    public boolean hasCustomName() {
        return this.hasCustomName;
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]);
    }

    @Override
    public void removeSlot(long id){
        ItemStack stack = map.remove(id);
        if(stack != null) markDirty2(true,stack);
        slots.remove(id);
    }

    public void createSlot(AbstractContainer.ProxySlot slot){
        long max = 0;
        for(Long id : map.keySet()){
            if(id > max){
                max = id;
            }
        }
        createSlot(max + 1,slot);
    }

    public void createSlot(ItemStack stack, int type){
        long max = 0;
        for(Long id : map.keySet()){
            if(id > max){
                max = id;
            }
        }
        createSlot(max + 1,stack,getCreator(),type);
    }

    public void createSlot(ItemStack stack, SlotCreator creator,int type){
        long max = 0;
        for(Long id : map.keySet()){
            if(id > max){
                max = id;
            }
        }
        createSlot(max + 1,stack,creator,type);
    }

    @Override
    public void createSlot(long id, AbstractContainer.ProxySlot slot){
        map.put(id,slot.getStack());
        slots.put(id,slot);
        markDirty2(false,slot.getStack());
    }

    @Override
    public void createSlot(long id, ItemStack stack, SlotCreator creator,int type) {
        map.put(id,stack);
        AbstractContainer.ProxySlot slot = creator.create(this,id,type);
        slots.put(id,slot);
        markDirty2(false,stack);
    }

    @Override
    public Map<Long, AbstractContainer.ProxySlot> getSlots() {
        return slots;
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

    public SlotCreator getCreator() {
        return creator;
    }

    public void setCreator(SlotCreator creator) {
        this.creator = creator;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        ItemStackMapHelper.saveAllItems(compound,map);
        IDynamicInventory.saveSlots(compound,this);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        ItemStackMapHelper.loadAllItems(compound, map);
        IDynamicInventory.loadAllSlots(compound,this);
    }
}
