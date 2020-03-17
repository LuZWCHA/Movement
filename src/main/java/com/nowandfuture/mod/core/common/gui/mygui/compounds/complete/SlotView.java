package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//wrap slot to view,however,mouse's actions also be processed in GuiContainer;
//this proxy view only control its position, and as a holder to take up the place in viewgroup/rootview.
public class SlotView extends View {
    private AbstractContainer.ProxySlot slot;

    public SlotView(@Nonnull RootView rootView, AbstractContainer.ProxySlot slot) {
        super(rootView);
        this.slot = slot;
        setWidth(16);
        setHeight(16);
    }

    public SlotView(@Nonnull RootView rootView, ViewGroup parent, @Nullable AbstractContainer.ProxySlot slot) {
        super(rootView, parent);
        this.slot = slot;
        setWidth(16);
        setHeight(16);
    }

    public void setSlot(AbstractContainer.ProxySlot slot) {
        this.slot = slot;
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
        if(slot != null)
            slot.xPos = 0;
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        if(slot != null)
            slot.yPos = 0;
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        if(slot == null) return;
        drawRect(0,0,getWidth(),getHeight(), colorInt(255,255,255,128));
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
        if(slot != null)
            slot.setEnable(enable);
    }

    public boolean isEnable(){
        return slot == null ? false : slot.isEnabled();
    }

    public AbstractContainer.ProxySlot getSlot() {
        return slot;
    }
}
