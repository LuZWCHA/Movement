package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.AbstractLayout;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class MyAbstractList<T extends MyAbstractList.ViewHolder> extends ViewGroup {

    private Adapter<T> adapter;

    private Color itemBackground,hoverBackground,selectedBackground;

    private List<T> cache;

    private float scrollDistance = 0;
    private float spiltDistance = 1;

    private boolean isEnableAutoScrolling = true;
    private float expectScrollDistance = 0;
    //animation frame num
    private int autoScrollFrame = 15;
    private boolean isAutoScrolling = false;

    private SelectMode mode = SelectMode.SINGLE;
    private Set<Integer> selectIndexList;

    public void setItemBackground(Color itemBackground) {
        this.itemBackground = itemBackground;
    }

    public void setHoverBackground(Color hoverBackground) {
        this.hoverBackground = hoverBackground;
    }

    public void setSelectedBackground(Color selectedBackground) {
        this.selectedBackground = selectedBackground;
    }

    enum  SelectMode{
        SINGLE,
        MULTI,
        NONE
    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return true;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    public MyAbstractList(@Nonnull RootView rootView) {
        super(rootView);
        init();
    }

    public MyAbstractList(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        init();
    }

    private void init(){
        cache = new ArrayList<>();
        setScissor(true);
        setClickable(true);
        selectIndexList = new HashSet<>();
        itemBackground = new Color(255,255,255,128);
        hoverBackground = new Color(128,128,128,180);
        selectedBackground = new Color(200,200,200,200);
    }

    public boolean isSelected(int index){
        return selectIndexList.contains(index);
    }

    public void bind(@Nonnull Adapter adapter){
        this.adapter = adapter;
    }

    public void setAutoScrollFrameNum(int num) {
        this.autoScrollFrame = num;
    }

    public void setEnableAutoScrolling(boolean enableAutoScrolling) {
        isEnableAutoScrolling = enableAutoScrolling;
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
        if(!getAdapter().isEnable()) return;

        final float ceilLength = getAdapter().getHeight() + spiltDistance;
        float diff = scrollDistance + getHeight() - ceilLength * getAdapter().getSize();

        //animation of scrolling
        if(diff > 0){
            if(isEnableAutoScrolling) {
                if(isAutoScrolling) {
                    if(diff > expectScrollDistance * autoScrollFrame){
                        expectScrollDistance = diff / autoScrollFrame;
                    }
                    scrollDistance = Math.max(0, scrollDistance - expectScrollDistance);
                }else{
                    isAutoScrolling = true;
                    expectScrollDistance = diff / scrollDistance;
                }
            }else{
                scrollDistance = Math.max(0, scrollDistance - diff);
            }
        }else{
            isAutoScrolling = false;
            expectScrollDistance = 0;
        }

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
                viewHolder = getAdapter().createHolder(getRoot(),this);
                cache.add(viewHolder);
            }
            viewHolder = cache.get(index - startIndex);

            getAdapter().handleIn(this,viewHolder, index);

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

                if(mode == SelectMode.SINGLE){
                    if(!selectIndexList.isEmpty()){
                        selectIndexList.clear();
                    }
                    selectIndexList.add(index);
                }else if(mode == SelectMode.MULTI){

                    if(selectIndexList.contains(index)){
                        selectIndexList.remove(index);
                    }else{
                        selectIndexList.add(index);
                    }
                }else{
                    selectIndexList.clear();
                }

                onItemClicked(index, mouseX, mouseY);

                T viewHolder;
                int startIndex = getDrawFirst();
                while (cache.size() <= index - startIndex) {
                    viewHolder = getAdapter().createHolder(getRoot(),this);
                    cache.add(viewHolder);
                }
                viewHolder = cache.get(index - startIndex);

                getAdapter().handleIn(this,viewHolder, index);
                viewHolder.onClicked(mouseX, mouseY, mouseButton);
            }
        }
        return true;
    }

    protected void onItemClicked(int index,int mouseX,int mouseY){

    }

    @Override
    public void focused() {
        super.focused();
    }

    @Override
    public void loseFocus() {
        super.loseFocus();
        selectIndexList.clear();
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
            if(isAutoScrolling && isEnableAutoScrolling){
                isAutoScrolling = false;
                expectScrollDistance = 0;
            }
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
        public abstract T createHolder(RootView rootView,MyAbstractList parent);
        private void handleIn(MyAbstractList parent,T viewHolder,int index){
            viewHolder.setIndex(index);
            handle(parent, viewHolder, index);
        }
        public abstract void handle(MyAbstractList parent,T viewHolder,int index);
        public boolean isEnable(){return true;}
    }

    public static class ViewHolder extends AbstractLayout {
        private MyAbstractList list;
        private int index;
        private Object tag;

        public ViewHolder(@Nonnull RootView rootView, MyAbstractList parent) {
            super(rootView, parent);
            this.list = parent;
        }

        public ViewHolder(@Nonnull RootView rootView, MyAbstractList parent, @Nonnull List list) {
            super(rootView, parent, list);
            this.list = parent;
        }

        void setIndex(int index){
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        @Override
        protected void onDraw(int mouseX, int mouseY, float partialTicks) {
            if(isMouseover(true)){
                setBackgroundColor(list.hoverBackground);
            }else{
                if(list.isSelected(this.index)){
                    setBackgroundColor(list.selectedBackground);
                }else
                    setBackgroundColor(list.itemBackground);
            }
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
            return false;
        }

        public Object getTag() {
            return tag;
        }

        public void setTag(Object tag) {
            this.tag = tag;
        }
    }
}
