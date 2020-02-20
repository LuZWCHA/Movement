package com.nowandfuture.mod.core.common.gui.mygui.compounds;

import com.google.common.collect.Lists;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.ISizeChanged;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.nio.Buffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class ViewGroup extends Gui implements MyGui, ISizeChanged {

    private int width;
    private int height;
    private int x,y;

    private ViewGroup parent;
    private RootView root;
    protected List<ViewGroup> children;

    private ViewGroup lastPressedChild;
    private long lastPressTime;

    private boolean isFocused;
    private boolean visible = true;
    private int[] pads = new int[4];

    private AbstractGuiContainer.ActionClick actionClick;

    protected ViewGroup(){

    }

    public ViewGroup(@Nonnull RootView rootView){
        this(rootView,rootView.getTopView());
    }

    public ViewGroup(@Nonnull RootView rootView, ViewGroup parent){
        this.children = new LinkedList<>();
        this.parent = parent;
        this.root = rootView;
    }

    public ViewGroup getParent() {
        return parent;
    }

    public RootView getRoot() {
        return root;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public int getX() {
        return x;
    }


    /**
     * @return get absolute location at root view
     */
    public int getAbsoluteX(){
        final ScaledResolution res = new ScaledResolution(getRoot().context);

        if(parent != null)
            return parent.getAbsoluteX() + x;
        else
            return x;
    }

    /**
     * @return get absolute location at root view
     */
    public int getAbsoluteY(){
        final ScaledResolution res = new ScaledResolution(getRoot().context);

        if(parent != null)
            return parent.getAbsoluteY() + y;
        else
            return y;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setWidth(int width) {
        if(width != this.width) {
            int temp = this.width;
            this.width = width;
            onWidthChanged(temp,width);
        }
    }

    @Override
    public void setHeight(int height) {
        if(height != this.height) {
            int temp = this.height;
            this.height = height;
            onHeightChanged(temp,height);
        }
    }

    public void layout(int parentWidth,int parentHeight){
        onLayout(parentWidth, parentHeight);
        onChildrenLayout();
    }

    protected void onLoad(){
        for (ViewGroup view :
                children) {
            view.onLoad();
        }
    }

    protected abstract void onLayout(int parentWidth,int parentHeight);

    protected void onChildrenLayout(){
        for (ViewGroup view :
                children) {
            view.layout(this.width,this.height);
        }
    }

    @Override
    public void onWidthChanged(int old, int cur) {
        if(getParent() != null)
            layout(getParent().getWidth(),getParent().getHeight());
        else
            layout(-1,-1);
    }

    @Override
    public void onHeightChanged(int old, int cur) {
        if(getParent() != null)
            layout(getParent().getWidth(),getParent().getHeight());
        else
            layout(-1,-1);
    }

    /**
     * @param mouseX relative location-x at parent view
     * @param mouseY relative location-y at parent view
     * @param partialTicks
     * draw at root-view
     */
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        onDraw(mouseX, mouseY, partialTicks);

        int tempX, tempY;
        for (ViewGroup view :
                children) {
            if(view.isVisible()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(view.getX(), view.getY(), 0);
                tempX = mouseX - view.getX();
                tempY = mouseY - view.getY();
                view.draw(tempX, tempY, partialTicks);
                GlStateManager.popMatrix();
            }
        }

    }

    /**
     * @param mouseX absolute location-x at root view
     * @param mouseY absolute location-y at root view
     * @param partialTicks
     * draw at root-view
     */
    @Override
    public void draw2(int mouseX, int mouseY, float partialTicks) {
        onDrawAtScreenCoordinate(mouseX, mouseY, partialTicks);

        for (ViewGroup view :
                children) {
            if(view.isVisible()) {
                view.draw2(mouseX, mouseY, partialTicks);
            }
        }
    }

    /**
     * @param mouseX relative location-x at parent view
     * @param mouseY relative location-y at parent view
     * @param partialTicks
     * draw at root-view
     */
    protected abstract void onDraw(int mouseX, int mouseY, float partialTicks);

    /**
     * @param mouseX absolute location-x at root view
     * @param mouseY absolute location-y at root view
     * @param partialTicks
     * draw at root-view
     */
    protected void onDrawAtScreenCoordinate(int mouseX, int mouseY, float partialTicks){

    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(actionClick != null) actionClick.clicked(this,mouseButton);

        return visible && onClicked(mouseX, mouseY, mouseButton);
    }

    protected abstract boolean onClicked(int mouseX, int mouseY, int mouseButton);

    @Override
    public boolean mouseLongClicked(int mouseX, int mouseY, int mouseButton) {
        return visible && onLongClicked(mouseX, mouseY, mouseButton);
    }

    protected abstract boolean onLongClicked(int mouseX, int mouseY, int mouseButton);

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if(lastPressedChild != null){
            mouseX -= lastPressedChild.getX();
            mouseY -= lastPressedChild.getY();
            lastPressedChild.mouseReleased(mouseX,mouseY,state);
        }else{
            onReleased(mouseX, mouseY, state);
            if(RootView.isInside(this,mouseX,mouseY)){
                if(Minecraft.getSystemTime() - lastPressTime < root.longClickThreshold)
                    mouseClicked(mouseX,mouseY,state);
                else{
                    if(actionClick != null) actionClick.longClick(this,state,Minecraft.getSystemTime() - lastPressTime);
                    mouseLongClicked(mouseX, mouseY, state);
                }
            }
        }
    }

    protected abstract void onReleased(int mouseX, int mouseY, int state);

    @Override
    public boolean mousePressed(int mouseX, int mouseY,int state) {
        boolean flag;

        if(lastPressedChild != null) {
            lastPressedChild.setLastPressTime(0);
            lastPressedChild = null;
        }

        int tempX,tempY;
        for (ViewGroup vg :
                children) {
            tempX = mouseX - vg.getX();
            tempY = mouseY - vg.getY();
            if(!RootView.isInside(vg,tempX,tempY)) continue;

            flag = vg.mousePressed(tempX, tempY,state);
            if (flag) {
                lastPressedChild = vg;
                vg.setLastPressTime(Minecraft.getSystemTime());
                return true;
            }
        }
        flag = visible && onPressed(mouseX, mouseY, state);
        if(flag && this != root.getFocusedView())
            root.setFocusedView(this);
        return flag;
    }

    protected abstract boolean onPressed(int mouseX, int mouseY,int state);

    protected boolean mousePressedMove(int mouseX, int mouseY, int state){
        int tempX,tempY;
        if(lastPressedChild != null)
            for (ViewGroup vg :
                    children) {

                tempX = mouseX - vg.getX();
                tempY = mouseY - vg.getY();
                if(!RootView.isInside(vg,tempX,tempY)) continue;

                if(lastPressedChild == vg && vg.mousePressedMove(tempX, tempY,state)){
                    return true;
                }
            }
        return onMousePressedMove(mouseX, mouseY, state);
    }

    protected boolean onMousePressedMove(int mouseX, int mouseY, int state){
        return false;
    }

    @Override
    public boolean handleMouseInput(int mouseX, int mouseY) {
        int tempX,tempY;
        boolean flag;

        for (ViewGroup vg :
                children) {

            tempX = mouseX - vg.getX();
            tempY = mouseY - vg.getY();
            if(!RootView.isInside(vg,tempX,tempY)) continue;

            flag = visible && vg.handleMouseInput(tempX, tempY);
            if (flag) {
                return true;
            }
        }

        return false;
    }

    public boolean onKeyType(char typedChar, int keyCode){
        return false;
    }

    public void onUpdate(){
        for (ViewGroup vg:
                children){
            if(vg.isVisible()){
                vg.onUpdate();
            }
        }
    }

    public boolean handleKeyType(char typedChar, int keyCode){
        for (ViewGroup vg:
                children){
            if(vg.handleKeyType(typedChar, keyCode)){
                return true;
            }
        }
        return isFocused && onKeyType(typedChar, keyCode);
    }

    public void focused(){

    }

    public void loseFocus(){

    }

    public long getLastPressTime() {
        return lastPressTime;
    }

    public void setLastPressTime(long lastPressTime) {
        this.lastPressTime = lastPressTime;
    }

    public void addChild(ViewGroup viewGroup){
        children.add(viewGroup);
    }

    public void addChildren(ViewGroup... viewGroup){
        children.addAll(Lists.newArrayList(viewGroup));
    }

    public void addChild(int index ,ViewGroup viewGroup){
        children.add(index,viewGroup);
    }

    public void removeChild(ViewGroup viewGroup){
        children.remove(viewGroup);
    }

    public void removeChild(int index){
        children.remove(index);
    }

    public void addAll(Collection<ViewGroup> viewGroups){
        children.addAll(viewGroups);
    }


    /**
     * when GUI close
     */
    public void destroy(){
        for (ViewGroup view :
                children) {
            view.destroy();
        }
        children.clear();
    }

    public void setActionClick(AbstractGuiContainer.ActionClick actionClick) {
        this.actionClick = actionClick;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setFocused(boolean focused) {
        if(isFocused != focused) {
            isFocused = focused;
            if(focused){
                this.focused();
            }else {
                this.loseFocus();
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if(isFocused && !visible){
            if(root.getFocusedView() == this){
                root.setFocusedView(null);
            }
            this.loseFocus();
        }
        for (ViewGroup vg :
                children) {
            vg.setVisible(visible);
        }
    }


    public static class Builder{
        ViewGroup viewGroup;

        private Builder(RootView rootView,Class<? extends ViewGroup> clazz){
            viewGroup = rootView.createInstance(clazz);
        }

        public static Builder newBuilder(RootView rootView,Class<? extends ViewGroup> clazz){
            return new Builder(rootView,clazz);
        }

        public Builder setX(int x){
            viewGroup.setX(x);
            return this;
        }

        public Builder setY(int y){
            viewGroup.setY(y);
            return this;
        }

        public Builder setWidth(int width){
            viewGroup.setWidth(width);
            return this;
        }

        public Builder setHeight(int height){
            viewGroup.setHeight(height);
            return this;
        }

        public Builder setVisible(boolean visible){
            viewGroup.setVisible(visible);
            return this;
        }

        public Builder setFocused(boolean focused){
            viewGroup.setFocused(focused);
            return this;
        }

        public ViewGroup build(){
            return viewGroup;
        }
    }

    protected void drawDebugInfo(int x,int y){
        if(getRoot().isShowDebugInfo()){
            Gui.drawRect(x,y,x + 1,y - 1,DrawHelper.colorInt(255,255,255,255));
            drawString(getRoot().getFontRenderer(),"(" + x + "," + y + ")",x,y - getRoot().getFontRenderer().FONT_HEIGHT,
                    DrawHelper.colorInt(255,255,255,255));
        }
    }

    //--------------------------------------tools----------------------------------------------------

    public void drawString3D(String s, float x, float y, float z, int r, int g, int b, int a, com.nowandfuture.mod.utils.math.Vector3f vector3f){
        drawString3D(s, x, y, z, r, g, b, a,new Vector3f(0,0,1));
    }

    public void drawString3D(String s, float x, float y , float z, int r, int g, int b, int a,Vector3f nomal){
        drawString3D(s, x, y, z, r, g, b, a,nomal,0.05f);
    }
    public void drawString3D(String s, float x, float y , float z, int r, int g, int b, int a, Vector3f nomal,float scale){
        GlStateManager.pushMatrix();
        GlStateManager.translate(x,y,z);
        GlStateManager.glNormal3f(nomal.x,nomal.y,nomal.z);
        GlStateManager.scale(scale,scale,1);
        GlStateManager.translate(0,getRoot().context.fontRenderer.FONT_HEIGHT,0);
        GlStateManager.rotate(180,1,0,0);
        drawString(getRoot().context.fontRenderer,s,0,(int)0,DrawHelper.colorInt(r,g,b,a));
        GlStateManager.popMatrix();
    }
}
