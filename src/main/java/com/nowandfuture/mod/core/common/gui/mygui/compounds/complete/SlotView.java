package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.google.common.collect.Lists;
import com.nowandfuture.mod.core.common.gui.DynamicInventory;
import com.nowandfuture.mod.core.common.gui.IDynamicInventory;
import com.nowandfuture.mod.core.common.gui.ItemStackMapHelper;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//wrap slot to view,however,mouse's actions also be processed in GuiContainer;
//this proxy view only control its position, and as a holder to take up the place in viewgroup/rootview.
public class SlotView extends View {
    private AbstractContainer.ProxySlot slot;

    public SlotView(@Nonnull RootView rootView, AbstractContainer.ProxySlot slot) {
        super(rootView);
        this.slot = slot;
        slot.setEnable(false);
        setWidth(16);
        setHeight(16);
    }

    public SlotView(@Nonnull RootView rootView, ViewGroup parent, AbstractContainer.ProxySlot slot) {
        super(rootView, parent);
        this.slot = slot;
        slot.setEnable(false);
        setWidth(16);
        setHeight(16);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
    }

    @Override
    public final void setWidth(int width) {
        super.setWidth(width);
    }

    @Override
    public final void setHeight(int height) {
        super.setHeight(height);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        slot.xPos = 0;
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        slot.yPos = 0;
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        drawRect(0,0,getWidth(),getHeight(), DrawHelper.colorInt(255,255,0,255));
        slot.setEnable(true);

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        getRoot().getGuiContainer().renderSlot(slot);

        //highlight
        if(slot.isEnabled() && isHover()) {
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            this.drawGradientRect(0, 0, getWidth(), getHeight(), -2130706433, -2130706433);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }

        RenderHelper.disableStandardItemLighting();
        slot.setEnable(false);
    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return super.onPressed(mouseX, mouseY, state);
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return true;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public void setEnable(boolean enable){
        slot.setEnable(enable);
    }

    public boolean isEnable(){
        return slot.isEnabled();
    }

    public AbstractContainer.ProxySlot getSlot() {
        return slot;
    }
}
