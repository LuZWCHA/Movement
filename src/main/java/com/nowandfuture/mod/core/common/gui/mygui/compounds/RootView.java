package com.nowandfuture.mod.core.common.gui.mygui.compounds;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.api.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.layouts.FrameLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class RootView implements MyGui{
    public Minecraft context = Minecraft.getMinecraft();
    private Container container;
    private AbstractGuiContainer guiContainer;

    protected long longClickThreshold = 1000;//ms

    private int x,y,w,h;

    private final ViewGroup topView;
    private ViewGroup focusedView;
    private ViewGroup hoverView;

    private Dialog dialogView;
    private final ViewGroup notifyView;

    ViewGroup getTopView() {
        return topView;
    }

    public static boolean isInside(MyGui gui, int mouseX, int mouseY){
        return mouseX >= 0 && mouseY >= 0 && mouseX <= gui.getWidth() && mouseY <= gui.getHeight();
    }

    public static boolean isInside2(MyGui gui, int mouseXAtParent, int mouseYAtParent){
        mouseXAtParent -= gui.getX();
        mouseYAtParent -= gui.getY();
        return mouseXAtParent >= 0 && mouseYAtParent >= 0 && mouseXAtParent <= gui.getWidth() && mouseYAtParent <= gui.getHeight();
    }

    public static boolean isInside(MyGui parent,MyGui gui, int mouseX, int mouseY){
        mouseX += gui.getX();
        mouseY += gui.getY();
        return mouseX >= 0 && mouseY >= 0 && mouseX <= parent.getWidth() && mouseY <= parent.getHeight();
    }

    public FontRenderer getFontRenderer(){
        return context.fontRenderer;
    }

    final public ViewGroup getFocusedView(){
        return focusedView;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public Container getContainer() {
        return container;
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
        topView = new TopView(this);
        topView.setClipping(false);

        topView.setX(x);
        topView.setY(y);
        topView.setWidth(w);
        topView.setHeight(h);

        dialogView = new Dialog();

        //not finished
        notifyView = new FrameLayout(this);
        notifyView.setX(x);
        notifyView.setY(y);
        notifyView.setWidth(w);
        notifyView.setHeight(h);
    }

    public static class DialogBuilder{
        RootView rootView;
        ViewGroup content;

        private DialogBuilder(RootView rootView,ViewGroup content){
            this.rootView = rootView;
            this.content = content;
        }

        static DialogBuilder newDialogBuilder(RootView rootView,ViewGroup content){
            return new DialogBuilder(rootView,content);
        }

        public DialogBuilder buildDialog(ViewGroup view){
            content = view;
            content.setX(rootView.x);
            content.setY(rootView.y);
            content.setWidth(rootView.w);
            content.setHeight(rootView.h);

            content.setVisible(false);
            return this;
        }


        public DialogBuilder showDialog(){
            if(content != null){
                content.setVisible(true);
            }
            return this;
        }

        public DialogBuilder hideDialog(){
            if(content != null){
                content.setVisible(false);
            }
            return this;
        }

        public Dialog build(){
            Dialog dialog = new Dialog(content);
            dialog.setSize(content.getWidth(),content.getHeight());
            rootView.setDialogView(dialog);
            return dialog;
        }
    }

    public DialogBuilder createDialogBuilder(ViewGroup content){
        return new DialogBuilder(this,content);
    }

    void setDialogView(Dialog view){
        this.dialogView = view;
    }

    public void onLoad(){
        Framebuffer framebuffer = Minecraft.getMinecraft().getFramebuffer();
        if(!framebuffer.isStencilEnabled()){
            framebuffer.enableStencil();
        }
        topView.load();
    }

    @Deprecated
    public void onSizeChanged(int oldW,int oldH,int w,int h){

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

    public void initGui(){
        //update dialog's position
        if(dialogView.isShowing() && dialogView.isInCenter())
            dialogView.setCenter();
    }

    @Override
    public final void draw(int mouseX, int mouseY, float partialTicks) {
        topView.layout(this.getWidth(),this.getHeight());
        if(dialogView.isShowing()) topView.setReachable(false);
        else topView.setReachable(true);

        GlStateManager.pushMatrix();
        GlStateManager.translate(topView.getX(),topView.getY(),0);
        updateHoveringView(topView,mouseX,mouseY);
        topView.draw(mouseX - topView.getX(), mouseY - topView.getY(), partialTicks);
        GlStateManager.popMatrix();
    }

    private boolean updateHoveringView(ViewGroup root,int mouseX, int mouseY){
        ViewGroup hover = root.checkHover(mouseX - root.getX(), mouseY - root.getY());
        if(hoverView != null) hoverView.setHovering(false);
        if(hover != null) hover.setHovering(true);
        hoverView = hover;
        return hoverView != null;
    }

    public final void drawDialog(int mouseX, int mouseY, float partialTicks) {
        if(!dialogView.isShowing()) return;
        ViewGroup content = dialogView.getView();
        content.layout(this.getWidth(),this.getHeight());

        GlStateManager.pushMatrix();
        GlStateManager.translate(content.getX(),content.getY(),0);
        ViewGroup hover = content.checkHover(mouseX - content.getX(), mouseY - content.getY());
        if(hoverView != null) hoverView.setHovering(false);
        if(hover != null) hover.setHovering(true);
        hoverView = hover;
        content.draw(mouseX - content.getX(), mouseY - content.getY(), partialTicks);
        GlStateManager.popMatrix();
    }

    public boolean isDialogShowing(){
        return dialogView.isShowing();
    }

    @Override
    public void draw2(int mouseX, int mouseY, float partialTicks) {
        topView.draw2(mouseX, mouseY, partialTicks);
    }

    public void drawDialog2(int mouseX, int mouseY, float partialTicks) {
        if(dialogView.isShowing())
            dialogView.getView().draw2(mouseX, mouseY, partialTicks);
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
        if(dialogView.isShowing()){
            ViewGroup content = dialogView.getView();
            content.mouseReleased(mouseX - content.getX(), mouseY - content.getY(), state);
        }else
            topView.mouseReleased(mouseX - getX(), mouseY - getY(), state);
    }

    public void mousePressedMove(int mouseX, int mouseY,int state){
        if(dialogView.isShowing()){
            ViewGroup content = dialogView.getView();
            content.mousePressedMove(mouseX - content.getX(), mouseY - content.getY(), state);
        }else
            topView.mousePressedMove(mouseX - getX(), mouseY - getY(), state);
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY,int state) {

        boolean flag;
        if(dialogView.isShowing()) {
            ViewGroup content = dialogView.getView();
            mouseX -= content.getX();
            mouseY -= content.getY();
            if(isInside(content,mouseX,mouseY))
                content.mousePressed(mouseX, mouseY, state);
            else{
                setFocusedView(null);
                dialogView.dispose();
            }
            flag = true;
        } else {
            mouseX -= getX();
            mouseY -= getY();
            flag = topView.mousePressed(mouseX, mouseY, state);
        }

        if(!flag) setFocusedView(null);

        return flag;
    }

    @Override
    public boolean handleMouseInput(int mouseX, int mouseY) {

        if(dialogView.isShowing()) {
            ViewGroup content = dialogView.getView();
            mouseX -= content.getX();
            mouseY -= content.getY();
            return content.handleMouseInput(mouseX, mouseY);
        }
        mouseX -= getX();
        mouseY -= getY();
        return topView.handleMouseInput(mouseX, mouseY);
    }

    public boolean handleKeyType(char typedChar, int keyCode){
        if(dialogView.isShowing()) {
            return dialogView.getView().handleKeyType(typedChar, keyCode);
        }
        return topView.handleKeyType(typedChar, keyCode);
    }

    //gametick update
    public void update(){
        topView.onUpdate();
        if(dialogView.isShowing())
            dialogView.getView().onUpdate();
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
        topView.destroy();
    }

    public void setVisible(boolean v){
        if(topView != null)
            topView.setVisible(v);
    }

    public boolean isShowDebugInfo(){
        return context.gameSettings.showDebugInfo;
    }

    public AbstractGuiContainer getGuiContainer() {
        return guiContainer;
    }

    public void setGuiContainer(AbstractGuiContainer guiContainer) {
        this.guiContainer = guiContainer;
    }

    public Slot getSlotFromInventory(IInventory inventory,int index){
        return getGuiContainer().inventorySlots.getSlotFromInventory(inventory,index);
    }

    public ViewGroup getHoverView() {
        return hoverView;
    }

    public static class TextTipEvent extends AbstractGuiContainer.AbstractGuiEvent{

        private String text;
        private RootView rootView;

        public TextTipEvent(String text){
            this.text = text;
        }

        @Override
        public void create(RootView rootView) {
            this.rootView = rootView;
        }

        @Override
        public void draw(int mouseX, int mouseY, float partialTicks) {
            rootView.getGuiContainer().drawHoveringText(text,mouseX,mouseY);
        }

        @Override
        public void destroy(int mouseX, int mouseY, float partialTicks) {

        }
    }
}
