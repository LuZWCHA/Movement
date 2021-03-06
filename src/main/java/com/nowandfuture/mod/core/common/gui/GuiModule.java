package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityShowModule;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyButton;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyLabel;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.SliderView;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.network.message.LMessage;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class GuiModule extends AbstractGuiContainer {
    public static final int GUI_ID = 0x104;

    private static final ResourceLocation MODULE_GUI_TEXTURE = new ResourceLocation(Movement.MODID,"textures/gui/module_shower.png");

    private TileEntityShowModule tileEntityShowModule;
    private SliderView view;
    private MyLabel tickLabel;
    private MyLabel tipLabel;
    private MyButton startBtn;
    private MyButton hideBlockBtn;
    private MyButton useClientCollisionBtn;//not finished

    public GuiModule(InventoryPlayer inventorySlotsIn, TileEntityShowModule tileEntityModule) {
        super(new ContainerModule(inventorySlotsIn,tileEntityModule));
        this.tileEntityShowModule = tileEntityModule;

        xSize = 177;
        ySize = 166;
    }

    @Override
    public void onLoad() {
        view = new SliderView(getRootView());
        view.setX(110);
        view.setY(56);
        view.setWidth(60);
        view.setHeight(20);
        view.setProgress(0);
        view.setProgressChanged(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                long total = tileEntityShowModule.getLine().getTotalTick();
                long tick = aFloat.longValue();
                System.out.println("tick = " + tick);
                LMessage.LongDataMessage message =
                        new LMessage.LongDataMessage(LMessage.LongDataMessage.GUI_TICK_SLIDE,tick);
                message.setPos(tileEntityShowModule.getPos());
                NetworkHandler.INSTANCE.sendMessageToServer(message);
//                tileEntityShowModule.getLine().setTick(tick);
            }
        });
        view.setProgressChanging(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                long total = tileEntityShowModule.getLine().getTotalTick();
                long tick = aFloat.longValue();

                tickLabel.setFirst(String.valueOf(tick));
            }
        });
        addView(view);

        long tick = tileEntityShowModule.getLine().getTick();
        long total = tileEntityShowModule.getLine().getTotalTick();
        view.setRange(total,0,0);
        tickLabel = createMyLabel(130,70,20,12,-1);
        tipLabel = createMyLabel(8,10,157,16,-1);
        tickLabel.addLine(String.valueOf(tick)).enableBackDraw(false);
        tipLabel.addLine(R.name(R.id.text_module_lab_collision_tip_id)).enableBackDraw(false);

        view.setProgress(tick);

        startBtn = createMyButton(134,30,26,16,R.name(R.id.text_module_btn_start_id));
        hideBlockBtn = createMyButton(104,30,26,16,R.name(R.id.text_module_btn_hide_id));
        useClientCollisionBtn = createMyButton(8,30,90,16,R.name(R.id.text_module_btn_collision_enable_id));

        bind(useClientCollisionBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                tileEntityShowModule.setEnableCollision(!tileEntityShowModule.isEnableCollision());
                updateCollisionEnableBtn();
                LMessage.VoidMessage voidMessage = new LMessage.VoidMessage(LMessage.VoidMessage.GUI_ENABLE_COLLISION_FLAG);
                voidMessage.setPos(tileEntityShowModule.getPos());
                NetworkHandler.INSTANCE.sendMessageToServer(voidMessage);
            }
        });

        bind(startBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                startOrStop();
            }
        });
        //noinspection Duplicates
        updateStartOrStopBtn();

        bind(hideBlockBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                tileEntityShowModule.setShowBlock(!tileEntityShowModule.isShowBlock());
                updateShowOrHideBtn();
                LMessage.VoidMessage voidMessage = new LMessage.VoidMessage(LMessage.VoidMessage.GUI_SHOW_OR_HIDE_BLOCK_FLAG);
                voidMessage.setPos(tileEntityShowModule.getPos());
                NetworkHandler.INSTANCE.sendMessageToServer(voidMessage);

            }
        });

        //noinspection Duplicates
        if(tileEntityShowModule.getStackInSlot(1).isEmpty()){
            view.setVisible(false);
            tickLabel.visible = false;
        }else{
            view.setVisible(true);
            tickLabel.visible = true;
        }

        updateShowOrHideBtn();

        addGuiCompoundsRelative(
                useClientCollisionBtn,
                hideBlockBtn,
                tickLabel,
                tipLabel,
                startBtn);
    }

    private void updateCollisionEnableBtn(){
        if(tileEntityShowModule.isEnableCollision()){
            useClientCollisionBtn.displayString = R.name(R.id.text_module_btn_collision_disable_id);
        }else {
            useClientCollisionBtn.displayString = R.name(R.id.text_module_btn_collision_enable_id);
        }
    }

    private void updateShowOrHideBtn(){
        if(tileEntityShowModule.isShowBlock()){
            hideBlockBtn.displayString = R.name(R.id.text_module_btn_hide_id);
        }else {
            hideBlockBtn.displayString = R.name(R.id.text_module_btn_show_id);
        }
    }

    private void updateStartOrStopBtn(){
        if(!tileEntityShowModule.getLine().isEnable()){
            startBtn.displayString = R.name(R.id.text_module_btn_start_id);
        }else {
            startBtn.displayString = R.name(R.id.text_module_btn_stop_id);
        }
    }

    @SuppressWarnings("Duplicates")
    private void startOrStop(){
        LMessage.VoidMessage voidMessage = new LMessage.VoidMessage(LMessage.VoidMessage.GUI_START_FLAG);
        voidMessage.setPos(tileEntityShowModule.getPos());
        if(tileEntityShowModule.getLine().isEnable()){
            tileEntityShowModule.getLine().setEnable(false);
            startBtn.displayString = R.name(R.id.text_module_btn_start_id);
        }else{
            tileEntityShowModule.getLine().setEnable(true);
            startBtn.displayString = R.name(R.id.text_module_btn_stop_id);
        }
        NetworkHandler.INSTANCE.sendMessageToServer(voidMessage);
    }

    // TODO: 2019/7/30 modify to even-notify-mode
    @Override
    public void updateScreen() {
        super.updateScreen();

        if(tileEntityShowModule.getStackInSlot(1).isEmpty()){
            view.setVisible(false);
            tickLabel.visible = false;
        }else{
            view.setVisible(true);
            tickLabel.visible = true;
        }

        if(view.isVisible()){
            long tick = tileEntityShowModule.getLine().getTick();
            long total = tileEntityShowModule.getLine().getTotalTick();

            view.setRange(total,0,0);
            if(!view.isDrag()) {
                tickLabel.setFirst(String.valueOf(tick));
                view.setProgress(tick);
            }
        }

        updateStartOrStopBtn();
        updateCollisionEnableBtn();
    }

    @Override
    public void onDestroy() {
        view.setProgressChanged(null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX,mouseY);
    }

    @Override
    protected void onDrawBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(MODULE_GUI_TEXTURE);

        DrawHelper.drawTexturedModalRect(this.guiLeft, this.guiTop,this.zLevel, 0, 0, this.xSize, this.ySize,177,166);
    }

    @Override
    public long getId() {
        return GUI_ID;
    }
}
