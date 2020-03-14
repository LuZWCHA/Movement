package com.nowandfuture.mod.core.common.gui.mygui;


import com.google.common.collect.Sets;
import com.nowandfuture.mod.core.common.gui.DynamicInventory;
import com.nowandfuture.mod.core.common.gui.IDynamicInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractContainer extends Container{
    protected IDynamicInventory dynamicInventory;

    protected int dragMode = -1;
    protected int dragEvent;
    protected final Set<Slot> dragSlots = Sets.newHashSet();

    public AbstractContainer(){
        super();
        dynamicInventory = new DynamicInventory();
    }

    public void addExtSlot(long id, ProxySlot slot, EntityPlayer player){
        dynamicInventory.createSlot(id,slot);
    }

    public void addExtSlot(long id, EntityPlayer player, DynamicInventory.SlotCreator creator,int type){
        addExtSlot(id,creator.create(dynamicInventory,id,type),player);
    }

    public void addExtSlots(Map<Long,ItemStack> slots, EntityPlayer player, DynamicInventory.SlotCreator creator,int type){
        for (Map.Entry<Long, ItemStack> e :
                slots.entrySet()) {
            addExtSlot(e.getKey(), player, creator,type);
        }
    }

    public void addExtSlots(Map<Long,ItemStack> slots,EntityPlayer player){
        addExtSlots(slots, player,CREATOR.defaultCreator(),0);
    }

    public void removeExtSlot(int id, EntityPlayer player){
        ItemStack itemStack = dynamicInventory.getStackInSlot(id);
        if(itemStack != null && !itemStack.isEmpty()){
            InventoryHelper.spawnItemStack(player.world,player.posX,player.posY,player.posZ, itemStack);
        }
        dynamicInventory.removeSlot(id);
    }

    @Nullable
    @Override
    public Slot getSlotFromInventory(IInventory inv, int slotIn) {
        if(inv instanceof IDynamicInventory) {
            for (Map.Entry<Long, ProxySlot> e :
                    ((IDynamicInventory) inv).getSlots().entrySet()) {
                if(e.getValue() != null && e.getValue().isHere(inv,slotIn)){
                    return e.getValue();
                }
            }
        }
        
        return super.getSlotFromInventory(inv, slotIn);
    }

    @Override
    public NonNullList<ItemStack> getInventory()
    {
        NonNullList<ItemStack> list = super.getInventory();
        for (Slot is :
                dynamicInventory.getSlots().values()) {
            if(is != null && is.getHasStack()){
                list.add(is.getStack());
            }
        }
        return list;
    }

    @Override
    public void setAll(List<ItemStack> allItemStacks) {
        for(int i = 0;i < inventorySlots.size();i++){
            getSlot(i).putStack(allItemStacks.get(i));
        }

        if(allItemStacks.size() > inventorySlots.size()){
            int i = inventorySlots.size();
            for (ProxySlot s :
                    dynamicInventory.getSlots().values()) {
                if(i >= allItemStacks.size()) break;
                s.putStack(allItemStacks.get(i));
                i++;
            }
        }
    }

    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;

        if (reverseDirection)
        {
            i = endIndex - 1;
        }

        if (stack.isStackable())
        {
            while (!stack.isEmpty())
            {
                if (reverseDirection)
                {
                    if (i < startIndex)
                    {
                        break;
                    }
                }
                else if (i >= endIndex)
                {
                    break;
                }

                Slot slot = this.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();

                if (!itemstack.isEmpty() && itemstack.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, itemstack))
                {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());

                    if (j <= maxSize)
                    {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.onSlotChanged();
                        flag = true;
                    }
                    else if (itemstack.getCount() < maxSize)
                    {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.onSlotChanged();
                        flag = true;
                    }
                }

                if (reverseDirection)
                {
                    --i;
                }
                else
                {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty())
        {
            if (reverseDirection)
            {
                i = endIndex - 1;
            }
            else
            {
                i = startIndex;
            }

            while (true)
            {
                if (reverseDirection)
                {
                    if (i < startIndex)
                    {
                        break;
                    }
                }
                else if (i >= endIndex)
                {
                    break;
                }

                Slot slot1 = this.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();

                if (itemstack1.isEmpty() && slot1.isItemValid(stack))
                {
                    if (stack.getCount() > slot1.getSlotStackLimit())
                    {
                        slot1.putStack(stack.splitStack(slot1.getSlotStackLimit()));
                    }
                    else
                    {
                        slot1.putStack(stack.splitStack(stack.getCount()));
                    }

                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }

                if (reverseDirection)
                {
                    --i;
                }
                else
                {
                    ++i;
                }
            }
        }

        return flag;
    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack) {
        super.putStackInSlot(slotID, stack);
    }

    @Override
    public ItemStack slotClick(int slotId, int button, ClickType clickTypeIn, EntityPlayer player) {
        int offset = inventorySlots.size();

        SlotProvider provider = new OriginalSlotProvider(inventorySlots);

        if(slotId >= offset){
            slotId -= offset;
            provider = new ProxySlotProvider(dynamicInventory.getSlots());
        }

        return slotClickInExtSlot(provider, slotId, button, clickTypeIn, player);
    }

    //retain vanilla slotCLick
    public ItemStack slotClick1(int slotId, int button, ClickType clickTypeIn, EntityPlayer player) {
        return super.slotClick(slotId, button, clickTypeIn, player);
    }

    protected ItemStack slotClickInExtSlot(SlotProvider inventorySlots,int slotId, int button, ClickType clickTypeIn, EntityPlayer player){

        ItemStack itemstack = ItemStack.EMPTY;
        InventoryPlayer inventoryplayer = player.inventory;

        if (clickTypeIn == ClickType.QUICK_CRAFT)
        {
            int j1 = this.dragEvent;
            this.dragEvent = getDragEvent(button);

            if ((j1 != 1 || this.dragEvent != 2) && j1 != this.dragEvent)
            {
                this.resetDrag();
            }
            else if (inventoryplayer.getItemStack().isEmpty())
            {
                this.resetDrag();
            }
            else if (this.dragEvent == 0)
            {
                this.dragMode = extractDragMode(button);

                if (isValidDragMode(this.dragMode, player))
                {
                    this.dragEvent = 1;
                    this.dragSlots.clear();
                }
                else
                {
                    this.resetDrag();
                }
            }
            else if (this.dragEvent == 1)
            {
                Slot slot7 = inventorySlots.get((long)slotId);
                ItemStack itemstack12 = inventoryplayer.getItemStack();

                if (slot7 != null && canAddItemToSlot(slot7, itemstack12, true) && slot7.isItemValid(itemstack12) && (this.dragMode == 2 || itemstack12.getCount() > this.dragSlots.size()) && this.canDragIntoSlot(slot7))
                {
                    this.dragSlots.add(slot7);
                }
            }
            else if (this.dragEvent == 2)
            {
                if (!this.dragSlots.isEmpty())
                {
                    ItemStack itemstack9 = inventoryplayer.getItemStack().copy();
                    int k1 = inventoryplayer.getItemStack().getCount();

                    for (Slot slot8 : this.dragSlots)
                    {
                        ItemStack itemstack13 = inventoryplayer.getItemStack();

                        if (slot8 != null && canAddItemToSlot(slot8, itemstack13, true) && slot8.isItemValid(itemstack13) && (this.dragMode == 2 || itemstack13.getCount() >= this.dragSlots.size()) && this.canDragIntoSlot(slot8))
                        {
                            ItemStack itemstack14 = itemstack9.copy();
                            int j3 = slot8.getHasStack() ? slot8.getStack().getCount() : 0;
                            computeStackSize(this.dragSlots, this.dragMode, itemstack14, j3);
                            int k3 = Math.min(itemstack14.getMaxStackSize(), slot8.getItemStackLimit(itemstack14));

                            if (itemstack14.getCount() > k3)
                            {
                                itemstack14.setCount(k3);
                            }

                            k1 -= itemstack14.getCount() - j3;
                            slot8.putStack(itemstack14);
                        }
                    }

                    itemstack9.setCount(k1);
                    inventoryplayer.setItemStack(itemstack9);
                }

                this.resetDrag();
            }
            else
            {
                this.resetDrag();
            }
        }
        else if (this.dragEvent != 0)
        {
            this.resetDrag();
        }
        else if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (button == 0 || button == 1))
        {
            if (slotId == -999)
            {
                if (!inventoryplayer.getItemStack().isEmpty())
                {
                    if (button == 0)
                    {
                        player.dropItem(inventoryplayer.getItemStack(), true);
                        inventoryplayer.setItemStack(ItemStack.EMPTY);
                    }

                    if (button == 1)
                    {
                        player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);
                    }
                }
            }
            else if (clickTypeIn == ClickType.QUICK_MOVE)
            {
                if (slotId < 0)
                {
                    return ItemStack.EMPTY;
                }

                Slot slot5 = inventorySlots.get((long)slotId);

                if (slot5 == null || !slot5.canTakeStack(player))
                {
                    return ItemStack.EMPTY;
                }

                for (ItemStack itemstack7 = this.transferStackInSlot(player, slotId); !itemstack7.isEmpty() && ItemStack.areItemsEqual(slot5.getStack(), itemstack7); itemstack7 = this.transferStackInSlot(player, slotId))
                {
                    itemstack = itemstack7.copy();
                }
            }
            else
            {
                if (slotId < 0)
                {
                    return ItemStack.EMPTY;
                }

                Slot slot6 = inventorySlots.get((long)slotId);

                if (slot6 != null)
                {
                    ItemStack itemstack8 = slot6.getStack();
                    ItemStack itemstack11 = inventoryplayer.getItemStack();

                    if (!itemstack8.isEmpty())
                    {
                        itemstack = itemstack8.copy();
                    }

                    if (itemstack8.isEmpty())
                    {
                        if (!itemstack11.isEmpty() && slot6.isItemValid(itemstack11))
                        {
                            int i3 = button == 0 ? itemstack11.getCount() : 1;

                            if (i3 > slot6.getItemStackLimit(itemstack11))
                            {
                                i3 = slot6.getItemStackLimit(itemstack11);
                            }

                            slot6.putStack(itemstack11.splitStack(i3));
                        }
                    }
                    else if (slot6.canTakeStack(player))
                    {
                        if (itemstack11.isEmpty())
                        {
                            if (itemstack8.isEmpty())
                            {
                                slot6.putStack(ItemStack.EMPTY);
                                inventoryplayer.setItemStack(ItemStack.EMPTY);
                            }
                            else
                            {
                                int l2 = button == 0 ? itemstack8.getCount() : (itemstack8.getCount() + 1) / 2;
                                inventoryplayer.setItemStack(slot6.decrStackSize(l2));

                                if (itemstack8.isEmpty())
                                {
                                    slot6.putStack(ItemStack.EMPTY);
                                }

                                slot6.onTake(player, inventoryplayer.getItemStack());
                            }
                        }
                        else if (slot6.isItemValid(itemstack11))
                        {
                            if (itemstack8.getItem() == itemstack11.getItem() && itemstack8.getMetadata() == itemstack11.getMetadata() && ItemStack.areItemStackTagsEqual(itemstack8, itemstack11))
                            {
                                int k2 = button == 0 ? itemstack11.getCount() : 1;

                                if (k2 > slot6.getItemStackLimit(itemstack11) - itemstack8.getCount())
                                {
                                    k2 = slot6.getItemStackLimit(itemstack11) - itemstack8.getCount();
                                }

                                if (k2 > itemstack11.getMaxStackSize() - itemstack8.getCount())
                                {
                                    k2 = itemstack11.getMaxStackSize() - itemstack8.getCount();
                                }

                                itemstack11.shrink(k2);
                                itemstack8.grow(k2);
                            }
                            else if (itemstack11.getCount() <= slot6.getItemStackLimit(itemstack11))
                            {
                                slot6.putStack(itemstack11);
                                inventoryplayer.setItemStack(itemstack8);
                            }
                        }
                        else if (itemstack8.getItem() == itemstack11.getItem() && itemstack11.getMaxStackSize() > 1 && (!itemstack8.getHasSubtypes() || itemstack8.getMetadata() == itemstack11.getMetadata()) && ItemStack.areItemStackTagsEqual(itemstack8, itemstack11) && !itemstack8.isEmpty())
                        {
                            int j2 = itemstack8.getCount();

                            if (j2 + itemstack11.getCount() <= itemstack11.getMaxStackSize())
                            {
                                itemstack11.grow(j2);
                                itemstack8 = slot6.decrStackSize(j2);

                                if (itemstack8.isEmpty())
                                {
                                    slot6.putStack(ItemStack.EMPTY);
                                }

                                slot6.onTake(player, inventoryplayer.getItemStack());
                            }
                        }
                    }

                    slot6.onSlotChanged();
                }
            }
        }
        else if (clickTypeIn == ClickType.SWAP && button >= 0 && button < 9)
        {
            Slot slot4 = inventorySlots.get((long)slotId);
            ItemStack itemstack6 = inventoryplayer.getStackInSlot(button);
            ItemStack itemstack10 = slot4.getStack();

            if (!itemstack6.isEmpty() || !itemstack10.isEmpty())
            {
                if (itemstack6.isEmpty())
                {
                    if (slot4.canTakeStack(player))
                    {
                        inventoryplayer.setInventorySlotContents(button, itemstack10);
//                        slot4.onSwapCraft(itemstack10.getCount());
                        slot4.putStack(ItemStack.EMPTY);
                        slot4.onTake(player, itemstack10);
                    }
                }
                else if (itemstack10.isEmpty())
                {
                    if (slot4.isItemValid(itemstack6))
                    {
                        int l1 = slot4.getItemStackLimit(itemstack6);

                        if (itemstack6.getCount() > l1)
                        {
                            slot4.putStack(itemstack6.splitStack(l1));
                        }
                        else
                        {
                            slot4.putStack(itemstack6);
                            inventoryplayer.setInventorySlotContents(button, ItemStack.EMPTY);
                        }
                    }
                }
                else if (slot4.canTakeStack(player) && slot4.isItemValid(itemstack6))
                {
                    int i2 = slot4.getItemStackLimit(itemstack6);

                    if (itemstack6.getCount() > i2)
                    {
                        slot4.putStack(itemstack6.splitStack(i2));
                        slot4.onTake(player, itemstack10);

                        if (!inventoryplayer.addItemStackToInventory(itemstack10))
                        {
                            player.dropItem(itemstack10, true);
                        }
                    }
                    else
                    {
                        slot4.putStack(itemstack6);
                        inventoryplayer.setInventorySlotContents(button, itemstack10);
                        slot4.onTake(player, itemstack10);
                    }
                }
            }
        }
        else if (clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode && inventoryplayer.getItemStack().isEmpty() && slotId >= 0)
        {
            Slot slot3 = inventorySlots.get((long)slotId);

            if (slot3 != null && slot3.getHasStack())
            {
                ItemStack itemstack5 = slot3.getStack().copy();
                itemstack5.setCount(itemstack5.getMaxStackSize());
                inventoryplayer.setItemStack(itemstack5);
            }
        }
        else if (clickTypeIn == ClickType.THROW && inventoryplayer.getItemStack().isEmpty() && slotId >= 0)
        {
            Slot slot2 = inventorySlots.get((long)slotId);

            if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(player))
            {
                ItemStack itemstack4 = slot2.decrStackSize(button == 0 ? 1 : slot2.getStack().getCount());
                slot2.onTake(player, itemstack4);
                player.dropItem(itemstack4, true);
            }
        }
        else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0)
        {
            Slot slot = inventorySlots.get((long)slotId);
            ItemStack itemstack1 = inventoryplayer.getItemStack();

            if (!itemstack1.isEmpty() && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player)))
            {
                int i = button == 0 ? 0 : inventorySlots.size() - 1;
                int j = button == 0 ? 1 : -1;

                for (int k = 0; k < 2; ++k)
                {
                    for (int l = i; l >= 0 && l < inventorySlots.size() && itemstack1.getCount() < itemstack1.getMaxStackSize(); l += j)
                    {
                        Slot slot1 = inventorySlots.get(l);
                        if(slot1 == null) break;

                        if (slot1.getHasStack() && canAddItemToSlot(slot1, itemstack1, true) && slot1.canTakeStack(player) && this.canMergeSlot(itemstack1, slot1))
                        {
                            ItemStack itemstack2 = slot1.getStack();

                            if (k != 0 || itemstack2.getCount() != itemstack2.getMaxStackSize())
                            {
                                int i1 = Math.min(itemstack1.getMaxStackSize() - itemstack1.getCount(), itemstack2.getCount());
                                ItemStack itemstack3 = slot1.decrStackSize(i1);
                                itemstack1.grow(i1);

                                if (itemstack3.isEmpty())
                                {
                                    slot1.putStack(ItemStack.EMPTY);
                                }

                                slot1.onTake(player, itemstack3);
                            }
                        }
                    }
                }
            }

            this.detectAndSendChanges();
        }

        return itemstack;
    }

    protected void resetDrag()
    {
        this.dragEvent = 0;
        this.dragSlots.clear();
    }

    public  interface SlotProvider<T extends Slot>{
        T get(long id);
        T get(int index);
        int size();
    }

    private static class OriginalSlotProvider implements SlotProvider<Slot>{
        private List<Slot> list;

        public OriginalSlotProvider(List<Slot> list){
            this.list = list;
        }

        @Override
        public Slot get(int index) {
            return list.get(index);
        }

        @Override
        public Slot get(long id) {
            return list.get((int)id);
        }

        @Override
        public int size() {
            return list.size();
        }
    }

    private static class ProxySlotProvider implements SlotProvider<ProxySlot>{
        private Map<Long, ProxySlot> list;

        public ProxySlotProvider(Map<Long,ProxySlot> list){
            this.list = list;
        }

        @Override
        public ProxySlot get(int index) {
            int i = 0;
            for (ProxySlot slot :
                    list.values()) {
                if(i == index){
                    return slot;
                }
                i++;
            }
            return null;
        }

        @Override
        public ProxySlot get(long id) {
            return list.get(id);
        }

        @Override
        public int size() {
            return list.size();
        }
    }

    public static class DefaultCreator extends DynamicInventory.SlotCreator {

        @Override
        public ProxySlot create(IDynamicInventory inventory,long index,int type) {
            return new ProxySlot(inventory, (int) index,-1){
                @Override
                public int getSlotStackLimit() {
                    return 64;
                }
            };
        }
    }

    public static class CREATOR{
        public static DynamicInventory.SlotCreator defaultCreator(){
            return new DefaultCreator();
        }
    }

    public static abstract class ProxySlot extends Slot{

        int type;
        boolean enable;

        public ProxySlot(IDynamicInventory inventoryIn, int index, int type) {
            super(inventoryIn, index,0,0);
            this.type = type;
        }

        @Override
        public void putStack(ItemStack stack) {
            super.putStack(stack);
        }

        @Override
        public boolean isEnabled() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public int getType() {
            return type;
        }
    }
}
