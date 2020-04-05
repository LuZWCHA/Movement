package com.nowandfuture.mod.core.common.gui.mygui.compounds;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.api.ISizeChanged;
import com.nowandfuture.mod.core.common.gui.mygui.api.MyGui;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
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

    private boolean isLoaded;
    private AbstractGuiContainer.GuiRegion rectangleRegion;

    private AbstractGuiContainer.ActionClick actionClick;

    //this scissor can only do not in nested-mode
    protected boolean isClipping = false;
    private static int stencilMaskDepth = -1;

    private boolean isReachable = true;
    private boolean isInside;

    private ViewClipMask viewClipMask;

    public int getPadLeft() {
        return padLeft;
    }

    public void setPadLeft(int padLeft) {
        this.padLeft = padLeft;
    }

    public int getPadRight() {
        return padRight;
    }

    public void setPadRight(int padRight) {
        this.padRight = padRight;
    }

    public int getPadTop() {
        return padTop;
    }

    public void setPadTop(int padTop) {
        this.padTop = padTop;
    }

    public int getPadBottom() {
        return padBottom;
    }

    public void setPadBottom(int padBottom) {
        this.padBottom = padBottom;
    }

    protected int padLeft,padRight,padTop,padBottom;

    protected ViewGroup(){
        isLoaded = false;
    }

    public ViewGroup(@Nonnull RootView rootView){
        this(rootView,rootView.getTopView());
    }

    public ViewGroup(@Nonnull RootView rootView, ViewGroup parent){
        this.children = new LinkedList<>();
        this.parent = parent;
        this.root = rootView;
        this.isLoaded = false;
        padBottom = padLeft = padRight = padTop = 0;
        viewClipMask = new RectangleClipMask(this);
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
        if(isLoaded) {
            onLayout(parentWidth, parentHeight);
            onChildrenLayout();
        }
    }

    public void load(){
        onLoad();
        this.isLoaded = true;
        for (ViewGroup view :
                children) {
            view.load();
        }
    }

    /**
     * @see AbstractGuiContainer#initGui() this method will be invoked only one time,even gui size changed
     * to re-layout the guis,to see {@link ViewGroup#onLayout(int, int)}
     */
    protected void onLoad(){

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
        if(isClipping) {

            stencilMaskDepth ++;
            int mask_layer = (0x01 << stencilMaskDepth);
            int layer = (mask_layer - 1) | mask_layer;

            int currentFunc = GL11.glGetInteger(GL11.GL_STENCIL_FUNC);
            int currentOpFailed = GL11.glGetInteger(GL11.GL_STENCIL_FAIL);
            int currentOpZPass = GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_PASS);
            int currentOpZFailed = GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_FAIL);
            int currentRef = GL11.glGetInteger(GL11.GL_STENCIL_REF);
            int currentMask = GL11.glGetInteger(GL11.GL_STENCIL_WRITEMASK);
            int currentValueMask = GL11.glGetInteger(GL11.GL_STENCIL_VALUE_MASK);
            boolean currentEnable = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);

            GL11.glEnable(GL11.GL_STENCIL_TEST);

            GL11.glStencilMask(mask_layer);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

            GL11.glStencilFunc(GL11.GL_NEVER,mask_layer,mask_layer);
            GL11.glStencilOp(GL11.GL_REPLACE,GL11.GL_KEEP,GL11.GL_KEEP);

            viewClipMask.drawMask();

            GL11.glStencilFunc(GL11.GL_EQUAL, layer, layer);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

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

//            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            stencilMaskDepth --;
            GL11.glStencilFunc(currentFunc,currentRef,currentValueMask);
            GL11.glStencilOp(currentOpFailed,currentOpZFailed,currentOpZPass);
            GL11.glStencilMask(currentMask);
            if(!currentEnable) {
                GL11.glDisable(GL11.GL_STENCIL_TEST);
            }

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
     * @param clipping whether it's children will be scissored by it
     */
    public void setClipping(boolean clipping) {
        isClipping = clipping;
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

    public void setViewClipMask(@Nonnull ViewClipMask viewClipMask) {
        this.viewClipMask = viewClipMask;
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

    public static void drawCenteredStringWithoutShadow(FontRenderer fontRendererIn, String text, int x, int y, int color)
    {
        fontRendererIn.drawString(text, (x - fontRendererIn.getStringWidth(text) / 2), y, color);
    }

    public static void drawStringWithoutShadow(FontRenderer fontRendererIn, String text, int x, int y, int color)
    {
        fontRendererIn.drawString(text, x, y, color);
    }

    public static void drawTexturedModalRect(int x, int y,float zLevel, int u, int v, int maxU, int maxV, int textureWidth, int textureHeight)
    {
        double f = 1f/(float)(textureWidth);
        double f1 = 1f/(float)(textureHeight);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(x, y + maxV, zLevel).tex(u*f, maxV*f1).endVertex();
        bufferBuilder.pos(x + maxU, y + maxV, zLevel).tex(maxU*f, maxV*f1).endVertex();
        bufferBuilder.pos(x + maxU, y, zLevel).tex(maxU*f, v*f1).endVertex();
        bufferBuilder.pos(x, y, zLevel).tex(u*f, v*f1).endVertex();
        tessellator.draw();
    }

    protected void preScissor(){
        final ScaledResolution res = new ScaledResolution(getRoot().context);
        final double scaleW = getRoot().context.displayWidth / res.getScaledWidth_double();
        final double scaleH = getRoot().context.displayHeight / res.getScaledHeight_double();
        final int ax = getAbsoluteX();
        final int ay = getAbsoluteY();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (ax * scaleW), (int) (getRoot().context.displayHeight - (ay + getHeight()) * scaleH),
                (int) (getWidth() * scaleW), (int) (getHeight() * scaleH));
    }

    protected void postScissor(){
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public abstract static class ViewClipMask {
        protected ViewGroup viewGroup;

        public ViewClipMask(ViewGroup viewGroup){
            this.viewGroup = viewGroup;
        }

        public abstract void drawMask();
    }

    public static class RectangleClipMask extends ViewClipMask {

        public RectangleClipMask(ViewGroup viewGroup) {
            super(viewGroup);
        }

        @Override
        public void drawMask() {
            {
                drawRect(0,0,viewGroup.getWidth(),viewGroup.getHeight(),viewGroup.colorInt(255,255,255,255));
            }
        }
    }
}
