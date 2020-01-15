package com.nowandfuture.mod.core.common.gui.mygui.compounds;

import com.nowandfuture.mod.core.common.gui.mygui.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.FrameLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class RootView implements MyGui{
    public Minecraft context = Minecraft.getMinecraft();

    protected long LONG_CLICK = 1000;//ms

    private int x,y,w,h;

    private final ViewGroup topView;
    private ViewGroup focusedView;

    ViewGroup getTopView() {
        return topView;
    }

    public static boolean isInside(MyGui gui, int mouseX, int mouseY){
        return mouseX >= 0 && mouseY >= 0 && mouseX <= gui.getWidth() && mouseY <= gui.getHeight();
    }

    public FontRenderer getFontRenderer(){
        return context.fontRenderer;
    }

    final public ViewGroup getFocusedView(){
        return focusedView;
    }

    final void setFocusedView(ViewGroup viewGroup){
        if(focusedView != null){
            focusedView.setFocused(false);

            if(viewGroup != null) {
                focusedView = viewGroup;
                viewGroup.setFocused(true);
            }else{
                focusedView = null;
            }

        }else{
            if(viewGroup != null) {
                focusedView = viewGroup;
                focusedView.setFocused(true);
            }
        }
    }

    public RootView(int x,int y,int w,int h){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        topView = new FrameLayout(this,null);

        topView.setX(x);
        topView.setY(y);
        topView.setWidth(w);
        topView.setHeight(h);
    }

    public void onLoad(){
        topView.onLoad();
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
        topView.setX(x);
    }

    @Override
    public void setY(int y) {
        this.y = y;
        topView.setY(y);
    }

    @Override
    public int getWidth() {
        return w;
    }

    @Override
    public int getHeight() {
        return h;
    }

    @Override
    public void setWidth(int width) {
        w = width;
        topView.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        h = height;
        topView.setHeight(height);
    }

    @Override
    public final void draw(int mouseX, int mouseY, float partialTicks) {
        topView.layout(this.getWidth(),this.getHeight());

        GlStateManager.pushMatrix();
        GlStateManager.translate(getX(),getY(),0);
        topView.draw(mouseX - getX(), mouseY - getY(), partialTicks);
        GlStateManager.popMatrix();

    }

    @Override
    public void draw2(int mouseX, int mouseY, float partialTicks) {
        topView.draw2(mouseX, mouseY, partialTicks);
    }

    @Override
    public final boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        //nothing to do
        return false;
    }

    @Override
    public final boolean mouseLongClicked(int mouseX, int mouseY, int mouseButton) {
        //nothing to do
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        topView.mouseReleased(mouseX - getX(), mouseY - getY(), state);
    }

    public void mousePressedMove(int mouseX, int mouseY,int state){
        topView.mousePressedMove(mouseX - getX(), mouseY - getY(), state);
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY,int state) {
        mouseX -= getX();
        mouseY -= getY();
        boolean flag = false;
        if(RootView.isInside(this,mouseX,mouseY))
            flag = topView.mousePressed(mouseX, mouseY, state);

        if(!flag) setFocusedView(null);

        return flag;
    }

    @Override
    public boolean handleMouseInput(int mouseX, int mouseY) {
        mouseX -= getX();
        mouseY -= getY();
        if(RootView.isInside(topView,mouseX,mouseY))
            return topView.handleMouseInput(mouseX, mouseY);
        return true;
    }

    public boolean handleKeyType(char typedChar, int keyCode){
        return topView.handleKeyType(typedChar, keyCode);
    }

    //gametick update
    public void update(){
        topView.onUpdate();
    }

    public void add(ViewGroup viewGroup){
        this.topView.addChild(viewGroup);
    }

    public void add(int index ,ViewGroup viewGroup){
        this.topView.addChild(index,viewGroup);
    }

    public void remove(ViewGroup viewGroup){
        this.topView.removeChild(viewGroup);
    }

    public void remove(int index){
        this.topView.removeChild(index);
    }

    public void clear(){
        topView.clear();
    }

    public void setVisible(boolean v){
        if(topView != null)
            topView.setVisible(v);
    }
}
