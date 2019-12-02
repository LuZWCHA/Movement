package com.nowandfuture.mod.core.common.gui;

import com.google.common.base.Predicate;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityTimelineEditor;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.ChangeListener;
import com.nowandfuture.mod.core.common.gui.mygui.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.MyComboBox;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.PreviewView;
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
import com.nowandfuture.mod.utils.MathHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
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

    private TimeLineView timelineView;
    private PreviewView previewView;

    private MyButton reStartBtn;

    private MyComboBox comboBox;


    //for scale need one
    //for pos need three
    //for rotation need six (center{vet3},aixRotAngle{vet3})
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
    private MyButton importBtn;

    private MyLabel totalTimeLabel;
    private MyTextField totalTimeBox;

    private int currentType = -1;

    public GuiTimelineEditor(InventoryPlayer playerInv, TileEntityTimelineEditor tileMovementModule){
        super(new ContainerAnmEditor(playerInv,tileMovementModule));
        this.tileMovementModule = tileMovementModule;
        this.inventoryPlayer = playerInv;
        timeLine = new KeyFrameLine();

        timelineView = new TimeLineView(getRootView());
        previewView = new PreviewView(getRootView());
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
    }

    @Override
    public void onLoad() {
        timeLine = tileMovementModule.getLine().clone();
        //trans a clone of keyframe line not the origin one
        timelineView.init(timeLine,tileMovementModule.getPrefab());
        //test
        previewView.setPrefab(tileMovementModule.getPrefab());

        //test
        previewView.setX(100);
        previewView.setY(0);
        previewView.setWidth(100);
        previewView.setHeight(60);

        addView(previewView);

        timelineView.setX(8);
        timelineView.setY(16);
        timelineView.setWidth(88);
        timelineView.setHeight(48);

        timelineView.setSelectKeyFrameChange(new Consumer<KeyFrame>() {
            @Override
            public void accept(KeyFrame keyFrame) {
                updateEditGroup(keyFrame);
            }
        });

        addView(timelineView);

        List<String> list = new ArrayList<>();
        list.add(R.name(R.id.text_module_cmb_mode_rcy_id));
        list.add(R.name(R.id.text_module_cmb_mode_rcy_reserve_id));
        list.add(R.name(R.id.text_module_cmb_mode_one_id));
        list.add(R.name(R.id.text_module_cmb_mode_one_rcy_id));
        list.add(R.name(R.id.text_module_cmb_mode_stop_id));
        comboBox.setContents(list);
        comboBox.setX(46);
        comboBox.setY(105);
        comboBox.setWidth(40);
        comboBox.setLabelHeight(12);
        comboBox.setListHeight(30);
        comboBox.setIndex(tileMovementModule.getLine().getMode().ordinal());

        addView(comboBox);

        reStartBtn = createMyButton(12,100,26,20,R.name(R.id.text_module_btn_start_id));
        applyBtn = createMyButton(72,130,26,20,R.name(R.id.text_module_btn_apply_id));
        importBtn = createMyButton(12,130,26,20,R.name(R.id.text_module_btn_import_id));
        exportBtn = createMyButton(42,130,26,20,R.name(R.id.text_module_btn_export_id));
        keyTitle = createMyLabel(160,4,40,12,-1)
                .addLine("").enableBackDraw(false);

        totalTimeLabel = createMyLabel(12,80,30,18,-1)
                .addLine(R.name(R.id.text_module_lab_total_time_id)).enableBackDraw(false);

        totalTimeBox = createMyTextField(50,80,26,18,"");
        totalTimeBox.setValidator(new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                if("".equals(input)) {
                    return true;
                }
                try {
                    long a = Long.parseLong(input);
                    if(a <= 0) return false;
                }catch (Exception e){
                    return false;
                }
                return true;
            }
        });

        totalTimeBox.setText(String.valueOf(timeLine.getTotalTick()));

        bind(reStartBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                startOrStop();
            }
        });

        if(tileMovementModule.getLine().isEnable()){
            reStartBtn.displayString = R.name(R.id.text_module_btn_stop_id);
        }else{
            reStartBtn.displayString = R.name(R.id.text_module_btn_start_id);
        }

        bind(applyBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                applyTimeline();
            }
        });

        bind(exportBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                exportTimeline();
            }
        });

        bind(importBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                importTimeline();
            }
        });

        int color = -1;
        numLabel0 = createMyLabel(100,16,20,12,color);
        numLabel1 = createMyLabel(160,16,20,12,color);
        numLabel2 = createMyLabel(230,16,20,12,color);

        numLabel3 = createMyLabel(100,35,20,12,color);
        numLabel4 = createMyLabel(160,35,20,12,color);
        numLabel5 = createMyLabel(230,35,20,12,color);

        numLabel6 = createMyLabel(100,54,20,12,color);

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

        numBox3 = createMyTextField(120,35,20,12,"");
        numBox4 = createMyTextField(180,35,20,12,"");
        numBox5 = createMyTextField(240,35,20,12,"");

        numBox6 = createMyTextField(120,54,20,12,"");

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
                importBtn,
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
        slotChange();
    }

    private void importTimeline(){
        ItemStack stack = tileMovementModule.getStackInSlot(1);
        NBTTagCompound compound = stack.getTagCompound();
        if(compound!= null){
            timeLine.deserializeNBT(compound);
        }
    }

    private void exportTimeline(){
        MovementMessage.VoidMessage voidMessage = new MovementMessage.VoidMessage(MovementMessage.VoidMessage.GUI_EXPORT_TIMELINE_FLAG);
        voidMessage.setPos(tileMovementModule.getPos());
        NetworkHandler.INSTANCE.sendMessageToServer(voidMessage);
    }

    private void applyTimeline(){
        TimeLine.Mode mode = TimeLine.Mode.values()[comboBox.getSelectIndex()];
        long totalTime;
        if(!totalTimeBox.getText().isEmpty())
            totalTime = Long.parseLong(totalTimeBox.getText());
        else
            totalTime = timeLine.getTotalTick();

        timeLine.setMode(mode);
        timeLine.setTotalTick(totalTime);
        timeLine.setStep(1);

        NBTTagCompound compound = timeLine.serializeNBT(new NBTTagCompound());
        MovementMessage.NBTMessage nbtMessage = new MovementMessage.NBTMessage(MovementMessage.NBTMessage.GUI_APPLY_TIMELINE_FLAG,compound);
        nbtMessage.setPos(tileMovementModule.getPos());
        NetworkHandler.INSTANCE.sendMessageToServer(nbtMessage);
    }

    private void startOrStop(){
        MovementMessage.VoidMessage voidMessage = new MovementMessage.VoidMessage(MovementMessage.VoidMessage.GUI_RESTART_FLAG);
        voidMessage.setPos(tileMovementModule.getPos());
        if(tileMovementModule.getLine().isEnable()){
            tileMovementModule.getLine().setEnable(false);
            reStartBtn.displayString = R.name(R.id.text_module_btn_start_id);
        }else{
            tileMovementModule.getLine().setEnable(true);
            reStartBtn.displayString = R.name(R.id.text_module_btn_stop_id);
        }
        NetworkHandler.INSTANCE.sendMessageToServer(voidMessage);
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

                    keyTitle.setLine(0,R.name(R.id.text_module_lab_key_title1_id) + String.valueOf(keyFrame.getBeginTick()));
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

                    Vector3f vector3f = MathHelper.quaternionToEulerAngles(rotationKeyFrame.quaternion);
                    numBox3.setText(String.valueOf(vector3f.x));
                    numBox4.setText(String.valueOf(vector3f.y));
                    numBox5.setText(String.valueOf(vector3f.z));

                    numBox3.setVisible(true);
                    numBox4.setVisible(true);
                    numBox5.setVisible(true);

//                    numBox6.setText(String.valueOf(rotationKeyFrame.axisAngle4f.angle));
//                    numBox6.setVisible(true);

                    keyTitle.setLine(0, R.name(R.id.text_module_lab_key_title2_id) + String.valueOf(keyFrame.getBeginTick()));

                    break;
                case 2://scale
                    ScaleTransformNode.ScaleKeyFrame scaleKeyFrame = (ScaleTransformNode.ScaleKeyFrame) keyFrame;
                    numLabel0.setLine(0,R.name(R.id.text_module_lab_value_scale_id));
                    numBox0.setText(String.valueOf(scaleKeyFrame.scale));

                    numLabel0.visible = true;
                    numBox0.setVisible(true);

                    keyTitle.setLine(0, R.name(R.id.text_module_lab_key_title3_id) + String.valueOf(keyFrame.getBeginTick()));

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

            if(gui != totalTimeBox) {
                ((MyTextField) gui).setFocused(false);
                if (((MyTextField) gui).getText() == null || ((MyTextField) gui).getText().isEmpty())
                    ((MyTextField) gui).setText("0");
                submitValue();
            } else {
                long time = timeLine.getTotalTick();
                try {
                    time = Long.parseLong(totalTimeBox.getText());
                }catch (NumberFormatException e){
                    onError();
                }
                timeLine.setTotalTick(time);
            }
        }
    }

    private void submitValue(){
        KeyFrame keyFrame = timelineView.getSelectFrame();
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
                float rotX, rotY, rotZ;
                try {
                    centerX = Integer.parseInt(numBox0.getText());
                    centerY = Integer.parseInt(numBox1.getText());
                    centerZ = Integer.parseInt(numBox2.getText());

                    rotX = Float.parseFloat(numBox3.getText());
                    rotY = Float.parseFloat(numBox4.getText());
                    rotZ = Float.parseFloat(numBox5.getText());

                    //rotW = Float.parseFloat(numBox6.getText());

                    Quaternion quaternion = MathHelper.eulerAnglesToQuaternion(rotX,rotY,rotZ);

                    ((RotationTransformNode.RotationKeyFrame) keyFrame).center =
                            new BlockPos(centerX, centerY, centerZ);
                    ((RotationTransformNode.RotationKeyFrame) keyFrame).quaternion =
                            quaternion;

                } catch (NumberFormatException e) {
                    onError();
                }
            }
        }
    }

    private void slotChange(){
        if(tileMovementModule.getStackInSlot(0).isEmpty()){
            System.out.println("empty");
            //do nothing
        }else{
            System.out.println("full"); 
            //do nothing
        }

        if(tileMovementModule.getStackInSlot(1).isEmpty()){
            exportBtn.enabled = false;
            importBtn.enabled = false;
        }else{
            exportBtn.enabled = true;
            importBtn.enabled = true;
        }
    }

    @Override
    public long getId() {
        return GUI_ID;
    }

}
