package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityTimelineEditor;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.ChangeListener;
import com.nowandfuture.mod.core.common.gui.mygui.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.MyComboBox;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.TimeLineView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyButton;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyLabel;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyTextField;
import com.nowandfuture.mod.core.transformers.LinearTransformNode;
import com.nowandfuture.mod.core.transformers.RotationTransformNode;
import com.nowandfuture.mod.core.transformers.ScaleTransformNode;
import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.core.transformers.animation.TimeLine;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.network.message.MovementMessage;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.vecmath.AxisAngle4f;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class GuiTimelineEditor extends AbstractGuiContainer{
    public static final int GUI_ID = 0x103;
    private final static ResourceLocation MODULE_GUI_TEXTURE = new ResourceLocation(Movement.MODID,"textures/gui/movement_module.png");

    private TileEntityTimelineEditor tileMovementModule;
    private InventoryPlayer inventoryPlayer;
    private KeyFrameLine timeLine;

    private TimeLineView view;
    private MyButton reStartBtn;

    private MyComboBox comboBox;

    //for scale need one
    //for pos need three
    //for rotation need seven (center,aix,angle)
    private MyLabel numLabel0;
    private MyTextField numBox0;
    private MyLabel numLabel1;
    private MyTextField numBox1;
    private MyLabel numLabel2;
    private MyTextField numBox2;
    private MyLabel numLabel3;
    private MyTextField numBox3;
    private MyLabel numLabel4;
    private MyTextField numBox4;
    private MyLabel numLabel5;
    private MyTextField numBox5;
    private MyLabel numLabel6;
    private MyTextField numBox6;

    private MyLabel keyTitle;
    private MyButton applyBtn;
    private MyButton exportBtn;

    private MyLabel totalTimeLabel;
    private MyTextField totalTimeBox;

    private int currentType = -1;

    public GuiTimelineEditor(InventoryPlayer playerInv, TileEntityTimelineEditor tileMovementModule){
        super(new ContainerAnmEditor(playerInv,tileMovementModule));
        this.tileMovementModule = tileMovementModule;
        this.inventoryPlayer = playerInv;
        timeLine = new KeyFrameLine();
        timeLine.setTotalTick(100);
        timeLine.setMode(TimeLine.Mode.CYCLE_RESTART);

        view = new TimeLineView(getRootView());
        comboBox = new MyComboBox(getRootView());

        tileMovementModule.setSlotChanged(new ChangeListener() {
            @Override
            public void changed() {
                GuiTimelineEditor.this.slotChange();
            }
        });

        xSize = 270;
        ySize = 166;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
//        timeLine.update();
    }

    @Override
    public void onLoad() {
        timeLine = tileMovementModule.getLine().clone();
        //trans a clone of keyframe line not the origin one
        view.init(timeLine,tileMovementModule.getPrefab());

        view.setX(8);
        view.setY(16);
        view.setWidth(88);
        view.setHeight(48);

        view.setSelectKeyFrameChange(new Consumer<KeyFrame>() {
            @Override
            public void accept(KeyFrame keyFrame) {
                updateEditGroup(keyFrame);
            }
        });

        addView(view);

        List<String> list = new ArrayList<>();
        list.add(R.name(R.id.text_module_cmb_mode_rcy_id));
        list.add(R.name(R.id.text_module_cmb_mode_rcy_reserve_id));
        list.add(R.name(R.id.text_module_cmb_mode_one_id));
        list.add(R.name(R.id.text_module_cmb_mode_one_rcy_id));
        list.add(R.name(R.id.text_module_cmb_mode_stop_id));
        comboBox.setContents(list);
        comboBox.setX(46);
        comboBox.setY(104);
        comboBox.setWidth(40);
        comboBox.setLabelHeight(12);
        comboBox.setListHeight(30);
        comboBox.setIndex(tileMovementModule.getLine().getMode().ordinal());
        comboBox.setOnItemClicked(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {

            }
        });

        addView(comboBox);

        reStartBtn = createMyButton(12,100,26,20,R.name(R.id.text_module_btn_start_id));
        applyBtn = createMyButton(12,130,26,20,R.name(R.id.text_module_btn_apply_id));
        exportBtn = createMyButton(42,130,26,20,R.name(R.id.text_module_btn_export_id));
        keyTitle = createMyLabel(160,4,40,12,-1)
                .addLine("keyframe").enableBackDraw(false);

        totalTimeLabel = createMyLabel(12,80,30,18,-1)
                .addLine(R.name(R.id.text_module_lab_total_time_id)).enableBackDraw(false);

        totalTimeBox = createMyTextField(44,80,26,18,"");
        totalTimeBox.setText(String.valueOf(timeLine.getTotalTick()));

        bind(reStartBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                MovementMessage.VoidMessage voidMessage = new MovementMessage.VoidMessage(MovementMessage.VoidMessage.GUI_RESTART_FLAG);
                voidMessage.setPos(tileMovementModule.getPos());
                NetworkHandler.INSTANCE.sendMessageToServer(voidMessage);
            }
        });

        bind(applyBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                TimeLine.Mode mode = TimeLine.Mode.values()[comboBox.getSelectIndex()];
                long totalTime = Long.parseLong(totalTimeBox.getText());
                if(totalTime <= 0) {
                    onError();
                    return;
                }

                timeLine.setMode(mode);
                timeLine.setEnable(true);
                timeLine.setTotalTick(totalTime);
                timeLine.setStep(1);

                NBTTagCompound compound = timeLine.serializeNBT(new NBTTagCompound());
                MovementMessage.NBTMessage nbtMessage = new MovementMessage.NBTMessage(MovementMessage.NBTMessage.GUI_APPLY_TIMELINE_FLAG,compound);
                nbtMessage.setPos(tileMovementModule.getPos());
                NetworkHandler.INSTANCE.sendMessageToServer(nbtMessage);
            }
        });

        bind(exportBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                MovementMessage.VoidMessage voidMessage = new MovementMessage.VoidMessage(MovementMessage.VoidMessage.GUI_EXPORT_TIMELINE_FLAG);
                voidMessage.setPos(tileMovementModule.getPos());
                NetworkHandler.INSTANCE.sendMessageToServer(voidMessage);
            }
        });

        int color = -1;
        numLabel0 = createMyLabel(100,16,20,12,color);
        numLabel1 = createMyLabel(160,16,20,12,color);
        numLabel2 = createMyLabel(230,16,20,12,color);

        numLabel3 = createMyLabel(100,37,20,12,color);
        numLabel4 = createMyLabel(160,37,20,12,color);
        numLabel5 = createMyLabel(230,37,20,12,color);

        numLabel6 = createMyLabel(100,58,20,12,color);

        numLabel0.addLine("").enableBackDraw(false);
        numLabel1.addLine("").enableBackDraw(false);
        numLabel2.addLine("").enableBackDraw(false);
        numLabel3.addLine("").enableBackDraw(false);
        numLabel4.addLine("").enableBackDraw(false);
        numLabel5.addLine("").enableBackDraw(false);
        numLabel6.addLine("").enableBackDraw(false);

        numBox0 = createMyTextField(120,16,20,12,"");
        numBox1 = createMyTextField(180,16,20,12,"");
        numBox2 = createMyTextField(240,16,20,12,"");

        numBox3 = createMyTextField(120,37,20,12,"");
        numBox4 = createMyTextField(180,37,20,12,"");
        numBox5 = createMyTextField(240,37,20,12,"");

        numBox6 = createMyTextField(120,58,20,12,"");

        Function<Character,Boolean> numFilter = new Function<Character, Boolean>() {
            @Override
            public Boolean apply(Character character) {
                int keyCode = Keyboard.getEventKey();
                return Character.isDigit(character) || character == '-' || character == '.' || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK ||
                        keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT;
            }
        };

        numBox0.setFilter(numFilter);
        numBox1.setFilter(numFilter);
        numBox2.setFilter(numFilter);
        numBox3.setFilter(numFilter);
        numBox4.setFilter(numFilter);
        numBox5.setFilter(numFilter);
        numBox6.setFilter(numFilter);

        addGuiCompoundsRelative(
                reStartBtn,
                applyBtn,
                exportBtn,
                keyTitle,
                totalTimeLabel,
                totalTimeBox,
                numBox0,
                numBox1,
                numBox2,
                numBox3,
                numBox4,
                numBox5,
                numBox6,
                numLabel0,
                numLabel1,
                numLabel2,
                numLabel3,
                numLabel4,
                numLabel5,
                numLabel6
        );
        clearAllEnum();
    }

    private void onError(){

    }

    public void updateEditGroup(KeyFrame keyFrame){
        clearAllEnum();
        keyTitle.setLine(0, "");

        if(keyFrame != null){

            switch (keyFrame.getType()){
                case 0://linear
                    LinearTransformNode.LinearKeyFrame linearKeyFrame = (LinearTransformNode.LinearKeyFrame) keyFrame;
                    numLabel0.setLine(0,R.name(R.id.text_module_lab_value_x_id));
                    numLabel1.setLine(0,R.name(R.id.text_module_lab_value_y_id));
                    numLabel2.setLine(0,R.name(R.id.text_module_lab_value_z_id));
                    numLabel0.visible = true;
                    numLabel1.visible = true;
                    numLabel2.visible = true;

                    numBox0.setText(String.valueOf(linearKeyFrame.curPos.x));
                    numBox1.setText(String.valueOf(linearKeyFrame.curPos.y));
                    numBox2.setText(String.valueOf(linearKeyFrame.curPos.z));

                    numBox0.setVisible(true);
                    numBox1.setVisible(true);
                    numBox2.setVisible(true);

                    keyTitle.setLine(0,"平移（时间）" + String.valueOf(keyFrame.getBeginTick()));
                    break;
                case 1://rotation
                    RotationTransformNode.RotationKeyFrame rotationKeyFrame = (RotationTransformNode.RotationKeyFrame) keyFrame;

                    numLabel0.setLine(0,R.name(R.id.text_module_lab_value_x_id));
                    numLabel1.setLine(0,R.name(R.id.text_module_lab_value_y_id));
                    numLabel2.setLine(0,R.name(R.id.text_module_lab_value_z_id));
                    numLabel0.visible = true;
                    numLabel1.visible = true;
                    numLabel2.visible = true;

                    numLabel3.setLine(0,R.name(R.id.text_module_lab_value_x_id));
                    numLabel4.setLine(0,R.name(R.id.text_module_lab_value_y_id));
                    numLabel5.setLine(0,R.name(R.id.text_module_lab_value_z_id));
                    numLabel3.visible = true;
                    numLabel4.visible = true;
                    numLabel5.visible = true;

                    numLabel6.setLine(0,R.name(R.id.text_module_lab_value_angle_id));
                    numLabel6.visible = true;

                    numBox0.setText(String.valueOf(rotationKeyFrame.center.getX()));
                    numBox1.setText(String.valueOf(rotationKeyFrame.center.getY()));
                    numBox2.setText(String.valueOf(rotationKeyFrame.center.getZ()));

                    numBox0.setVisible(true);
                    numBox1.setVisible(true);
                    numBox2.setVisible(true);

                    numBox3.setText(String.valueOf(rotationKeyFrame.axisAngle4f.x));
                    numBox4.setText(String.valueOf(rotationKeyFrame.axisAngle4f.y));
                    numBox5.setText(String.valueOf(rotationKeyFrame.axisAngle4f.z));

                    numBox3.setVisible(true);
                    numBox4.setVisible(true);
                    numBox5.setVisible(true);

                    numBox6.setText(String.valueOf(rotationKeyFrame.axisAngle4f.angle));
                    numBox6.setVisible(true);

                    keyTitle.setLine(0, "旋转（时间）" + String.valueOf(keyFrame.getBeginTick()));

                    break;
                case 2://scale
                    ScaleTransformNode.ScaleKeyFrame scaleKeyFrame = (ScaleTransformNode.ScaleKeyFrame) keyFrame;
                    numLabel0.setLine(0,R.name(R.id.text_module_lab_value_scale_id));
                    numBox0.setText(String.valueOf(scaleKeyFrame.scale));

                    numLabel0.visible = true;
                    numBox0.setVisible(true);

                    keyTitle.setLine(0, "缩放（时间）" + String.valueOf(keyFrame.getBeginTick()));

                    break;
            }
            currentType = keyFrame.getType();
        }
    }

    //mojang gui design ? no design
    public void clearAllEnum(){
        currentType = -1;

        numBox0.setVisible(false);
        numBox1.setVisible(false);
        numBox2.setVisible(false);
        numBox3.setVisible(false);
        numBox4.setVisible(false);
        numBox5.setVisible(false);
        numBox6.setVisible(false);

        numLabel0.visible = false;
        numLabel1.visible = false;
        numLabel2.visible = false;
        numLabel3.visible = false;
        numLabel4.visible = false;
        numLabel5.visible = false;
        numLabel6.visible = false;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        tileMovementModule.setSlotChanged(null);
        comboBox.setOnItemClicked(null);
    }

    @Override
    protected void onDrawBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(MODULE_GUI_TEXTURE);

        DrawHelper.drawTexturedModalRect(this.guiLeft, this.guiTop,this.zLevel, 0, 0, this.xSize, this.ySize,this.xSize,this.ySize);

        this.mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        drawPrefab(partialTicks);
    }

    private void drawPrefab(float partialTicks){
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();

        if(tileMovementModule.getModuleBase().getPrefab().isLocalWorldInit())
            tileMovementModule.getModuleBase().renderForGui(partialTicks,(float) (timeLine.getProgress(partialTicks) * 360));
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.popMatrix();
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX,mouseY);
    }

    @Override
    protected void childLoseFocus(MyGui gui) {
        if(gui instanceof MyTextField){
            //MyTextField will be focused when click,but not auto lose focus by this system;
            ((MyTextField) gui).setFocused(false);
            if(gui != totalTimeBox)
                submitValue();
            else {
                try {
                    long time = Long.parseLong(totalTimeBox.getText());
                    timeLine.setTotalTick(time);
                }catch (NumberFormatException e){
                    onError();
                }
            }
        }
    }

    private void submitValue(){
        KeyFrame keyFrame = view.getSelectFrame();
        if(currentType == KeyFrame.KeyFrameType.LINEAR.ordinal()){
            if(keyFrame instanceof LinearTransformNode.LinearKeyFrame){
                double x,y,z;
                try {
                    x = Double.parseDouble(numBox0.getText());
                    y = Double.parseDouble(numBox1.getText());
                    z = Double.parseDouble(numBox2.getText());
                    ((LinearTransformNode.LinearKeyFrame) keyFrame).curPos =
                            new Vec3d(x,y,z);
                }catch (NumberFormatException e){
                    onError();
                }
            }
        }else if(currentType == KeyFrame.KeyFrameType.SCALE.ordinal()){
            if(keyFrame instanceof ScaleTransformNode.ScaleKeyFrame) {
                float scale;
                try {
                    scale = Float.parseFloat(numBox0.getText());

                    ((ScaleTransformNode.ScaleKeyFrame) keyFrame).scale = scale;
                }catch (NumberFormatException e){
                    onError();
                }
            }
        }else if(currentType == KeyFrame.KeyFrameType.ROTATION.ordinal()){
            if(keyFrame instanceof RotationTransformNode.RotationKeyFrame) {
                int centerX, centerY, centerZ;
                float rotX, rotY, rotZ,angle;
                try {
                    centerX = Integer.parseInt(numBox0.getText());
                    centerY = Integer.parseInt(numBox1.getText());
                    centerZ = Integer.parseInt(numBox2.getText());

                    rotX = Float.parseFloat(numBox3.getText());
                    rotY = Float.parseFloat(numBox4.getText());
                    rotZ = Float.parseFloat(numBox5.getText());

                    angle = Float.parseFloat(numBox6.getText());

                    ((RotationTransformNode.RotationKeyFrame) keyFrame).center =
                            new BlockPos(centerX, centerY, centerZ);
                    ((RotationTransformNode.RotationKeyFrame) keyFrame).axisAngle4f =
                            new AxisAngle4f(rotX, rotY, rotZ,angle);

                } catch (NumberFormatException e) {
                    onError();
                }
            }
        }


    }

    private void slotChange(){

    }


    @Override
    public long getId() {
        return GUI_ID;
    }

}
