package com.nowandfuture.mod.core.common.gui.mygui;

import com.nowandfuture.mod.core.common.gui.mygui.api.IType;
import com.nowandfuture.mod.core.common.gui.mygui.api.IUpdate;
import com.nowandfuture.mod.core.common.gui.mygui.api.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyButton;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyLabel;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyTextField;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.SlotView;
import com.nowandfuture.mod.core.common.gui.mygui.network.ClickDynInventoryCMessage;
import com.nowandfuture.mod.core.common.gui.mygui.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

public abstract class AbstractGuiContainer extends MCGuiContainer {

    private HashMap<Integer,MyGuiWrapper> actions;
    private List<MyGui> guiList;
    private MyGui focusGui;

    private MyGui nowPressedGui;
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

    protected AbstractGuiContainer(){super(null);}

    public AbstractGuiContainer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
        actions = new HashMap<>();
        guiList = new ArrayList<>();

        rootView = new RootView(this.guiLeft,this.guiTop,this.xSize,this.ySize);
        rootView.setContainer(inventorySlotsIn);
        rootView.setGuiContainer(this);
        GuiManager.INSTANCE.register(this,getId());
    }

    @Override
    public final void initGui() {
        super.initGui();
        rootView.setX(this.guiLeft);
        rootView.setY(this.guiTop);
        rootView.setWidth(this.xSize);
        rootView.setHeight(this.ySize);
        rootView.initGui();
        onStart();
        if(isFirstInit) {
            onLoad();
            rootView.onLoad();
            isFirstInit = false;
            MinecraftForge.EVENT_BUS.register(this);
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
        return GuiManager.INSTANCE.getSuggestId(this);
    }

    //Compatible with mc gui, tools to create button,textfield,label

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
        drawDialog(mouseX, mouseY, partialTicks);
        drawTopTip(mouseX, mouseY, partialTicks);
    }

    public interface GuiEvent extends Comparable<GuiEvent> {
        void draw(int mouseX, int mouseY, float partialTicks);
        boolean isDied(int mouseX, int mouseY, float partialTicks);
        void destroy(int mouseX, int mouseY, float partialTicks);
        int getPriority();
        void create(RootView rootView);
    }

    public static abstract class AbstractGuiEvent implements GuiEvent{

        @Override
        public int compareTo(GuiEvent o) {
            return this.getPriority() - o.getPriority();
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public boolean isDied(int mouseX, int mouseY, float partialTicks) {
            return true;
        }
    }

    private final PriorityQueue<GuiEvent> tipList = new PriorityQueue<>();

    public void post(GuiEvent event){
        synchronized (tipList) {
            tipList.add(event);
        }
    }

    public void clearAll(){
        synchronized (tipList) {
            tipList.clear();
        }
    }

    protected void drawTopTip(int mouseX, int mouseY, float partialTicks){
        synchronized (tipList) {
            Iterator<GuiEvent> iterator = tipList.iterator();

            while (iterator.hasNext()) {
                GuiEvent event = iterator.next();

                event.create(rootView);
                event.draw(mouseX, mouseY, partialTicks);

                if (event.isDied(mouseX, mouseY, partialTicks)) {
                    event.destroy(mouseX, mouseY, partialTicks);
                    iterator.remove();
                }
            }
        }
    }

    private void drawDialog(int mouseX, int mouseY, float partialTicks){
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        rootView.drawDialog(mouseX,mouseY,partialTicks);
        rootView.drawDialog2(mouseX,mouseY,partialTicks);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    protected final void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        onDrawForegroundLayer(mouseX,mouseY);
    }

    //rename to obey naming style
    protected void onDrawForegroundLayer(int mouseX, int mouseY){

    }

    @Override
    protected void renderHoveredToolTip(int p_191948_1_, int p_191948_2_) {
        super.renderHoveredToolTip(p_191948_1_, p_191948_2_);
        if(this.mc.player.inventory.getItemStack().isEmpty() && getHoveredExtSlot() != null &&
                !getHoveredExtSlot().getStack().isEmpty()){
            renderToolTip(getHoveredExtSlot().getStack(),p_191948_1_,p_191948_2_);
        }
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
        if(!(gui instanceof ViewGroup))
            offset(gui);
        return addGuiCompound(gui);
    }

    protected final <T extends MyGui> T addGuiCompound(T gui) {
        if(gui instanceof ViewGroup)
            addView((ViewGroup) gui);
        else
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

        //in ViewGroup clicked means btn's down-up(it only happened after release button)
        //pressed means button's down
        //released means button's up
        if(!rootView.mousePressed(mouseX, mouseY,mouseButton)) {
            MyGuiWrapper myGuiWrapper;
            if (mouseButton == 0) {
                for (MyGui gui : this.guiList) {
                    if (gui.mousePressed(mouseX, mouseY, mouseButton)) {
                        nowPressedGui = gui;
                        setFocusGui(gui);
                        myGuiWrapper = actions.get(nowPressedGui.getId());
                        if (myGuiWrapper != null && myGuiWrapper.action != null) {
                            myGuiWrapper.action.press(myGuiWrapper.gui, mouseButton);
                        }
                        return;
                    }
                }
            }
            setFocusGui(null);
        }else{
            setFocusGui(null);
        }

        if(!rootView.isDialogShowing())
            super.mouseClicked(mouseX, mouseY, mouseButton);
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

    //set Visible of all kind of gui once
    protected void setVisible(boolean value,MyGui... guis){
        for (MyGui gui :
                guis) {
            if (gui instanceof MyButton) ((MyButton) gui).visible = value;
            else if(gui instanceof MyTextField) ((MyTextField) gui).setVisible(value);
            else if(gui instanceof MyLabel) ((MyLabel) gui).visible = value;
            else if(gui instanceof ViewGroup) {
                ((ViewGroup) gui).setVisible(value);
            }
            else if(gui instanceof RootView){
                ((RootView) gui).setVisible(value);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        pressTime = Minecraft.getSystemTime() - lastPressTime;

        rootView.mouseReleased(mouseX, mouseY, state);

        if (this.nowPressedGui != null && state == 0)
        {
            try {
                this.nowPressedGui.mouseReleased(mouseX, mouseY,state);

                MyGuiWrapper myGuiWrapper = actions.get(nowPressedGui.getId());
                if(myGuiWrapper != null && myGuiWrapper.action != null){
                    myGuiWrapper.action.release(myGuiWrapper.gui,state);
                }

                if(pressTime > 0 && pressTime <1000) {
                    nowPressedGui.mouseClicked(mouseX, mouseY, state);
                    if(myGuiWrapper != null && myGuiWrapper.action != null){
                        myGuiWrapper.action.clicked(myGuiWrapper.gui,state);
                    }
                } else if(pressTime >= 1000){
                    nowPressedGui.mouseLongClicked(mouseX, mouseY,state);
                    if(myGuiWrapper != null && myGuiWrapper.action != null){
                        myGuiWrapper.action.longClick(myGuiWrapper.gui,state,pressTime);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            pressTime = 0;
            this.nowPressedGui = null;
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
        rootView.update();
    }

    private boolean handle = false;
    //avoid of cancel by other mod's logical
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void handleMouseInputEvent(GuiScreenEvent.MouseInputEvent event){
        handle = false;
        if(handleMouseInputIn()){
            event.setCanceled(true);
        }
        try {
            handleMouseInput();
        }catch (Exception e){

        }
        handle = true;
    }

//    @Override
//    public void handleMouseInput() throws IOException {
//        super.handleMouseInput();
//    }

    public boolean handleMouseInputIn(){
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        //handle mouse other behaves
        return rootView.handleMouseInput(i,j);
    }

    protected void childLoseFocus(MyGui gui){

    }

    protected void childFocused(MyGui gui){

    }

    @Override
    protected Slot getSlotAtPosition(int x, int y) {
        Slot slot = super.getSlotAtPosition(x, y);
        if(slot == null){
            if(rootView.getHoverView() instanceof SlotView){
                Slot ps = ((SlotView) rootView.getHoverView()).getSlot();
                ps.slotNumber = ps.getSlotIndex();
                return ps;
            }
        }
        return slot;
    }

    @Override
    public final void onGuiClosed() {
        onDestroy();
        super.onGuiClosed();
        isFirstInit = true;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public void onDestroy(){
        rootView.clear();
        tipList.clear();
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if(slotIn instanceof AbstractContainer.ProxySlot){
            slotId = slotIn.getSlotIndex();
            AbstractContainer container = (AbstractContainer) inventorySlots;
            short short1 = container.getNextTransactionID(mc.player.inventory);
            ItemStack itemStack = container.slotClickInExtSlot(slotId,mouseButton,type,mc.player);
            ClickDynInventoryCMessage message = new ClickDynInventoryCMessage(inventorySlots.windowId,container.getDynInventoryId(),
                    itemStack,slotId,mouseButton,type, short1);
            NetworkHandler.INSTANCE.sendMessageToServer(message);
        }else
            super.handleMouseClick(slotIn, slotId, mouseButton, type);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {

        if(rootView.getFocusedView() != null){
            rootView.handleKeyType(typedChar, keyCode);
        }else if (focusGui != null && keyCode != 1/* esc */) {
            if(focusGui instanceof IType){
                ((IType) focusGui).keyTyped(typedChar, keyCode);
            }
        }else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY) {
        if(rootView.isDialogShowing()) return false;
        return super.isMouseOverSlot(slotIn, mouseX, mouseY);
    }

    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int left, int top) {
        return super.hasClickedOutside(mouseX, mouseY, left, top) &&
                !isInside(getExtraRegion(),mouseX,mouseY);
    }

    private boolean isInside(List<GuiRegion> regions,int x,int y){
        if(regions == null) return false;
        for (GuiRegion v :
                regions) {
            if(x > v.left && x < v.right && y < v.bottom && y > v.top){
                return true;
            }
        }
        return false;
    }

    protected abstract List<GuiRegion> getExtraRegion();

    @Optional.Method(modid = "jei")
    protected JEIGuiHandler<? extends AbstractGuiContainer> createJEIGuiHandler(){return null;}

    public static class GuiBuilder<T extends ViewGroup>{
        private int x,y,w,h;
        private T v;

        public static <T extends ViewGroup,R extends GuiBuilder<T>>R wrap(T viewGroup){
            return newBuilder(viewGroup);
        }

        public GuiBuilder(T viewGroup){
            v = viewGroup;
            x = v.getX();
            y = v.getY();
            w = v.getWidth();
            h = v.getHeight();
        }

        private static <T extends ViewGroup,R extends GuiBuilder<T>> R newBuilder(T v){
            return (R) new GuiBuilder<>(v);
        }

        public <R extends GuiBuilder<T>> R setWidth(int w) {
            this.w = w;
            return (R) this;
        }

        public <R extends GuiBuilder<T>> R setHeight(int h) {
            this.h = h;
            return (R) this;
        }

        public T build(){
            v.setX(x);
            v.setY(y);
            v.setWidth(w);
            v.setHeight(h);
            return v;
        }

        public <R extends GuiBuilder<T>> R setX(int x) {
            this.x = x;
            return (R) this;
        }

        public <R extends GuiBuilder<T>> R setY(int y) {
            this.y = y;
            return (R) this;
        }
    }

    public static class GuiRegion{
        public int left,top,right,bottom;

        private GuiRegion(){

        }

        private GuiRegion(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public static GuiRegion of(int left, int top, int right, int bottom){
            return new GuiRegion(left,top,right,bottom);
        }

        public static GuiRegion of(MyGui gui){
            return new GuiRegion(gui.getX(),gui.getY(),gui.getX() + gui.getWidth(),gui.getY() + gui.getHeight());
        }


    }

    public static String Vec3iString(Vec3i pos){
        return String.format("%d,%d,%d", pos.getX(),pos.getY(),pos.getZ());
    }

    public static String Vec3dString(Vec3d vec3d){
        return String.format("%f,%f,%f", vec3d.x,vec3d.y,vec3d.z);
    }
}
