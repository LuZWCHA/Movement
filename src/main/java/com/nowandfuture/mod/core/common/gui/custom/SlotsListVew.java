package com.nowandfuture.mod.core.common.gui.custom;

import com.nowandfuture.mod.core.common.gui.IDynamicInventory;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.MyAbstractList;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.SlotView;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.util.Color;

import javax.annotation.Nonnull;
import java.util.Map;

public class SlotsListVew extends MyAbstractList<MyAbstractList.ViewHolder> {

    public SlotsListVew(@Nonnull RootView rootView) {
        super(rootView);
    }

    public SlotsListVew(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    @Override
    protected void drawSplitLine() {
        drawHorizontalLine(0,getWidth(),0, DrawHelper.colorInt(255,0,0,255));
    }

    static class SlotsViewHolder extends ViewHolder{

        public SlotsViewHolder(RootView rootView){
            super(rootView);
            AbstractGuiContainer.GuiBuilder.wrap(this)
                    .setX(0).setY(0).setWidth(100).setHeight(20).build();
        }

        @Override
        protected void onDraw(int mouseX, int mouseY, float partialTicks) {
            drawBackground();
        }

        @Override
        public boolean onClicked(int mouseX, int mouseY, int mouseButton) {
            for(int i = getChildrenSize();i>0;i--){
                if(RootView.isInside2(getChild(i - 1),mouseX,mouseY) &&
                        getChild(i - 1).mouseClicked(mouseX, mouseY, mouseButton))
                    return true;
            }
            return false;
        }

        public void setSlots(SlotView prefabSlot,SlotView timelineSlot) {
            removeAllChildren();
            prefabSlot.setScissor(false);
            timelineSlot.setScissor(false);
            addChildren(prefabSlot,timelineSlot);
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
            return iInventory.getSizeInventory();
        }

        @Override
        public int getHeight() {
            return 20;
        }

        @Override
        public SlotsViewHolder createHolder(RootView rootView) {
            return new SlotsViewHolder(rootView);
        }

        @Override
        public void handle(MyAbstractList list,SlotsViewHolder viewHolder, int index) {
            Map.Entry<Long, ItemStack> entry = iInventory.getEntryByIndex(index);
            long id = entry.getKey();
            AbstractContainer.ProxySlot slot = iInventory.getSlots().get(id);
            if(slot != null){
                SlotView psv = new SlotView(list.getRoot(),list, slot);
                SlotView tlv = new SlotView(list.getRoot(),list, slot);
                int offsetY = (getHeight() - psv.getHeight())/2;
                psv.setX(0);
                psv.setY(offsetY);
                tlv.setX(20);
                tlv.setY(offsetY);

                viewHolder.setSlots(psv,tlv);
            }

        }
    }
}
