package com.nowandfuture.mod.core.common.gui;

import com.google.common.base.Predicate;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityTimelineEditor;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.ChangeListener;
import com.nowandfuture.mod.core.common.gui.mygui.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.FrameLayout;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.ComboBox;
import com.nowandfuture.mod.core.common.gui.custom.PreviewView;
import com.nowandfuture.mod.core.common.gui.custom.TimeLineView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyButton;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyLabel;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyTextField;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.NumberBox;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.SliderView;
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
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class GuiTimelineEditor extends AbstractGuiContainer{
    public static final int GUI_ID = 0x103;

    public static final int SLOT_PREFAB = 0;
    public static final int SLOT_TIMELINE = 1;
    private final static ResourceLocation MODULE_GUI_TEXTURE =
            new ResourceLocation(Movement.MODID,
                    "textures/gui/movement_module.png");
    private final FrameLayout rightLayout;

    private TileEntityTimelineEditor tileMovementModule;
    private InventoryPlayer inventoryPlayer;
    private KeyFrameLine timeLine;

    private TimeLineView timelineView;
    private PreviewView previewView;

    private MyButton reStartBtn;

    private ComboBox comboBox;

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

    private MyLabel keyTitle;
    private MyButton applyBtn,exportBtn,importBtn;

    private MyLabel totalTimeLabel;
    private MyTextField totalTimeBox;

    private SliderView xSliderView,ySliderView,zSliderView;
    private NumberBox xOffset,yOffset,zOffset;

    private MyButton resetBtn,recoverBtn;


    private int currentType = -1;

    public GuiTimelineEditor(InventoryPlayer playerInv, TileEntityTimelineEditor tileMovementModule){
        super(new ContainerAnmEditor(playerInv,tileMovementModule));
        this.tileMovementModule = tileMovementModule;
        this.inventoryPlayer = playerInv;
        timeLine = new KeyFrameLine();

        timelineView = new TimeLineView(getRootView());
        comboBox = new ComboBox(getRootView());

        rightLayout =new FrameLayout(getRootView());

        previewView = new PreviewView(getRootView(), rightLayout);
        xSliderView = new SliderView(getRootView(), rightLayout);
        ySliderView = new SliderView(getRootView(), rightLayout);
        zSliderView = new SliderView(getRootView(), rightLayout);
        xOffset = new NumberBox(getRootView(),rightLayout);
        yOffset = new NumberBox(getRootView(),rightLayout);
        zOffset = new NumberBox(getRootView(),rightLayout);

        rightLayout.addChildren(previewView,xSliderView,ySliderView,zSliderView,
                xOffset,yOffset,zOffset);

        tileMovementModule.setSlotChanged(new ChangeListener.ChangeEvent() {
            @Override
            public void changed(int index) {
                GuiTimelineEditor.this.slotChange(index);
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

        rightLayout.setX(xSize);
        rightLayout.setY(0);
        rightLayout.setWidth(100);
        rightLayout.setHeight(220);

        previewView.setPrefab(tileMovementModule.getPrefab());

        previewView.setX(0);
        previewView.setY(0);
        previewView.setWidth(100);
        previewView.setHeight(80);

        resetBtn = createMyButton(270,90,50,16,R.name(R.id.text_module_btn_preview_reset_id));
        recoverBtn = createMyButton(324,90,50,16,R.name(R.id.text_module_btn_preview_recover_id));

        xSliderView.setRange(180,-180,0);
        xSliderView.setX(0);
        xSliderView.setY(110);
        xSliderView.setWidth(100);
        xSliderView.setHeight(10);
        xSliderView.setProgressChanging(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                previewView.setXAngle(aFloat);
                numBox3.setText(String.valueOf(previewView.getXAngle()));
                previewView.saveToFrame();
            }
        });

        ySliderView.setRange(180,-180,0);
        ySliderView.setX(0);
        ySliderView.setY(120);
        ySliderView.setWidth(100);
        ySliderView.setHeight(10);
        ySliderView.setProgressChanging(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                previewView.setYAngle(aFloat);
                numBox4.setText(String.valueOf(previewView.getYAngle()));
                previewView.saveToFrame();
            }
        });

        zSliderView.setRange(180,-180,0);
        zSliderView.setX(0);
        zSliderView.setY(130);
        zSliderView.setWidth(100);
        zSliderView.setHeight(10);
        zSliderView.setProgressChanging(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                previewView.setZAngle(aFloat);
                numBox5.setText(String.valueOf(previewView.getZAngle()));
                previewView.saveToFrame();
            }
        });

        xOffset.setX(0);
        xOffset.setY(160);
        xOffset.setWidth(100);
        xOffset.setHeight(10);
        yOffset.setX(0);
        yOffset.setY(180);
        yOffset.setWidth(100);
        yOffset.setHeight(10);
        zOffset.setX(0);
        zOffset.setY(200);
        zOffset.setWidth(100);
        zOffset.setHeight(10);

        xOffset.setDefaultValue(0);
        yOffset.setDefaultValue(0);
        zOffset.setDefaultValue(0);
        xOffset.setValueChangedListener(new Consumer<Integer>() {
            @Override
            public void accept(Integer v) {
                numBox0.setText(String.valueOf(v));
                previewView.setOffsetX(v);
                submitValue();
            }
        });
        yOffset.setValueChangedListener(new Consumer<Integer>() {
            @Override
            public void accept(Integer v) {
                numBox1.setText(String.valueOf(v));
                previewView.setOffsetY(v);
                submitValue();
            }
        });
        zOffset.setValueChangedListener(new Consumer<Integer>() {
            @Override
            public void accept(Integer v) {
                numBox2.setText(String.valueOf(v));
                previewView.setOffsetZ(v);
                submitValue();
            }
        });

        addView(rightLayout);

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
                .enableBackDraw(false);

        totalTimeLabel = createMyLabel(12,80,30,18,-1)
                .setFirst(R.name(R.id.text_module_lab_total_time_id))
                .enableBackDraw(false);

        totalTimeBox = createMyTextField(50,80,26,18,"");
        totalTimeBox.setValidator(new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                if("".equals(input) || input ==null) {
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

        bind(resetBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                previewView.resetView();
            }
        });

        bind(recoverBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                previewView.reload();
                previewView.saveToFrame();
                xSliderView.setProgress(0);
                ySliderView.setProgress(0);
                zSliderView.setProgress(0);
            }
        });

        int color = -1;
        numLabel0 = createMyLabel(100,16,20,12,color);
        numLabel1 = createMyLabel(160,16,20,12,color);
        numLabel2 = createMyLabel(230,16,20,12,color);

        numLabel3 = createMyLabel(100,35,20,12,color);
        numLabel4 = createMyLabel(160,35,20,12,color);
        numLabel5 = createMyLabel(230,35,20,12,color);

//        numLabel6 = createMyLabel(100,54,20,12,color);

        numLabel0.enableBackDraw(false);
        numLabel1.enableBackDraw(false);
        numLabel2.enableBackDraw(false);
        numLabel3.enableBackDraw(false);
        numLabel4.enableBackDraw(false);
        numLabel5.enableBackDraw(false);
//        numLabel6.addLine("").enableBackDraw(false);

        numBox0 = createMyTextField(120,16,20,12,"");
        numBox1 = createMyTextField(180,16,20,12,"");
        numBox2 = createMyTextField(240,16,20,12,"");

        numBox3 = createMyTextField(120,35,20,12,"");
        numBox4 = createMyTextField(180,35,20,12,"");
        numBox5 = createMyTextField(240,35,20,12,"");

//        numBox6 = createMyTextField(120,54,20,12,"");

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
//        numBox6.setFilter(numFilter);

        addGuiCompoundsRelative(
                reStartBtn,
                applyBtn,
                exportBtn,
                importBtn,
                recoverBtn,
                resetBtn,
                keyTitle,
                totalTimeLabel,
                totalTimeBox,
                numBox0,
                numBox1,
                numBox2,
                numBox3,
                numBox4,
                numBox5,
//                numBox6,
                numLabel0,
                numLabel1,
                numLabel2,
                numLabel3,
                numLabel4,
                numLabel5
//                numLabel6
        );
        clearAllUIState();
        slotChange(SLOT_TIMELINE);//init import and export buttons
    }

    private void importTimeline(){
        ItemStack stack = tileMovementModule.getStackInSlot(1);
        NBTTagCompound compound = stack.getTagCompound();
        if(compound!= null){
            timeLine.deserializeNBT(compound);
        }
    }

    private void exportTimeline(){
        MovementMessage.VoidMessage voidMessage =
                new MovementMessage.VoidMessage(MovementMessage.VoidMessage.GUI_EXPORT_TIMELINE_FLAG);
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
        MovementMessage.NBTMessage nbtMessage =
                new MovementMessage.NBTMessage(MovementMessage.NBTMessage.GUI_APPLY_TIMELINE_FLAG,compound);
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
        clearAllUIState();
        keyTitle.empty();
        if(keyFrame != null){

            switch (keyFrame.getType()){
                case 0://linear
                    LinearTransformNode.LinearKeyFrame linearKeyFrame = (LinearTransformNode.LinearKeyFrame) keyFrame;
                    numLabel0.setFirst(R.name(R.id.text_module_lab_value_x_id));
                    numLabel1.setFirst(R.name(R.id.text_module_lab_value_y_id));
                    numLabel2.setFirst(R.name(R.id.text_module_lab_value_z_id));

                    numBox0.setText(String.valueOf(linearKeyFrame.curPos.x));
                    numBox1.setText(String.valueOf(linearKeyFrame.curPos.y));
                    numBox2.setText(String.valueOf(linearKeyFrame.curPos.z));

                    keyTitle.setFirst(R.name(R.id.text_module_lab_key_title1_id) + String.valueOf(keyFrame.getBeginTick()));

                    setVisible(true,numBox0,numBox1,numBox2,numLabel0,numLabel1,numLabel2);
                    break;
                case 1://rotation
                    RotationTransformNode.RotationKeyFrame rotationKeyFrame = (RotationTransformNode.RotationKeyFrame) keyFrame;

                    numLabel0.setFirst(R.name(R.id.text_module_lab_value_x_id));
                    numLabel1.setFirst(R.name(R.id.text_module_lab_value_y_id));
                    numLabel2.setFirst(R.name(R.id.text_module_lab_value_z_id));

                    numLabel3.setFirst(R.name(R.id.text_module_lab_value_x_id));
                    numLabel4.setFirst(R.name(R.id.text_module_lab_value_y_id));
                    numLabel5.setFirst(R.name(R.id.text_module_lab_value_z_id));

//                    numLabel6.setLine(0,R.name(R.id.text_module_lab_value_angle_id));
//                    numLabel6.visible = true;

                    numBox0.setText(String.valueOf(rotationKeyFrame.center.getX()));
                    numBox1.setText(String.valueOf(rotationKeyFrame.center.getY()));
                    numBox2.setText(String.valueOf(rotationKeyFrame.center.getZ()));

                    Vector3f vector3f = new Vector3f(rotationKeyFrame.rotX,rotationKeyFrame.rotY,rotationKeyFrame.rotZ);
                    numBox3.setText(String.valueOf(vector3f.x));
                    numBox4.setText(String.valueOf(vector3f.y));
                    numBox5.setText(String.valueOf(vector3f.z));

                    keyTitle.setFirst(R.name(R.id.text_module_lab_key_title2_id) + keyFrame.getBeginTick());

                    previewView.setPreKeyFrame((RotationTransformNode.RotationKeyFrame) timeLine.getPreFrame(rotationKeyFrame)
                            .orElse(new RotationTransformNode.RotationKeyFrame()));

                    previewView.setNextKeyFrame(rotationKeyFrame);

                    previewView.setXAngle(rotationKeyFrame.rotX);
                    previewView.setYAngle(rotationKeyFrame.rotY);
                    previewView.setZAngle(rotationKeyFrame.rotZ);

                    previewView.setOffsetX(rotationKeyFrame.center.getX());
                    previewView.setOffsetY(rotationKeyFrame.center.getY());
                    previewView.setOffsetZ(rotationKeyFrame.center.getZ());

                    xSliderView.setProgress(previewView.getXAngle());
                    ySliderView.setProgress(previewView.getYAngle());
                    zSliderView.setProgress(previewView.getZAngle());

                    xOffset.setCurValue(rotationKeyFrame.center.getX());
                    yOffset.setCurValue(rotationKeyFrame.center.getY());
                    zOffset.setCurValue(rotationKeyFrame.center.getZ());

                    if(tileMovementModule.getPrefab() == null || tileMovementModule.getPrefab().getMinAABB() == null) {
                        xOffset.setMax(xOffset.getCurValue());
                        yOffset.setMax(yOffset.getCurValue());
                        zOffset.setMax(zOffset.getCurValue());
                    }else{
                        AxisAlignedBB a = tileMovementModule.getPrefab().getMinAABB();
                        xOffset.setMax(256);
                        yOffset.setMax(256);
                        zOffset.setMax(256);
                    }

                    numBox0.setEnabled(false);
                    numBox1.setEnabled(false);
                    numBox2.setEnabled(false);
                    numBox3.setEnabled(false);
                    numBox4.setEnabled(false);
                    numBox5.setEnabled(false);

                    setVisible(true,
                            numLabel3,numLabel4,numLabel5,numLabel0,numLabel1,numLabel2,
                            numBox0,numBox1,numBox2,numBox3,numBox4,numBox5,
                            rightLayout,resetBtn,recoverBtn);
                    break;
                case 2://scale
                    ScaleTransformNode.ScaleKeyFrame scaleKeyFrame = (ScaleTransformNode.ScaleKeyFrame) keyFrame;
                    numLabel0.setFirst(R.name(R.id.text_module_lab_value_scale_id));
                    numBox0.setText(String.valueOf(scaleKeyFrame.scale));

                    keyTitle.setFirst(R.name(R.id.text_module_lab_key_title3_id) + String.valueOf(keyFrame.getBeginTick()));

                    setVisible(true,numLabel0,numBox0);
                    break;
            }
            currentType = keyFrame.getType();
        }
    }

    public void clearAllUIState(){
        currentType = -1;

        numBox0.setEnabled(true);
        numBox1.setEnabled(true);
        numBox2.setEnabled(true);
        numBox3.setEnabled(true);
        numBox4.setEnabled(true);
        numBox5.setEnabled(true);

        setVisible(false,
                numLabel3,numLabel4,numLabel5,numLabel0,numLabel1,numLabel2,
                numBox0,numBox1,numBox2,numBox3,numBox4,numBox5,
                rightLayout,resetBtn,recoverBtn);
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
            //MyTextField will be focused when clicking,but not auto lose focus by this system;

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

                    ((RotationTransformNode.RotationKeyFrame) keyFrame).center =
                            new BlockPos(centerX, centerY, centerZ);
                    ((RotationTransformNode.RotationKeyFrame) keyFrame).rotX = rotX;
                    ((RotationTransformNode.RotationKeyFrame) keyFrame).rotY = rotY;
                    ((RotationTransformNode.RotationKeyFrame) keyFrame).rotZ = rotZ;

                } catch (NumberFormatException e) {
                    onError();
                }
            }
        }
    }

    private void slotChange(int index){
        if(index == SLOT_PREFAB) {
            if (tileMovementModule.getStackInSlot(index).isEmpty()) {
                previewView.setPrefab(null);
                //do nothing
            } else {
                previewView.setPrefab(tileMovementModule.getPrefab());
                //do nothing
            }
        }else{
            if(tileMovementModule.getStackInSlot(index).isEmpty()){
                exportBtn.enabled = false;
                importBtn.enabled = false;
            }else{
                exportBtn.enabled = true;
                importBtn.enabled = true;
            }
        }
    }

    @Override
    public long getId() {
        return GUI_ID;
    }

}
