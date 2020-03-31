package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.Items.PrefabItem;
import com.nowandfuture.mod.core.common.entities.TileEntityConstructor;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.api.IChangeListener;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.Button;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.EditorView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.TextView;
import com.nowandfuture.mod.core.common.gui.slots.PrefabOnlySlot;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.network.message.LMessage;
import com.nowandfuture.mod.utils.DrawHelper;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.Color;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class GuiConstructor extends AbstractGuiContainer implements IContainerListener{
    public static final int GUI_ID = 0x102;

    private Button lockBtn;
    private Button createBtn;

    private Button resizeBtnXAdd;
    private Button resizeBtnYAdd;
    private Button resizeBtnZAdd;
    private Button resizeBtnXSub;
    private Button resizeBtnYSub;
    private Button resizeBtnZSub;

    private TextView xLengthTv;
    private TextView yLengthTv;
    private TextView zLengthTv;

    private EditorView nameEv;

    private static final ResourceLocation CONSTRUCTOR_GUI_TEXTURE = new ResourceLocation(Movement.MODID,"textures/gui/prefab_constructor.png");

    private final TileEntityConstructor tileConstructor;

    public GuiConstructor(InventoryPlayer playerInv, TileEntityConstructor tileConstructor) {
        super(new ContainerConstructor(playerInv,tileConstructor));
        this.tileConstructor = tileConstructor;

        xSize = 285;
        ySize = 165;
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {
        inventorySlots.removeListener(this);
        super.onResize(mcIn, w, h);
    }

    @Override
    public void onLoad() {

        lockBtn = GuiBuilder.wrap(new Button(getRootView()))
                .setX(116).setY(56).setWidth(48).setHeight(16).build();
        lockBtn.setText(R.name(R.id.text_constructor_btn_lock_lock_id));
        createBtn = GuiBuilder.wrap(new Button(getRootView()))
                .setX(172).setY(56).setWidth(48).setHeight(16).build();
        createBtn.setText(R.name(R.id.text_constructor_btn_create_create_id));

        nameEv = GuiBuilder.wrap(new EditorView(getRootView()))
                .setX(156).setY(15).setWidth(95).setHeight(12).build();
        nameEv.setText(Strings.EMPTY);

        resizeBtnXAdd = GuiBuilder.wrap(new Button(getRootView()))
                .setX(10).setY(56).setWidth(20).setHeight(16).build();
        resizeBtnXAdd.setText(R.name(R.id.text_constructor_btn_resizex_add_id));
        resizeBtnXSub = GuiBuilder.wrap(new Button(getRootView()))
                .setX(80).setY(56).setWidth(20).setHeight(16).build();
        resizeBtnXSub.setText(R.name(R.id.text_constructor_btn_resizex_sub_id));
        resizeBtnYAdd = GuiBuilder.wrap(new Button(getRootView()))
                .setX(10).setY(82).setWidth(20).setHeight(16).build();
        resizeBtnYAdd.setText(R.name(R.id.text_constructor_btn_resizey_add_id));
        resizeBtnYSub = GuiBuilder.wrap(new Button(getRootView()))
                .setX(80).setY(82).setWidth(20).setHeight(16).build();
        resizeBtnYSub.setText(R.name(R.id.text_constructor_btn_resizey_sub_id));
        resizeBtnZAdd = GuiBuilder.wrap(new Button(getRootView()))
                .setX(10).setY(108).setWidth(20).setHeight(16).build();
        resizeBtnZAdd.setText(R.name(R.id.text_constructor_btn_resizez_add_id));
        resizeBtnZSub = GuiBuilder.wrap(new Button(getRootView()))
                .setX(80).setY(108).setWidth(20).setHeight(16).build();
        resizeBtnZSub.setText(R.name(R.id.text_constructor_btn_resizez_sub_id));

        xLengthTv = GuiBuilder.wrap(new TextView(getRootView()))
                .setX(40).setY(56).setWidth(30).setHeight(16).build();
        yLengthTv = GuiBuilder.wrap(new TextView(getRootView()))
                .setX(40).setY(82).setWidth(30).setHeight(16).build();
        zLengthTv = GuiBuilder.wrap(new TextView(getRootView()))
                .setX(40).setY(108).setWidth(30).setHeight(16).build();

        nameEv.setTextColor(new Color(255,255,255));
        nameEv.setDisabledTextColor(new Color(128,128,128));
        nameEv.setDrawDecoration(false);
        nameEv.setMaxStringLength(40);

        nameEv.setOnLoseFocus(new Consumer<EditorView>() {
            @Override
            public void accept(EditorView editorView) {
                if(!tileConstructor.isEmpty() &&
                        !tileConstructor.getPrefabName()
                                .equals(nameEv.getText())) {
                    LMessage.StringDataSyncMessage message =
                            new LMessage.StringDataSyncMessage(LMessage.StringDataSyncMessage.GUI_CONSTRUCT_RENAME, nameEv.getText());
                    message.setPos(tileConstructor.getPos());
                    NetworkHandler.INSTANCE.sendMessageToServer(message);
                }
            }
        });

        xLengthTv.setCentered(true);
        yLengthTv.setCentered(true);
        zLengthTv.setCentered(true);

        addGuiCompoundsRelative(
                lockBtn,
                createBtn,
                resizeBtnXAdd,
                resizeBtnYAdd,
                resizeBtnZAdd,
                resizeBtnXSub,
                resizeBtnYSub,
                resizeBtnZSub,
                xLengthTv,
                yLengthTv,
                zLengthTv,
                nameEv
        );

        createBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                if(tileConstructor.isLock()) {
                    if(Minecraft.getMinecraft().player.getName().equals(tileConstructor.getLockUserName()))
                        tileConstructor.constructTest(
                                Minecraft.getMinecraft().world,
                                tileConstructor.getPos()
                        );
                    else {
                        NetworkHandler.INSTANCE.sendClientCommandMessage("locked by other player");
                    }
                }else {
                    NetworkHandler.INSTANCE.sendClientCommandMessage("constructor should be locked at first!");
                }
            }
        });

        lockBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                tileConstructor.askForConstruct();
            }
        });

        resizeBtnXAdd.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                int data = (short) tileConstructor.getAABBSelectArea().getXLength() + 1;
                sendData(data);
            }

            @Override
            public void onLongClicked(View v) {
                int data = (short) tileConstructor.getAABBSelectArea().getXLength() + 16;
                sendData(data);
            }

            private void sendData(int data){
                data |= 0x00000000;
                sendIntMessageToServer(LMessage.IntDataSyncMessage.RESIZE_FLAG,data);
            }
        });

        resizeBtnYAdd.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                int data = (short) tileConstructor.getAABBSelectArea().getYLength() + 1;
                sendData(data);
            }

            @Override
            public void onLongClicked(View v) {
                int data = (short) tileConstructor.getAABBSelectArea().getYLength() + 16;
                sendData(data);
            }

            private void sendData(int data){
                data |= 0x00010000;
                sendIntMessageToServer(LMessage.IntDataSyncMessage.RESIZE_FLAG,data);
            }
        });

        resizeBtnZAdd.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                int data = (short) tileConstructor.getAABBSelectArea().getZLength() + 1;
                sendData(data);
            }

            @Override
            public void onLongClicked(View v) {
                int data = (short) tileConstructor.getAABBSelectArea().getZLength() + 16;
                sendData(data);
            }

            private void sendData(int data){
                data |= 0x00020000;
                sendIntMessageToServer(LMessage.IntDataSyncMessage.RESIZE_FLAG,data);
            }
        });

        resizeBtnXSub.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                int data = (short) tileConstructor.getAABBSelectArea().getXLength() - 1;
                if(data <= 0) return;
                sendData(data);
            }

            @Override
            public void onLongClicked(View v) {
                int data = (short) tileConstructor.getAABBSelectArea().getXLength() - 16;
                if(data <= 0) return;
                sendData(data);
            }

            private void sendData(int data){
                data |= 0x00040000;
                sendIntMessageToServer(LMessage.IntDataSyncMessage.RESIZE_FLAG,data);
            }
        });

        resizeBtnYSub.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                int data = (short) tileConstructor.getAABBSelectArea().getYLength() - 1;
                if(data <= 0) return;
                sendData(data);
            }

            @Override
            public void onLongClicked(View v) {
                int data = (short) tileConstructor.getAABBSelectArea().getYLength() - 16;
                if(data <= 0) return;
                sendData(data);
            }

            private void sendData(int data){
                data |= 0x00050000;
                sendIntMessageToServer(LMessage.IntDataSyncMessage.RESIZE_FLAG,data);
            }
        });

        resizeBtnZSub.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                int data = (short) tileConstructor.getAABBSelectArea().getZLength() - 1;
                if(data <= 0) return;
                sendData(data);
            }

            @Override
            public void onLongClicked(View v) {
                int data = (short) tileConstructor.getAABBSelectArea().getZLength() - 16;
                if(data <= 0) return;
                sendData(data);
            }

            private void sendData(int data){
                data |= 0x00060000;
                sendIntMessageToServer(LMessage.IntDataSyncMessage.RESIZE_FLAG,data);
            }
        });

        tileConstructor.setLockChanged(new IChangeListener.IChangeEvent() {
            @Override
            public void changed() {
                updateBtn();
            }
        });

        tileConstructor.setConstructChanged(new IChangeListener.IChangeEvent() {
            @Override
            public void changed() {
                updateBtn();
            }
        });

        tileConstructor.setAreaSizeChanged(new IChangeListener.IChangeEvent() {
            @Override
            public void changed() {
                xLengthTv.setText(String.valueOf(tileConstructor.getAABBSelectArea().getXLength()));
                yLengthTv.setText(String.valueOf(tileConstructor.getAABBSelectArea().getYLength()));
                zLengthTv.setText(String.valueOf(tileConstructor.getAABBSelectArea().getZLength()));
            }
        });

        tileConstructor.setSlotChanged(new IChangeListener.IChangeEvent() {
            @Override
            public void changed() {
                GuiConstructor.this.slotChanged();
            }
        });

        inventorySlots.addListener(this);
    }

    private void updateBtn(){
        if(tileConstructor.isLock()){
            if(!mc.player.getName().equals(tileConstructor.getLockUserName())) {
                lockBtn.setEnable(false);
                createBtn.setEnable(false);
            }else{
                lockBtn.setEnable(!tileConstructor.isConstructing());
                lockBtn.setText(R.name(R.id.text_constructor_btn_lock_unlock_id));
                createBtn.setEnable(!tileConstructor.isEmpty() && !tileConstructor.isConstructing());
            }
        }else{
            lockBtn.setText(R.name(R.id.text_constructor_btn_lock_lock_id));
            lockBtn.setEnable(true);
            createBtn.setEnable(false);
        }
    }

    private void sendIntMessageToServer(short flag,int data){
        LMessage.IntDataSyncMessage message = new LMessage.IntDataSyncMessage(flag,data);
        message.setPos(tileConstructor.getPos());
        NetworkHandler.INSTANCE.sendMessageToServer(message);
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
        this.mc.renderEngine.bindTexture(CONSTRUCTOR_GUI_TEXTURE);

        DrawHelper.drawTexturedModalRect(this.guiLeft, this.guiTop,this.zLevel, 0, 0, this.xSize, this.ySize,285,256);

        if(tileConstructor.isConstructing()){
            float progress = tileConstructor.getConstructProgress();

            this.drawTexturedModalRect(this.guiLeft + 225, this.guiTop + 58,
                    0,166,
                    (int) (24/* progress width - 1 */ * (progress == -1 ? 0:progress) ) + 1, 16);
        }
    }


    @Override
    public void onDestroy() {
        //remove listener
        tileConstructor.setLockChanged(null);
        tileConstructor.setAreaSizeChanged(null);
        tileConstructor.setConstructChanged(null);
        inventorySlots.removeListener(this);
        tileConstructor.setSlotChanged(null);

        if(!tileConstructor.isEmpty()&&
                !tileConstructor.getPrefabName().equals(nameEv.getText()))
        {
            LMessage.StringDataSyncMessage message = new LMessage.StringDataSyncMessage(LMessage.StringDataSyncMessage.GUI_CONSTRUCT_RENAME, nameEv.getText());
            message.setPos(tileConstructor.getPos());
            NetworkHandler.INSTANCE.sendMessageToServer(message);
        }
    }

    @Override
    protected List<GuiRegion> getExtraRegion() {
        return null;
    }

    private void slotChanged(){
        Slot prefabSlot = inventorySlots.getSlot(0);
        if(prefabSlot instanceof PrefabOnlySlot){
            if(!prefabSlot.getStack().isEmpty()) {
                String s = PrefabItem.getPrefabName(prefabSlot.getStack());
                nameEv.setEnabled(true);
                nameEv.setVisible(true);
                nameEv.setText(Strings.isNullOrEmpty(s) ?
                        R.name(R.id.text_constructor_editview_rename_hint_id) : s);

            } else {
                nameEv.setText(Strings.EMPTY);
                nameEv.setVisible(false);
                nameEv.setEnabled(false);
            }

            updateBtn();
        }
    }

    @Override
    public long getId() {
        return GUI_ID;
    }

    @Override
    public void sendAllContents(@Nonnull Container containerToSend, @Nonnull NonNullList<ItemStack> itemsList) {

        if(!tileConstructor.isEmpty()) {
            String name = tileConstructor.getPrefabName();
            nameEv.setText(Strings.isNullOrEmpty(name) ? "NoName" : name);
        }else{
            nameEv.setText(Strings.EMPTY);
            nameEv.setVisible(false);
            nameEv.setEnabled(false);
        }

        xLengthTv.setText(String.valueOf(tileConstructor.getAABBSelectArea().getXLength()));
        yLengthTv.setText(String.valueOf(tileConstructor.getAABBSelectArea().getYLength()));
        zLengthTv.setText(String.valueOf(tileConstructor.getAABBSelectArea().getZLength()));

        updateBtn();
    }

    @Override
    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
    }

    @Override
    public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
    }

    @Override
    public void sendAllWindowProperties(Container containerIn, IInventory inventory) {
    }
}
