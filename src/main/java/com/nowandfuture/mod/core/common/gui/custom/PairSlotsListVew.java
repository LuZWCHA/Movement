package com.nowandfuture.mod.core.common.gui.custom;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityCoreModule;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.DynamicInventory;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.Button;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.MyAbstractList;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.SlotView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.TextView;
import com.nowandfuture.mod.core.movecontrol.ModuleNode;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.network.message.LMessage;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Map;

public class PairSlotsListVew extends MyAbstractList<MyAbstractList.ViewHolder> {

    public PairSlotsListVew(@Nonnull RootView rootView) {
        super(rootView);
    }

    public PairSlotsListVew(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    @Override
    protected void drawSplitLine() {
        drawHorizontalLine(0,getWidth(),0, DrawHelper.colorInt(180,180,180,128));
    }

    static class SlotsViewHolder extends ViewHolder{
        final TextView indexTv;
        final SlotView psv;
        final SlotView tlv;
        final Button removeBtn;
        final Button enterBtn;

        public SlotsViewHolder(RootView rootView,MyAbstractList parent){
            super(rootView,parent);
            AbstractGuiContainer.GuiBuilder.wrap(this)
                    .setX(0).setY(0).setWidth(100).setHeight(20).build();
            psv = new SlotView(rootView,this,null);
            tlv = new SlotView(rootView,this,null);
            removeBtn = new Button(rootView,this);
            indexTv = new TextView(rootView,this);
            enterBtn = new Button(rootView,this);

            indexTv.setX(4);
            indexTv.setY(2);
            indexTv.setWidth(20);
            indexTv.setHeight(16);

            psv.setScissor(false);
            tlv.setScissor(false);
            int offsetY = (getHeight() - psv.getHeight())/2;
            psv.setX(20);
            psv.setY(offsetY);
            tlv.setX(40);
            tlv.setY(offsetY);

            removeBtn.setX(80);
            removeBtn.setY(2);
            removeBtn.setWidth(16);
            removeBtn.setHeight(16);
            removeBtn.setImageLocation(new ResourceLocation(Movement.MODID,"textures/gui/remove.png"));

            enterBtn.setX(64);
            enterBtn.setY(2);
            enterBtn.setWidth(16);
            enterBtn.setHeight(16);
            enterBtn.setImageLocation(new ResourceLocation(Movement.MODID,"textures/gui/enter.png"));

            addChildren(indexTv,psv,tlv,removeBtn, enterBtn);
        }

        @Override
        protected boolean onPressed(int mouseX, int mouseY, int state) {
            return true;
        }

        @Override
        protected boolean onInterceptClickAction(int mouseX, int mouseY, int button) {
            return true;
        }

        public void setSlots(AbstractContainer.ProxySlot slot1, AbstractContainer.ProxySlot slot2) {
            psv.setSlot(slot1);
            tlv.setSlot(slot2);
        }

        public void setIndex(int index){
            indexTv.setText(String.valueOf(index));
        }

    }

    public static class SlotsAdapter extends Adapter<SlotsViewHolder> {

        private TileEntityCoreModule coreModule;

        public SlotsAdapter(){
        }

        @Override
        public int getSize() {
            IInventory inventory = coreModule.getDynInventory();
            if((inventory.getSizeInventory() & 1) == 0)
                return inventory.getSizeInventory() / 2;
            else
                return 0;
        }

        @Override
        public int getHeight() {
            return 20;
        }

        @Override
        public SlotsViewHolder createHolder(RootView rootView,MyAbstractList parent) {
            return new SlotsViewHolder(rootView,parent);
        }

        @Override
        public void handle(MyAbstractList list,SlotsViewHolder viewHolder, int index) {
            DynamicInventory inventory = coreModule.getCurModuleNode().getDynInventory();
            Map.Entry<Long, ItemStack> entry = inventory.getEntryByIndex(index * 2);
            Map.Entry<Long, ItemStack> entry1 = inventory.getEntryByIndex(index * 2 + 1);
            if(entry == null || entry1 == null) return;

            long id = entry.getKey();
            AbstractContainer.ProxySlot prefabSlot = inventory.getSlots().get(id);
            id = entry1.getKey();
            AbstractContainer.ProxySlot timelineSlot = inventory.getSlots().get(id);

            if(prefabSlot != null && timelineSlot != null){
                viewHolder.setSlots(prefabSlot,timelineSlot);
                viewHolder.removeBtn.setActionListener(new View.ActionListener() {
                    @Override
                    public void onClicked(View v) {
                        NBTTagCompound compound = new NBTTagCompound();
                        compound.setString("moduleId",coreModule.getCurModuleNode().getId());
                        compound.setInteger("index",index);
                        LMessage.NBTMessage message = new LMessage.NBTMessage(LMessage.NBTMessage.GUI_REMOVE_NODE,compound);
                        message.setPos(coreModule.getPos());
                        NetworkHandler.INSTANCE.sendMessageToServer(message);
                        // TODO: 2020/3/23 predict ...
                    }
                });
                viewHolder.enterBtn.setActionListener(new View.ActionListener() {
                    @Override
                    public void onClicked(View v) {
                        ModuleNode curNode = coreModule.getCurModuleNode();
                        Map.Entry<Long, ItemStack> entry = curNode.getDynInventory().getEntryByIndex(index * 2);
                        if(entry != null) {
                            long prefabId = entry.getKey();
                            ModuleNode node = curNode.getModuleMap().prefabToModule(prefabId);
                            if (node != null) {
                                String clickItemId = node.getId();
                                LMessage.StringDataSyncMessage message =
                                        new LMessage.StringDataSyncMessage(LMessage.StringDataSyncMessage.GUI_CLICK_NODE, clickItemId);
                                message.setPos(coreModule.getPos());

                                NetworkHandler.INSTANCE.sendMessageToServer(message);
                            }
                        }
                    }
                });

                viewHolder.setIndex(index);
            }


        }

        public void setCoreModule(TileEntityCoreModule coreModule) {
            this.coreModule = coreModule;
        }
    }
}
