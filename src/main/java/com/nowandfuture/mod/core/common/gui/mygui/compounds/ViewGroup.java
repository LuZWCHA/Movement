package com.nowandfuture.mod.core.common.gui.mygui.compounds;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.api.ISizeChanged;
import com.nowandfuture.mod.core.common.gui.mygui.api.MyGui;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ViewGroup extends Gui implements MyGui, ISizeChanged {

    private int width;
    private int height;
    private int x,y;

    private ViewGroup parent;
    private RootView root;
    protected List<ViewGroup> children;
    private boolean enableIntercepted = false;

    private ViewGroup lastPressedChild;
    private long lastPressTime;

    private boolean isFocused;
    private boolean visible = true;
    private boolean isHover = false;
    private boolean isClickable = true;
    private int[] pads = new int[4];

    private AbstractGuiContainer.ActionClick actionClick;

    protected boolean isScissor = false;

    private boolean isReachable = true;
    private boolean isInside;

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

    public void setReachable(boolean reachable) {
        isReachable = reachable;
    }

    public boolean isReachable() {
        return isReachable;
    }

    public ViewGroup getParent() {
        return parent;
    }

    public RootView getRoot() {
        return root;
    }

    public boolean isClickable() {
        return isClickable;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
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

    /**
     * @see AbstractGuiContainer#initGui() this method will be invoked only one time,even gui size changed
     * to re-layout the guis,to see {@link ViewGroup#onLayout(int, int)}
     */
    protected void onLoad(){
        for (ViewGroup view :
                children) {
            view.onLoad();
        }
    }

    /**
     * @param parentWidth its parent's width
     * @param parentHeight its parent's height
     * this method will be invoked when the gui's size changed: {@link ViewGroup#onWidthChanged(int, int)}
     *                     {@link ViewGroup#onHeightChanged(int, int)} and also be invoked before its children
     *                     to be layout {@link ViewGroup#onChildrenLayout()}
     */
    protected abstract void onLayout(int parentWidth,int parentHeight);

    protected void onChildrenLayout(){
        for (ViewGroup view :
                children) {
            view.layout(this.width,this.height);
        }
    }


    /**
     * @param old the old width
     * @param cur the new width
     */
    @Override
    public void onWidthChanged(int old, int cur) {
        if(getParent() != null)
            layout(getParent().getWidth(),getParent().getHeight());
        else
            layout(-1,-1);
    }

    /**
     * @param old the old height
     * @param cur the new height
     */
    @Override
    public void onHeightChanged(int old, int cur) {
        if(getParent() != null)
            layout(getParent().getWidth(),getParent().getHeight());
        else
            layout(-1,-1);
    }

    protected boolean checkMouseInside(int mouseX, int mouseY, float partialTicks){
        return isReachable && RootView.isInside(this,mouseX,mouseY);
    }

    /**
     * @param mouseX relative location-x at parent view
     * @param mouseY relative location-y at parent view
     * @param partialTicks
     * draw at root-view
     */
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        isInside = checkMouseInside(mouseX, mouseY, partialTicks);
        drawDebugHoverBackground();

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

            int tempX, tempY;
            for (ViewGroup view :
                    children) {
                if (view.isVisible()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(view.getX(), view.getY(), 0);
                    tempX = mouseX - view.getX();
                    tempY = mouseY - view.getY();
                    view.draw(tempX, tempY, partialTicks);
                    GlStateManager.popMatrix();
                }
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }else{
            onDraw(mouseX, mouseY, partialTicks);

            int tempX, tempY;
            for (ViewGroup view :
                    children) {
                if (view.isVisible()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(view.getX(), view.getY(), 0);
                    tempX = mouseX - view.getX();
                    tempY = mouseY - view.getY();
                    view.draw(tempX, tempY, partialTicks);
                    GlStateManager.popMatrix();
                }
            }
        }

    }

    protected ViewGroup checkHover(int mouseX, int mouseY){
        ViewGroup vg;
        int tempX,tempY;
        if(isVisible() &&
                checkParentHover() && checkMouseInside(mouseX,mouseY,0) ||
                !checkParentHover()) {
            for (int i = children.size(); i > 0; i--) {
                vg = children.get(i - 1);
                tempX = mouseX - vg.getX();
                tempY = mouseY - vg.getY();
                ViewGroup v = vg.checkHover(tempX,tempY);
                if(v != null){
                    return v;
                }
            }
            return this;
        }
        return null;
    }


    /**
     * @return true when checking whether it's hovering by mouse and also its parents are hovered
     *         false will check itself only.
     */
    protected boolean checkParentHover(){
        return true;
    }

    /**
     * @param mouseX absolute location-x at root view
     * @param mouseY absolute location-y at root view
     * @param partialTicks
     * draw at root-view
     */
    @Override
    public void draw2(int mouseX, int mouseY, float partialTicks) {

        if(isScissor) {
            final ScaledResolution res = new ScaledResolution(getRoot().context);
            final double scaleW = getRoot().context.displayWidth / res.getScaledWidth_double();
            final double scaleH = getRoot().context.displayHeight / res.getScaledHeight_double();
            final int ax = getAbsoluteX();
            final int ay = getAbsoluteY();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((int) (ax * scaleW), (int) (getRoot().context.displayHeight - (ay + getHeight()) * scaleH),
                    (int) (getWidth() * scaleW), (int) (getHeight() * scaleH));

            onDrawAtScreenCoordinate(mouseX, mouseY, partialTicks);

            for (ViewGroup view :
                    children) {
                if(view.isVisible()) {
                    view.draw2(mouseX, mouseY, partialTicks);
                }
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }else{
            onDrawAtScreenCoordinate(mouseX, mouseY, partialTicks);

            for (ViewGroup view :
                    children) {
                if(view.isVisible()) {
                    view.draw2(mouseX, mouseY, partialTicks);
                }
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
     * @see ViewGroup#onDraw(int, int, float) the diffierent between them just the Coordinate where
     * it drawing
     */
    protected void onDrawAtScreenCoordinate(int mouseX, int mouseY, float partialTicks){

    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(actionClick != null && visible) actionClick.clicked(this,mouseButton);

        return visible && onClicked(mouseX, mouseY, mouseButton);
    }

    protected abstract boolean onClicked(int mouseX, int mouseY, int mouseButton);

    @Override
    public boolean mouseLongClicked(int mouseX, int mouseY, int mouseButton) {
        return visible && onLongClicked(mouseX, mouseY, mouseButton);
    }

    protected abstract boolean onLongClicked(int mouseX, int mouseY, int mouseButton);

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if(!interceptClickAction(mouseX, mouseY, button)
                && lastPressedChild != null){
            mouseX -= lastPressedChild.getX();
            mouseY -= lastPressedChild.getY();
            lastPressedChild.mouseReleased(mouseX,mouseY,button);
        }else{
            onReleased(mouseX, mouseY, button);
            if(RootView.isInside(this,mouseX,mouseY)){
                if(Minecraft.getSystemTime() - lastPressTime < root.longClickThreshold)
                    mouseClicked(mouseX,mouseY,button);
                else{
                    if(actionClick != null) actionClick.longClick(this,button,Minecraft.getSystemTime() - lastPressTime);
                    mouseLongClicked(mouseX, mouseY, button);
                }
            }
        }
    }

    protected abstract void onReleased(int mouseX, int mouseY, int state);

    /**
     * @return true if it enable its parent intercept its {@link ViewGroup#onClicked(int, int, int)}
     */
    private boolean enableIntercepted(){
        return enableIntercepted;
    }

    protected boolean intercept(){
        return true;
    }

    protected boolean interceptClickAction(int mouseX, int mouseY, int button){
        boolean flag = visible && RootView.isInside(this,mouseX,mouseY)
                && onPressed(mouseX, mouseY, button);

        if(flag){
            flag = onInterceptClickAction(mouseX, mouseY, button);
            if(flag && lastPressedChild != null){
                if(!lastPressedChild.enableIntercepted()){
                    lastPressedChild.mouseReleased(mouseX - lastPressedChild.getX(),
                            mouseY - lastPressedChild.getY(), button);
                }
            }
        }
        return flag;
    }

    protected boolean onInterceptClickAction(int mouseX, int mouseY, int button){
        return false;
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY,int state) {
        boolean flag;

        if(lastPressedChild != null) {
            lastPressedChild.setLastPressTime(0);
            lastPressedChild = null;
        }

        int tempX,tempY;
        ViewGroup vg;
        for (int i = children.size();i > 0;i--) {
            vg = children.get(i - 1);
            tempX = mouseX - vg.getX();
            tempY = mouseY - vg.getY();
            if(!RootView.isInside(vg,tempX,tempY) || !vg.isClickable()) continue;

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
        ViewGroup vg;
        for (int i = children.size();i > 0;i--) {
            vg = children.get(i - 1);

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

    /**
     * this method is executed in {@link AbstractGuiContainer#updateScreen()} every tick
     */
    public void onUpdate(){
        for (ViewGroup vg:
                children){
            if(vg.isVisible()){
                vg.onUpdate();
            }
        }
    }

    public boolean handleKeyType(char typedChar, int keyCode){
        ViewGroup vg;
        for (int i = children.size();i > 0;i--) {
            vg = children.get(i - 1);
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

    //public click event,visible is not affect
    public void performClickAction(int x,int y,int button){
        onClicked(x, y, button);
    }

    public long getLastPressTime() {
        return lastPressTime;
    }

    public final void setLastPressTime(long lastPressTime) {
        this.lastPressTime = lastPressTime;
    }

    public void addChild(ViewGroup viewGroup){
        viewGroup.parent = this;
        children.add(viewGroup);
    }

    public void addChildren(ViewGroup... viewGroup){
        for (ViewGroup v :
                viewGroup) {
            addChild(v);
        }
    }

    public void addChild(int index ,ViewGroup viewGroup){
        viewGroup.parent = this;
        children.add(index,viewGroup);
    }

    public void removeChild(ViewGroup viewGroup){
        viewGroup.parent = null;
        children.remove(viewGroup);
    }

    public void removeChild(int index){
        children.remove(index).parent = null;
    }

    public void removeAllChildren(){
        for (ViewGroup v :
                children) {
            v.parent = null;
        }

        children.clear();
    }

    public void addAll(Collection<ViewGroup> viewGroups){
        for (ViewGroup v :
                viewGroups) {
            addChild(v);
        }
    }

    public void forEach(Consumer<? super ViewGroup> consumer) {
        children.forEach(consumer);
    }

    public ViewGroup getChild(int index){
        return children.get(index);
    }

    public int getChildrenSize(){
        return children.size();
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
            setFocused(false);
        }
        for (ViewGroup vg :
                children) {
            vg.setVisible(visible);
        }
    }

    public boolean isHovering() {
        return isHover;
    }

    public void setHovering(boolean hover) {
        isHover = hover;
    }

    /**
     * @param isInsideParents same as {@link ViewGroup#checkParentHover()}
     * @return true if it's hovering
     *
     * the difference between this and {@link ViewGroup#isHovering()} is that hovering will only allow one
     * view being hovered,if its children being hovered,{@link ViewGroup#isHovering()} will return false.
     *In anther world, {@link ViewGroup#isHovering()} only check the top view in the rootView.
     */
    public boolean isMouseover(boolean isInsideParents){
        if(isInsideParents && getParent() != null){
            return isInside && getParent().isMouseover(true);
        }else{
            return isInside;
        }
    }

    /**
     * @param scissor whether it's children will be scissored by it
     */
    public void setScissor(boolean scissor) {
        isScissor = scissor;
    }

    public void setEnableIntercepted(boolean value){
        enableIntercepted = value;
    }

    public void childrenEnableIntercepted(boolean value){
        forEach(new Consumer<ViewGroup>() {
            @Override
            public void accept(ViewGroup viewGroup) {
                viewGroup.setEnableIntercepted(value);
            }
        });
    }

    public static class Builder{
        ViewGroup viewGroup;

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

    protected void drawDebugHoverBackground(){
        if(getRoot().isShowDebugInfo() && isMouseover(true))
            drawRect(0,0,getWidth(),getHeight(),colorInt(100,100,0,100));
    }

    //--------------------------------------tools----------------------------------------------------

    public int colorInt(int r,int g,int b,int a){
        a = (a & 255) << 24;
        r = (r & 255) << 16;
        g = (g & 255) << 8;
        b &= 255;
        return a | r | g | b;
    }

    public int colorInt(Color color){
        return colorInt(color.getRed(),color.getGreen(),color.getBlue(),color.getAlpha());
    }

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
