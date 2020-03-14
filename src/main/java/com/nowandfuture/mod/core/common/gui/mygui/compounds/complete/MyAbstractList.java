package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.AbstractLayout;
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
import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class MyAbstractList<T extends MyAbstractList.ViewHolder> extends ViewGroup {

    private Adapter<T> adapter;

    private Color itemBackground,hoverBackground;

    private List<T> cache;

    private float scrollDistance = 0;
    private float spiltDistance = 1;

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return true;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    public MyAbstractList(@Nonnull RootView rootView) {
        super(rootView);
        cache = new ArrayList<>();
        setScissor(true);
        setClickable(true);
    }

    public MyAbstractList(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        cache = new ArrayList<>();
        setScissor(true);
        setClickable(true);
    }

    public void bind(@Nonnull Adapter adapter){
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
        drawBackground();
        final float ceilLength = getAdapter().getHeight() + spiltDistance;
        final int startIndex = getDrawFirst();
        for (int index = startIndex; index < Math.min(startIndex + computeItemNum(),getAdapter().getSize());index++) {

            float scrollItemLength = index * ceilLength;
            float offsetY = scrollItemLength - scrollDistance;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0,getAdapter().getHeight() + offsetY,0);
            drawSplitLine();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void onLayout(int parentWidth, int parentHeight) {
        layoutItems();
    }

    private void drawBackground(){
        Gui.drawRect(0,0,getWidth(),getHeight(),DrawHelper.colorInt(0,0,0,130));
    }

    private void layoutItems(){
        final float ceilLength = getAdapter().getHeight() + spiltDistance;
        final int startIndex = getDrawFirst();
        removeAllChildren();
        for (int index = startIndex; index < Math.min(startIndex + computeItemNum(),getAdapter().getSize());index++) {
            T viewHolder;
            while (cache.size() <= index - startIndex){
                viewHolder = getAdapter().createHolder(getRoot());
                cache.add(viewHolder);
            }
            viewHolder = cache.get(index - startIndex);

            getAdapter().handle(this,viewHolder, index);

            float scrollItemLength = index * ceilLength;
            float offsetY = scrollItemLength - scrollDistance;

            viewHolder.setY((int) offsetY);
            viewHolder.setX(0);
            viewHolder.setWidth(getWidth());
            viewHolder.setHeight(getAdapter().getHeight());
            addChild(viewHolder);
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
        if(index >= 0 && index < getAdapter().getSize()) {
            if (actY > index * ceilLength && actY < ceilLength * index + getAdapter().getHeight()) {
                mouseY = (int) (actY - index * ceilLength);
                onItemClicked(index, mouseX, mouseY);

                T viewHolder;
                int startIndex = getDrawFirst();
                while (cache.size() <= index - startIndex) {
                    viewHolder = getAdapter().createHolder(getRoot());
                    cache.add(viewHolder);
                }
                viewHolder = cache.get(index - startIndex);

                getAdapter().handle(this,viewHolder, index);
                viewHolder.onClicked(mouseX, mouseY, mouseButton);
            }
        }
        return true;
    }

    protected void onItemClicked(int index,int mouseX,int mouseY){

    }

    @Override
    public boolean onKeyType(char typedChar, int keyCode) {
        return false;
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

    @Override
    public void destroy() {
        super.destroy();
        cache.clear();
    }

    public static abstract class Adapter<T extends ViewHolder>{
        public abstract int getSize();
        public abstract int getHeight();
        public abstract T createHolder(RootView rootView);
        public abstract void handle(MyAbstractList parent,T viewHolder,int index);
    }

    public static class ViewHolder extends AbstractLayout {

        public ViewHolder(@Nonnull RootView rootView) {
            super(rootView);
        }

        public ViewHolder(@Nonnull RootView rootView, ViewGroup parent) {
            super(rootView, parent);
        }

        public ViewHolder(@Nonnull RootView rootView, ViewGroup parent, @Nonnull List list) {
            super(rootView, parent, list);
        }

        @Override
        protected void onDraw(int mouseX, int mouseY, float partialTicks) {
            drawBackground();
        }

        @Override
        protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
            return false;
        }

        @Override
        protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
            return false;
        }

        @Override
        protected void onReleased(int mouseX, int mouseY, int state) {

        }

        @Override
        protected boolean onPressed(int mouseX, int mouseY, int state) {
            return true;
        }
    }
}
