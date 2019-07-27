package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.Items.PrefabItem;
import com.nowandfuture.mod.core.common.entities.TileEntityConstructor;
import com.nowandfuture.mod.core.common.gui.mygui.*;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyButton;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyLabel;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyTextField;
import com.nowandfuture.mod.core.common.gui.slots.PrefabOnlySlot;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.network.message.MovementMessage;
import com.nowandfuture.mod.utils.DrawHelper;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class GuiConstructor extends AbstractGuiContainer implements IContainerListener{
    public static final int GUI_ID = 0x102;

    private MyButton lockBtn;
    private MyButton createBtn;

    private MyButton resizeBtnXAdd;
    private MyButton resizeBtnYAdd;
    private MyButton resizeBtnZAdd;
    private MyButton resizeBtnXSub;
    private MyButton resizeBtnYSub;
    private MyButton resizeBtnZSub;

    private MyLabel xLengthLab;
    private MyLabel yLengthLab;
    private MyLabel zLengthLab;

    private MyTextField nameTexField;

    private static final ResourceLocation CONSTRUCTOR_GUI_TEXTURE = new ResourceLocation(Movement.MODID,"textures/gui/prefab_constructor.png");

    private final InventoryPlayer playerInventory;
    private final TileEntityConstructor tileConstructor;

    public GuiConstructor(InventoryPlayer playerInv, TileEntityConstructor tileConstructor) {
        super(new ContainerConstructor(playerInv,tileConstructor));
        this.playerInventory = playerInv;
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

        lockBtn = createMyButton(116, 52,48,24, R.name(R.id.text_constructor_btn_lock_lock_id));
        createBtn = createMyButton(172, 52,48,24, R.name(R.id.text_constructor_btn_create_create_id));
        nameTexField = createMyTextField(156, 15, 95, 12,"");

        resizeBtnXAdd = createMyButton(10,56,20,20,R.name(R.id.text_constructor_btn_resizex_add_id));
        resizeBtnXSub = createMyButton(80,56,20,20,R.name(R.id.text_constructor_btn_resizex_sub_id));
        resizeBtnYAdd = createMyButton(10,82,20,20,R.name(R.id.text_constructor_btn_resizey_add_id));
        resizeBtnYSub = createMyButton(80,82,20,20,R.name(R.id.text_constructor_btn_resizey_sub_id));
        resizeBtnZAdd = createMyButton(10,108,20,20,R.name(R.id.text_constructor_btn_resizez_add_id));
        resizeBtnZSub = createMyButton(80,108,20,20,R.name(R.id.text_constructor_btn_resizez_sub_id));

        xLengthLab = createMyLabel(40,56,30,20,-1);
        yLengthLab = createMyLabel(40,82,30,20,-1);
        zLengthLab = createMyLabel(40,108,30,20,-1);

        xLengthLab.enableBackDraw(false);
        yLengthLab.enableBackDraw(false);
        zLengthLab.enableBackDraw(false);

        nameTexField.setTextColor(16777215);
        nameTexField.setDisabledTextColour(-1);
        nameTexField.setEnableBackgroundDrawing(false);
        nameTexField.setMaxStringLength(40);

        xLengthLab.addLine("");
        yLengthLab.addLine("");
        zLengthLab.addLine("");
        xLengthLab.setCentered();
        yLengthLab.setCentered();
        zLengthLab.setCentered();

        addGuiCompoundsRelative(
                lockBtn,
                createBtn,
                resizeBtnXAdd,
                resizeBtnYAdd,
                resizeBtnZAdd,
                resizeBtnXSub,
                resizeBtnYSub,
                resizeBtnZSub,
                xLengthLab,
                yLengthLab,
                zLengthLab,
                nameTexField
        );

        bind(createBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                //construct at client
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

        bind(lockBtn,new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                tileConstructor.askForConstruct();
            }
        });

        bind(resizeBtnXAdd,new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                int data = (short) tileConstructor.getAABBSelectArea().getXLength() + 1;
                sendData(data);
            }

            @Override
            public void longClick(MyGui gui,int state, long lastTime) {
                int data = (short) tileConstructor.getAABBSelectArea().getXLength() + 16;
                sendData(data);
            }

            private void sendData(int data){
                data |= 0x00000000;
                sendIntMessageToServer(MovementMessage.IntDataSyncMessage.RESIZE_FLAG,data);
            }
        });

        bind(resizeBtnYAdd, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                int data = (short) tileConstructor.getAABBSelectArea().getYLength() + 1;
                sendData(data);
            }

            @Override
            public void longClick(MyGui gui,int state, long lastTime) {
                int data = (short) tileConstructor.getAABBSelectArea().getYLength() + 16;
                sendData(data);
            }

            private void sendData(int data){
                data |= 0x00010000;
                sendIntMessageToServer(MovementMessage.IntDataSyncMessage.RESIZE_FLAG,data);
            }
        });

        bind(resizeBtnZAdd, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                int data = (short) tileConstructor.getAABBSelectArea().getZLength() + 1;
                sendData(data);
            }
            @Override
            public void longClick(MyGui gui,int state, long lastTime) {
                int data = (short) tileConstructor.getAABBSelectArea().getZLength() + 16;
                sendData(data);
            }

            private void sendData(int data){
                data |= 0x00020000;
                sendIntMessageToServer(MovementMessage.IntDataSyncMessage.RESIZE_FLAG,data);
            }

        });

        bind(resizeBtnXSub, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                int data = (short) tileConstructor.getAABBSelectArea().getXLength() - 1;
                if(data <= 0) return;

                sendData(data);
            }
            @Override
            public void longClick(MyGui gui,int state, long lastTime) {
                int data = (short) tileConstructor.getAABBSelectArea().getXLength() - 16;
                if(data <= 0) return;

                sendData(data);
            }

            private void sendData(int data){
                data |= 0x00040000;
                sendIntMessageToServer(MovementMessage.IntDataSyncMessage.RESIZE_FLAG,data);
            }

        });

        bind(resizeBtnYSub, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                int data = (short) tileConstructor.getAABBSelectArea().getYLength() - 1;
                if(data <= 0) return;

                sendData(data);
            }

            @Override
            public void longClick(MyGui gui,int state, long lastTime) {
                int data = (short) tileConstructor.getAABBSelectArea().getYLength() - 16;
                if(data <= 0) return;

                sendData(data);
            }

            private void sendData(int data){
                data |= 0x00050000;
                sendIntMessageToServer(MovementMessage.IntDataSyncMessage.RESIZE_FLAG,data);
            }
        });

        bind(resizeBtnZSub, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                int data = (short) tileConstructor.getAABBSelectArea().getZLength() - 1;
                if(data <= 0) return;

                sendData(data);
            }

            @Override
            public void longClick(MyGui gui,int state, long lastTime) {
                int data = (short) tileConstructor.getAABBSelectArea().getZLength() - 16;
                if(data <= 0) return;

                sendData(data);
            }

            private void sendData(int data){
                data |= 0x00060000;
                sendIntMessageToServer(MovementMessage.IntDataSyncMessage.RESIZE_FLAG,data);
            }
        });

        tileConstructor.setLockChanged(new ChangeListener() {
            @Override
            public void changed() {
                updateBtn();
            }
        });

        tileConstructor.setConstructChanged(new ChangeListener() {
            @Override
            public void changed() {
                updateBtn();
            }
        });

        tileConstructor.setAreaSizeChanged(new ChangeListener() {
            @Override
            public void changed() {
                xLengthLab.setLine(0, String.valueOf(tileConstructor.getAABBSelectArea().getXLength()));
                yLengthLab.setLine(0, String.valueOf(tileConstructor.getAABBSelectArea().getYLength()));
                zLengthLab.setLine(0, String.valueOf(tileConstructor.getAABBSelectArea().getZLength()));
            }
        });

        tileConstructor.setSlotChanged(new ChangeListener() {
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
                lockBtn.enabled = false;
                createBtn.enabled = false;
            }else{
                lockBtn.enabled = !tileConstructor.isConstructing();
                lockBtn.displayString = R.name(R.id.text_constructor_btn_lock_unlock_id);;
                createBtn.enabled = !tileConstructor.isEmpty() && !tileConstructor.isConstructing();
            }
        }else{
            lockBtn.displayString = R.name(R.id.text_constructor_btn_lock_lock_id);
            lockBtn.enabled = true;
            createBtn.enabled = false;
        }
    }

    private void sendIntMessageToServer(short flag,int data){
        MovementMessage.IntDataSyncMessage message = new MovementMessage.IntDataSyncMessage(flag,data);
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
    public void onGuiClosed() {
        super.onGuiClosed();
        //remove listener
        tileConstructor.setLockChanged(null);
        tileConstructor.setAreaSizeChanged(null);
        tileConstructor.setConstructChanged(null);
        inventorySlots.removeListener(this);
        tileConstructor.setSlotChanged(null);

        if(!tileConstructor.isEmpty()&&
                !tileConstructor.getPrefabName().equals(nameTexField.getText()))                 {
            NetworkHandler.INSTANCE.sendMessageToServer(
                    new MovementMessage.RenamePrefabMessage(
                            tileConstructor.getPos(),
                            nameTexField.getText())

            );
        }
    }

    @Override
    protected void childLoseFocus(MyGui gui) {
        if(gui instanceof GuiTextField){
            if(gui == nameTexField){
                if(!tileConstructor.isEmpty() &&
                        !tileConstructor.getPrefabName().equals(nameTexField.getText())) {
                    NetworkHandler.INSTANCE.sendMessageToServer(
                            new MovementMessage.RenamePrefabMessage(
                                    tileConstructor.getPos(),
                                    nameTexField.getText())
                    );
                }

            }
        }
    }


    private void slotChanged(){

        Slot prefabSlot = inventorySlots.getSlot(0);
        if(prefabSlot instanceof PrefabOnlySlot){
            if(!prefabSlot.getStack().isEmpty()) {
                String s = ((PrefabItem)(prefabSlot.getStack().getItem())).getPrefabName(prefabSlot.getStack());
                nameTexField.setEnabled(true);
                nameTexField.setVisible(true);
                nameTexField.setText(Strings.isNullOrEmpty(s) ? "NoName" : s);

            } else {
                nameTexField.setText("");
                nameTexField.setVisible(false);
                nameTexField.setEnabled(false);
            }

            updateBtn();
        }

    }

    @Override
    public long getId() {
        return GUI_ID;
    }

    @Override
    public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
        if(!tileConstructor.isEmpty()) {
            String name = tileConstructor.getPrefabName();
            nameTexField.setText(Strings.isNullOrEmpty(name) ? "NoName" : name);
        }else{
            nameTexField.setText("");
            nameTexField.setVisible(false);
            nameTexField.setEnabled(false);
        }

        xLengthLab.setLine(0, String.valueOf(tileConstructor.getAABBSelectArea().getXLength()));
        yLengthLab.setLine(0, String.valueOf(tileConstructor.getAABBSelectArea().getYLength()));
        zLengthLab.setLine(0, String.valueOf(tileConstructor.getAABBSelectArea().getZLength()));

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
