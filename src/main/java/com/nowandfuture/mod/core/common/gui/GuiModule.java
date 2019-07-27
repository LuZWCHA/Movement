package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityShowModule;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyLabel;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.SliderView;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.network.message.MovementMessage;
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
                long tick = (long)(total * aFloat);
                MovementMessage.LongDataMessage message = new MovementMessage.LongDataMessage(MovementMessage.LongDataMessage.GUI_TICK_SLIDE,tick);
                message.setPos(tileEntityShowModule.getPos());
                NetworkHandler.INSTANCE.sendMessageToServer(message);
                tileEntityShowModule.getLine().setTick(tick);
            }
        });
        view.setProgressChanging(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                long total = tileEntityShowModule.getLine().getTotalTick();
                long tick = (long)(total * aFloat);

                tickLabel.setLine(0, String.valueOf(tick));
            }
        });
        addView(view);

        long tick = tileEntityShowModule.getLine().getTick();
        long total = tileEntityShowModule.getLine().getTotalTick();
        tickLabel = createMyLabel(130,70,20,12,-1);
        tickLabel.addLine(String.valueOf(tick)).enableBackDraw(false);

        view.setProgress(tick / (float)total);

        addGuiCompoundsRelative(tickLabel);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if(tileEntityShowModule.getStackInSlot(1).isEmpty()){
            view.setVisible(false);
        }else{
            view.setVisible(true);
        }

        if(view.isVisible()){
            long tick = tileEntityShowModule.getLine().getTick();
            long total = tileEntityShowModule.getLine().getTotalTick();

            if(!view.isDrag()) {
                tickLabel.setLine(0, String.valueOf(tick));
                view.setProgress(tick / (float) total);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
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
