package com.nowandfuture.mod.core.common.gui;

import api.java.yalter.mousetweaks.api.MouseTweaksIgnore;
import com.google.common.collect.Lists;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityCoreModule;
import com.nowandfuture.mod.core.common.gui.custom.PairSlotsListVew;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.DynamicInventory;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyLabel;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.Button;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.SliderView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.TextView;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.network.message.LMessage;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

@MouseTweaksIgnore
public class GuiModule extends AbstractGuiContainer {
    public static final int GUI_ID = 0x104;

    private static final ResourceLocation MODULE_GUI_TEXTURE = new ResourceLocation(Movement.MODID,"textures/gui/module_shower.png");

    private TileEntityCoreModule tileEntityCoreModule;
    private SliderView view;
    //just as an example
    private MyLabel tickLabel;
    private TextView tipLabel;
    private Button addBtn,removeBtn;
    private Button startBtn;
    private Button hideBlockBtn;
    private Button useClientCollisionBtn;//not finished

    private PairSlotsListVew pairSlotsListVew;

    public GuiModule(InventoryPlayer inventorySlotsIn, TileEntityCoreModule tileEntityModule) {
        super(new ContainerModule(inventorySlotsIn,tileEntityModule));
        this.tileEntityCoreModule = tileEntityModule;

        xSize = 177;
        ySize = 166;
    }

    @Override
    public void onLoad() {
        view = GuiBuilder.wrap(new SliderView(getRootView()))
                .setX(110).setY(56).setWidth(60).setHeight(20).build();
        view.setProgress(0);
        view.setProgressChanged(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                long tick = aFloat.longValue();
                LMessage.LongDataMessage message =
                        new LMessage.LongDataMessage(LMessage.LongDataMessage.GUI_TICK_SLIDE,tick);
                message.setPos(tileEntityCoreModule.getPos());
                NetworkHandler.INSTANCE.sendMessageToServer(message);
            }
        });
        view.setProgressChanging(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                long total = tileEntityCoreModule.getLine().getTotalTick();
                long tick = aFloat.longValue();

                tickLabel.setFirst(String.valueOf(tick));
            }
        });
        addView(view);

        //example for mix minecraft gui and my gui,so the ticklabel won't replace with textview
        long tick = tileEntityCoreModule.getLine().getTick();
        long total = tileEntityCoreModule.getLine().getTotalTick();
        view.setRange(total,0,0);
        tickLabel = createMyLabel(130,70,20,12,-1);
        tickLabel.setFirst(String.valueOf(tick)).setBackColor(0).setBorderColor(0);

        tipLabel = new TextView(getRootView())
                .setText(R.name(R.id.text_module_lab_collision_tip_id));
        tipLabel = GuiBuilder.wrap(tipLabel)
                .setWidth(100).setHeight(14).setX(8).setY(16).build();

        view.setProgress(tick);

        startBtn = GuiBuilder.wrap(new Button(getRootView()))
                .setX(134).setY(30).setWidth(26).setHeight(16).build();
        startBtn.setText(R.name(R.id.text_module_btn_start_id));

        hideBlockBtn = GuiBuilder.wrap(new Button(getRootView()))
                .setX(104).setY(30).setWidth(26).setHeight(16).build();
        hideBlockBtn.setText(R.name(R.id.text_module_btn_hide_id));

        useClientCollisionBtn = GuiBuilder.wrap(new Button(getRootView()))
                .setX(8).setY(30).setWidth(90).setHeight(16).build();
        useClientCollisionBtn.setText(R.name(R.id.text_module_btn_collision_enable_id));

        addBtn = GuiBuilder.wrap(new Button(getRootView()))
                .setX(134).setY(8).setWidth(16).setHeight(16).build();
        removeBtn = GuiBuilder.wrap(new Button(getRootView()))
                .setX(150).setY(8).setWidth(16).setHeight(16).build();

        removeBtn.setText("-");
        addBtn.setText("+");

        removeBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                DynamicInventory dynamicInventory = tileEntityCoreModule.getDynInventory();
                if(dynamicInventory.getSizeInventory() > 1) {
                    long id = dynamicInventory
                            .getEntryByIndex(dynamicInventory.getSizeInventory() - 1)
                            .getKey();

                    dynamicInventory.removeSlot(id,false);

                    id = dynamicInventory
                            .getEntryByIndex(dynamicInventory.getSizeInventory() - 1)
                            .getKey();

                    dynamicInventory.removeSlot(id,true);
                }
            }
        });

        addBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                tileEntityCoreModule.getDynInventory().createSlot(ItemStack.EMPTY, 0,false);
                tileEntityCoreModule.getDynInventory().createSlot(ItemStack.EMPTY, 1,true);
            }
        });

        useClientCollisionBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                tileEntityCoreModule.setEnableCollision(!tileEntityCoreModule.isEnableCollision());
                updateCollisionEnableBtn();
                LMessage.VoidMessage voidMessage = new LMessage.VoidMessage(LMessage.VoidMessage.GUI_ENABLE_COLLISION_FLAG);
                voidMessage.setPos(tileEntityCoreModule.getPos());
                NetworkHandler.INSTANCE.sendMessageToServer(voidMessage);
            }
        });

        startBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                startOrStop();
            }
        });

        //noinspection Duplicates
        updateStartOrStopBtn();

        hideBlockBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                tileEntityCoreModule.setShowBlock(!tileEntityCoreModule.isShowBlock());
                updateShowOrHideBtn();
                LMessage.VoidMessage voidMessage = new LMessage.VoidMessage(LMessage.VoidMessage.GUI_SHOW_OR_HIDE_BLOCK_FLAG);
                voidMessage.setPos(tileEntityCoreModule.getPos());
                NetworkHandler.INSTANCE.sendMessageToServer(voidMessage);
            }
        });

        //noinspection Duplicates
        if(tileEntityCoreModule.getStackInSlot(1).isEmpty()){
            setVisible(false,view,tickLabel);
        }else{
            setVisible(true,tickLabel,view);
        }

        pairSlotsListVew = new PairSlotsListVew(getRootView());
        PairSlotsListVew.SlotsAdapter adapter = new PairSlotsListVew.SlotsAdapter();
        adapter.setInventory(tileEntityCoreModule.getDynInventory());
        pairSlotsListVew.bind(adapter);

        pairSlotsListVew.setX(getXSize() + 4);
        pairSlotsListVew.setY(4);
        pairSlotsListVew.setWidth(100);
        pairSlotsListVew.setHeight(120);

        updateShowOrHideBtn();

        addGuiCompoundsRelative(
                addBtn,
                removeBtn,
                useClientCollisionBtn,
                hideBlockBtn,
                tickLabel,
                tipLabel,
                startBtn,
                pairSlotsListVew);
    }

    private void updateCollisionEnableBtn(){
        if(tileEntityCoreModule.isEnableCollision()){
            useClientCollisionBtn.setText(R.name(R.id.text_module_btn_collision_disable_id));
        }else {
            useClientCollisionBtn.setText(R.name(R.id.text_module_btn_collision_enable_id));
        }
    }

    private void updateShowOrHideBtn(){
        if(tileEntityCoreModule.isShowBlock()){
            hideBlockBtn.setText(R.name(R.id.text_module_btn_hide_id));
        }else {
            hideBlockBtn.setText(R.name(R.id.text_module_btn_show_id));
        }
    }

    private void updateStartOrStopBtn(){
        if(!tileEntityCoreModule.getLine().isEnable()){
            startBtn.setText(R.name(R.id.text_module_btn_start_id));
        }else {
            startBtn.setText(R.name(R.id.text_module_btn_stop_id));
        }
    }

    @SuppressWarnings("Duplicates")
    private void startOrStop(){
        LMessage.VoidMessage voidMessage = new LMessage.VoidMessage(LMessage.VoidMessage.GUI_START_FLAG);
        voidMessage.setPos(tileEntityCoreModule.getPos());
        if(tileEntityCoreModule.getLine().isEnable()){
            tileEntityCoreModule.getLine().setEnable(false);
            startBtn.setText(R.name(R.id.text_module_btn_start_id));
        }else{
            tileEntityCoreModule.getLine().setEnable(true);
            startBtn.setText(R.name(R.id.text_module_btn_stop_id));
        }
        NetworkHandler.INSTANCE.sendMessageToServer(voidMessage);
    }

    // TODO: 2019/7/30 modify to even-notify-mode
    @Override
    public void updateScreen() {
        super.updateScreen();

        if(tileEntityCoreModule.getStackInSlot(1).isEmpty()){
            view.setVisible(false);
            tickLabel.visible = false;
        }else{
            view.setVisible(true);
            tickLabel.visible = true;
        }

        if(view.isVisible()){
            long tick = tileEntityCoreModule.getLine().getTick();
            long total = tileEntityCoreModule.getLine().getTotalTick();

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
    protected List<GuiRegion> getExtraRegion() {
        List<GuiRegion> list = Lists.newArrayList();
        list.add(GuiRegion.of(xSize + 4,ySize + 4,xSize + pairSlotsListVew.getWidth(),ySize + pairSlotsListVew.getHeight()));
        return list;
    }

    @Override
    public long getId() {
        return GUI_ID;
    }
}
