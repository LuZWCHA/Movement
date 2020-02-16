package com.nowandfuture.mod.core.common.gui.mygui.compounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public abstract class View extends ViewGroup {

    private boolean isScissor = true;

    public View(@Nonnull RootView rootView) {
        super(rootView);
    }

    public View(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    @Override
    public final void layout(int parentWidth, int parentHeight) {
        super.layout(parentWidth, parentHeight);
    }

    @Override
    protected final void onChildrenLayout() {
        //do nothing
    }

    @Override
    public void onLayout(int parentWidth, int parentHeight) {

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return true;
    }

    @Override
    public boolean handleKeyType(char typedChar, int keyCode) {
        return super.handleKeyType(typedChar, keyCode);
    }

    @Override
    public boolean onKeyType(char typedChar, int keyCode) {
        return false;
    }

    /**
     * this method would execute at the first time of the view been load (at parent view's onLoad())
     * and it only execute one time on a view's life
     */
    @Override
    protected void onLoad() {

    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public boolean handleMouseInput(int mouseX, int mouseY) {
        return false;
    }

    @Override
    public final void draw(int mouseX, int mouseY, float partialTicks) {
        if(isScissor) {
            final ScaledResolution res = new ScaledResolution(getRoot().context);
            final double scaleW = getRoot().context.displayWidth / res.getScaledWidth_double();
            final double scaleH = getRoot().context.displayHeight / res.getScaledHeight_double();
            final int ax = getAbsoluteX();
            final int ay = getAbsoluteY();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((int) (ax * scaleW), (int) (getRoot().context.displayHeight - (ay + getHeight()) * scaleH),
                    (int) (getWidth() * scaleW), (int) (getHeight() * scaleH));
            onDraw(mouseX, mouseY, partialTicks);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }else{
            onDraw(mouseX, mouseY, partialTicks);
        }
    }


    /**
     * @param scissor whether it will be scissored by parent
     *                if it set true,this view may scissor by its parents/parent(if this view is out of this parents)
     *                else not be scissored
     */
    public void setScissor(boolean scissor) {
        isScissor = scissor;
    }


    interface ClickListener{
        void onClicked(View v);
        void onLongClicked(View v);
    }

    public static abstract class ActionListener implements ClickListener{
        @Override
        public void onClicked(View v) {

        }

        @Override
        public void onLongClicked(View v) {

        }
    }
}
