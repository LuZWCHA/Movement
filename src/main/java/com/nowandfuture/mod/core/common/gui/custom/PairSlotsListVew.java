package com.nowandfuture.mod.core.common.gui.custom;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.api.IDynamicInventory;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.Button;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.MyAbstractList;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.SlotView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.TextView;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.item.ItemStack;

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
        final Button button;

        public SlotsViewHolder(RootView rootView,MyAbstractList parent){
            super(rootView,parent);
            AbstractGuiContainer.GuiBuilder.wrap(this)
                    .setX(0).setY(0).setWidth(100).setHeight(20).build();
            psv = new SlotView(rootView,this,null);
            tlv = new SlotView(rootView,this,null);
            button = new Button(rootView,this);
            indexTv = new TextView(rootView,this);

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

            button.setX(66);
            button.setY(2);
            button.setWidth(30);
            button.setHeight(16);
            button.setText("remove");
            addChildren(indexTv,psv,tlv,button);
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

        private IDynamicInventory iInventory;

        public SlotsAdapter(){
        }

        public void setInventory(IDynamicInventory iInventory) {
            this.iInventory = iInventory;
        }

        @Override
        public int getSize() {
            if((iInventory.getSizeInventory() & 1) == 0)
                return iInventory.getSizeInventory() / 2;
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
            Map.Entry<Long, ItemStack> entry = iInventory.getEntryByIndex(index * 2);
            Map.Entry<Long, ItemStack> entry1 = iInventory.getEntryByIndex(index * 2 + 1);
            if(entry == null || entry1 == null) return;

            long id = entry.getKey();
            AbstractContainer.ProxySlot prefabSlot = iInventory.getSlots().get(id);
            id = entry1.getKey();
            AbstractContainer.ProxySlot timelineSlot = iInventory.getSlots().get(id);

            if(prefabSlot != null && timelineSlot != null){
                viewHolder.setSlots(prefabSlot,timelineSlot);
                viewHolder.button.setActionListener(new View.ActionListener() {
                    @Override
                    public void onClicked(View v) {
                        //send one packet to reduce the bandwidth loss
                        iInventory.removeSlot(prefabSlot.getSlotIndex(),false);//this will not send a sync-packet
                        iInventory.removeSlot(timelineSlot.getSlotIndex(),true);
                    }
                });

                viewHolder.setIndex(index);
            }


        }
    }
}
