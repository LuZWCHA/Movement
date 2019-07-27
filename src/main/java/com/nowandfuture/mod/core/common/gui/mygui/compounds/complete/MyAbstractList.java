package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class MyAbstractList<T extends MyAbstractList.ViewHolder> extends View {

    private Adapter<T> adapter;

    // TODO: 2019/7/20 add cache to store viewholder
    private List<T> cache;

    private float scrollDistance = 0;
    private float spiltDistance = 1;

    public MyAbstractList(@Nonnull RootView rootView) {
        super(rootView);
        cache = new ArrayList<>();
    }

    public MyAbstractList(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    public void bind(Adapter adapter){
        this.adapter = adapter;
    }

    private int getDrawFirst(){
        float one = getAdapter().getHeight() + spiltDistance;
        return (int) (scrollDistance / one);
    }

    private int computeItemNum(){
        if(getContentLength() > getHeight()){
            float one = getAdapter().getHeight() + spiltDistance;
            return (int) (getHeight() / one + 2);
        }else{
            return getAdapter().getSize();
        }
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        Minecraft client = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(client);
        final double scaleW = client.displayWidth / res.getScaledWidth_double();
        final double scaleH = client.displayHeight / res.getScaledHeight_double();
        final int ax = getAbsoluteX();
        final int ay = getAbsoluteY();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(ax * scaleW), (int)(client.displayHeight - (ay + getHeight()) * scaleH),
                (int)(getWidth() * scaleW), (int)(getHeight() * scaleH));
        drawBackground();
        drawItems(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void drawBackground(){
        Gui.drawRect(0,0,getWidth(),getHeight(),DrawHelper.colorInt(0,0,0,130));
    }

    private void drawItems(int mouseX, int mouseY, float partialTicks){
        final float ceilLength = getAdapter().getHeight() + spiltDistance;
        final int startIndex = getDrawFirst();
        for (int index = startIndex; index < Math.min(startIndex + computeItemNum(),getAdapter().getSize());index++) {
            T viewHolder = getAdapter().createHolder();
            getAdapter().handle(viewHolder, index);

            float scrollItemLength = index * ceilLength;
            float offsetY = scrollItemLength - scrollDistance;

            final float actY = mouseY + scrollDistance;

            boolean isHover = false;

            if(actY > index * ceilLength && actY < ceilLength * index + getAdapter().getHeight() &&
                    mouseX > 0 && mouseX < getWidth() && mouseY <= getHeight() && mouseY >= 0){
                isHover = true;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0,offsetY,0);
            viewHolder.draw(this,mouseX, mouseY, partialTicks,isHover);
            drawSplitLine();
            GlStateManager.popMatrix();
        }
    }

    protected void drawSplitLine(){
        //do nothing
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        final float ceilLength = getAdapter().getHeight() + spiltDistance;

        float actY = mouseY + scrollDistance;
        int index = (int) (actY / ceilLength);

        if(actY > index * ceilLength && actY < ceilLength * index + getAdapter().getHeight()){
            onItemClicked(index);
            return true;
        }
        return false;
    }

    protected void onItemClicked(int index){

    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    public boolean handleMouseInput(int mouseX, int mouseY) {

        int scroll = Mouse.getEventDWheel();
        if (scroll != 0)
        {
            this.scrollDistance += (-1 * scroll / 120.0F) * this.getAdapter().getHeight() / 2;
        }

        fixScroll();
        return true;
    }

    public void fixScroll(){
        if(getContentLength() - getHeight() < 0){
            scrollDistance = 0;
            return;
        }

        if(scrollDistance > getContentLength() - getHeight()) {
            scrollDistance = getContentLength() - getHeight();
        } else if(scrollDistance < 0)
            scrollDistance = 0;
    }

    private float getContentLength(){
        return getAdapter().getHeight() * getAdapter().getSize() + spiltDistance * (getAdapter().getSize() - 1);
    }

    public Adapter<T> getAdapter() {
        if(adapter == null)
            throw new RuntimeException("adapter should bind first!");
        return adapter;
    }

    public static abstract class Adapter<T extends ViewHolder>{
        public abstract int getSize();
        public abstract int getHeight();
        public abstract T createHolder();
        public abstract void handle(T viewHolder,int index);
    }

    public static class ViewHolder{
        public void draw(MyAbstractList list,int mouseX, int mouseY, float partialTicks,boolean isHover){}
    }
}
