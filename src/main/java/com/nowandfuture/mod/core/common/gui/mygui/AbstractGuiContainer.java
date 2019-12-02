package com.nowandfuture.mod.core.common.gui.mygui;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.*;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyButton;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyLabel;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractGuiContainer extends GuiContainer {

    private HashMap<Integer,MyGuiWrapper> actions;
    private List<MyGui> guiList;
    private MyGui focusGui;

    private MyGui nowPressGui;
    private long pressTime;
    private long lastPressTime;
    private boolean isFirstInit = true;

    private RootView rootView;

    public interface IAction{
        void clicked(MyGui gui,int button);
        void press(MyGui gui,int button);
        void release(MyGui gui,int button);
        void longClick(MyGui gui,int button,long lastTime);
    }

    public static abstract  class ActionClick implements IAction{
        public final void press(MyGui gui,int button){}
        public final void release(MyGui gui,int button){}
        public void longClick(MyGui gui,int button,long lastTime){}
    }

    public static abstract  class ActionPress implements IAction{
        public final void clicked(MyGui gui,int button){}
        public final void release(MyGui gui,int button){}
    }

    public static abstract  class ActionRelease implements IAction{
        public final void clicked(MyGui gui,int button){}
        public final void press(MyGui gui,int button){}
    }

    public static class MyGuiWrapper {
        private MyGui gui;
        private IAction action;

        public MyGuiWrapper(MyGui gui, IAction action){
            this.action = action;
            this.gui = gui;
        }
    }

    @Override
    protected final void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        MyGuiWrapper myGuiWrapper = actions.get(button.id);
        if(myGuiWrapper != null && myGuiWrapper.action != null){
            myGuiWrapper.action.clicked(myGuiWrapper.gui,0);
        }
    }

    protected void bind(MyGui gui, IAction action){
        actions.put(gui.getId(),new MyGuiWrapper(gui, action));
    }

    public AbstractGuiContainer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
        actions = new HashMap<>();
        guiList = new ArrayList<>();

        rootView = new RootView(this.guiLeft,this.guiTop,this.xSize,this.ySize);
        GuiIdManager.INSTANCE.register(this,getId());
    }

    @Override
    public final void initGui() {
        super.initGui();
        rootView.setX(this.guiLeft);
        rootView.setY(this.guiTop);
        rootView.setWidth(this.width);
        rootView.setHeight(this.height);
        onStart();
        if(isFirstInit) {
            onLoad();
            rootView.onLoad();
            isFirstInit = false;
        }
    }

    //if use buttonList create here!
    public void onStart(){

    }

    public abstract void onLoad();

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {
        this.mc = mcIn;
        this.itemRender = mc.getRenderItem();
        this.fontRenderer = mc.fontRenderer;
        this.width = w;
        this.height = h;

        buttonList.clear();

        int offsetX = guiLeft;
        int offsetY = guiTop;

        this.initGui();

        offsetX = guiLeft - offsetX;
        offsetY = guiTop - offsetY;

        //reset pos for gui
        for (MyGui gui :
                guiList) {
            gui.setX(gui.getX() + offsetX);
            gui.setY(gui.getY() + offsetY);
        }
    }

    public RootView getRootView() {
        return rootView;
    }

    public abstract long getId();

    public long genChildId(){
        return GuiIdManager.INSTANCE.getSuggestId(this);
    }

    public MyButton createMyButton(int x, int y, int width, int height, String string){
        return new MyButton((int) genChildId(),x,y,width,height,string);
    }

    public MyTextField createMyTextField(int x, int y, int width, int height, String hint){
        return new MyTextField((int) genChildId(),this.fontRenderer,x,y,width,height);
    }

    public MyLabel createMyLabel(int x, int y, int width, int height, int textColor){
        return new MyLabel(this.fontRenderer,(int) genChildId(),x,y,width,height,textColor);
    }

    @Override
    protected final void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        onDrawBackgroundLayer(partialTicks, mouseX, mouseY);
        onDrawBackgroundLayerIn(partialTicks, mouseX, mouseY);
    }

    protected void onDrawBackgroundLayer(float partialTicks, int mouseX, int mouseY){

    }

    private void onDrawBackgroundLayerIn(float partialTicks, int mouseX, int mouseY){
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        for (MyGui t :
                guiList) {
            t.draw(mouseX,mouseY,partialTicks);
            t.draw2(mouseX, mouseY, partialTicks);
        }
        rootView.draw(mouseX, mouseY, partialTicks);
        rootView.draw2(mouseX, mouseY, partialTicks);

        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected final void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        onDrawForegroundLayer(mouseX,mouseY);
    }

    //rename to obey naming style
    protected void onDrawForegroundLayer(int mouseX, int mouseY){

    }

    protected final void offset(MyGui gui){
        gui.setX(guiLeft + gui.getX());
        gui.setY(guiTop + gui.getY());
    }

    protected final void offsetBack(MyGui gui){
        gui.setX(gui.getX() - guiLeft);
        gui.setY(gui.getY() - guiTop);
    }

    protected final <T extends MyGui> T addGuiCompoundRelative(T gui) {
        offset(gui);
        return addGuiCompound(gui);
    }

    protected final <T extends MyGui> T addGuiCompound(T gui) {
        guiList.add(gui);
        return gui;
    }

    protected <T extends MyGui> void addGuiCompoundsRelative(T... guis) {
        for (T b :
                guis) {
            addGuiCompoundRelative(b);
        }
    }

    protected <T extends MyGui> void addGuiCompounds(T... guis) {
        for (T b :
                guis) {
            addGuiCompound(b);
        }
    }

    public void addView(ViewGroup viewGroup){
        rootView.add(viewGroup);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        pressTime = 0;
        lastPressTime = Minecraft.getSystemTime();
        super.mouseClicked(mouseX, mouseY, mouseButton);

        //in ViewGroup clicked means btn's down-up(it only happened after release button)
        //pressed means btn's down
        //released means btn's up
        if(!rootView.mousePressed(mouseX, mouseY,mouseButton)) {
            MyGuiWrapper myGuiWrapper;
            if (mouseButton == 0) {
                for (MyGui gui : this.guiList) {
                    if (gui.mousePressed(mouseX, mouseY, mouseButton)) {
                        nowPressGui = gui;
                        setFocusGui(gui);
                        myGuiWrapper = actions.get(nowPressGui.getId());
                        if (myGuiWrapper != null && myGuiWrapper.action != null) {
                            myGuiWrapper.action.press(myGuiWrapper.gui, mouseButton);
                        }
                        return;
                    }
                }
            }
            setFocusGui(null);
        }

    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        rootView.mousePressedMove(mouseX,mouseY,clickedMouseButton);
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void setFocusGui(MyGui focusGui) {
        if(this.focusGui != focusGui){
            childLoseFocus(this.focusGui);
            this.focusGui = focusGui;
            childFocused(this.focusGui);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        pressTime = Minecraft.getSystemTime() - lastPressTime;

        rootView.mouseReleased(mouseX, mouseY, state);

        if (this.nowPressGui != null && state == 0)
        {
            try {
                this.nowPressGui.mouseReleased(mouseX, mouseY,state);

                MyGuiWrapper myGuiWrapper = actions.get(nowPressGui.getId());
                if(myGuiWrapper != null && myGuiWrapper.action != null){
                    myGuiWrapper.action.release(myGuiWrapper.gui,state);
                }

                if(pressTime > 0 && pressTime <1000) {
                    nowPressGui.mouseClicked(mouseX, mouseY, state);
                    if(myGuiWrapper != null && myGuiWrapper.action != null){
                        myGuiWrapper.action.clicked(myGuiWrapper.gui,state);
                    }
                } else if(pressTime >= 1000){
                    nowPressGui.mouseLongClicked(mouseX, mouseY,state);
                    if(myGuiWrapper != null && myGuiWrapper.action != null){
                        myGuiWrapper.action.longClick(myGuiWrapper.gui,state,pressTime);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            pressTime = 0;
            this.nowPressGui = null;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (MyGui gui :
                guiList) {
            if (gui instanceof IUpdate)
                ((IUpdate) gui).update();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        //handle mouse other behaves
        rootView.handleMouseInput(i,j);
    }

    protected void childLoseFocus(MyGui gui){

    }

    protected void childFocused(MyGui gui){

    }

    @Override
    public void onGuiClosed() {
        onDestroy();
        super.onGuiClosed();
    }

    public void onDestroy(){
        rootView.clear();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {

        if (focusGui != null && keyCode != 1/* esc */) {
            if(focusGui instanceof IType){
                ((IType) focusGui).keyTyped(typedChar, keyCode);
            }
        }else {
            super.keyTyped(typedChar, keyCode);
        }
    }

}
