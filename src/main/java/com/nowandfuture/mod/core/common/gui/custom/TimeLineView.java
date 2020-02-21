package com.nowandfuture.mod.core.common.gui.custom;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.transformers.LinearTransformNode;
import com.nowandfuture.mod.core.transformers.RotationTransformNode;
import com.nowandfuture.mod.core.transformers.ScaleTransformNode;
import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.utils.DrawHelper;
import com.nowandfuture.mod.utils.math.MathHelper;
import net.minecraft.client.gui.Gui;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Consumer;

public class TimeLineView extends View {

    private int timeLineOffsetY;
    private int distance = 10;

    private KeyFrameLine keyFrameLine;
    private AbstractPrefab prefab;

    private Consumer<KeyFrame> selectKeyFrameChange;

    private KeyFrame selectFrame;

    private int keyFrameSize = 3;

    private long hoverTime = -1;

    //for draw line
    private int linearLine;
    private int rotationLine;
    private int scaleLine;
    private int linePadding = 2;
    private long curTick;

    public TimeLineView(@Nonnull RootView rootView){
        super(rootView);
    }

    public void init(KeyFrameLine keyFrameLine,AbstractPrefab prefab){
        this.keyFrameLine = keyFrameLine;
        this.prefab = prefab;
    }

    @Override
    public void onDraw(int mouseX, int mouseY, float partialTicks) {
        this.timeLineOffsetY = this.getHeight() / 3;
        this.linearLine = KeyFrame.KeyFrameType.LINEAR.ordinal() * distance + timeLineOffsetY;
        this.rotationLine = KeyFrame.KeyFrameType.ROTATION.ordinal() * distance + timeLineOffsetY;
        this.scaleLine = KeyFrame.KeyFrameType.SCALE.ordinal() * distance + timeLineOffsetY;

        drawBackground();
        drawFollowLine(mouseX, mouseY);

        for (KeyFrame.KeyFrameType type :
                KeyFrame.KeyFrameType.values()) {
            drawLine(type);
            if(keyFrameLine == null) continue;
            for (KeyFrame keyFrame :
                    keyFrameLine.getKeyFrames(type)) {
                drawKeyFrame(keyFrame,type);
            }
        }

        drawTimestamp();
    }

    private void drawBackground(){
        Gui.drawRect(0,0,getWidth(),getHeight(),DrawHelper.colorInt(10,10,10,200));
        drawHorizontalLine(0,getWidth(),0,DrawHelper.colorInt(180,180,180,255));
        //draw start
        drawVerticalLine(linePadding,0,2,DrawHelper.colorInt(180,180,180,255));
        drawString(getRoot().getFontRenderer(),"0",linePadding,1,DrawHelper.colorInt(180,180,180,180));

        drawVerticalLine(getWidth() - linePadding,0,2,DrawHelper.colorInt(180,180,180,255));
        String endString = String.valueOf(keyFrameLine.getTotalTick());

        int stringWidth = getRoot().getFontRenderer().getStringWidth(endString);
        drawString(getRoot().getFontRenderer(),endString,getWidth() - stringWidth - linePadding,1,DrawHelper.colorInt(200,200,200,180));
    }

    private void drawLine(KeyFrame.KeyFrameType type){
        int posY = type.ordinal() * distance + timeLineOffsetY;
        drawHorizontalLine(0,getWidth(),posY,DrawHelper.colorInt(200,200,200,255));
    }

    public void drawKeyFrame(KeyFrame keyFrame, KeyFrame.KeyFrameType type){
        long tick = keyFrame.getBeginTick();

        final int fixOffset = 1;

        int posX = getCurPosX(tick) + fixOffset;
        int posY = type.ordinal() * distance + timeLineOffsetY + fixOffset;

        if(keyFrame != selectFrame) {

            DrawHelper.drawRhombus(posX - keyFrameSize, posY - keyFrameSize,
                    posX + keyFrameSize, posY + keyFrameSize,
                    DrawHelper.colorInt(150, 0, 0, 255));
            DrawHelper.drawRhombus(posX - keyFrameSize + 1, posY - keyFrameSize + 1,
                    posX + keyFrameSize - 1, posY + keyFrameSize - 1,
                    DrawHelper.colorInt(100, 0, 0, 255));

            if(keyFrame.getBeginTick() == hoverTime){
                DrawHelper.drawRhombus(posX - keyFrameSize + 1, posY - keyFrameSize + 1,
                        posX + keyFrameSize - 1, posY + keyFrameSize - 1,
                        DrawHelper.colorInt(255, 0, 0, 200));
            }else if(MathHelper.approximate(keyFrame.getBeginTick(),curTick,1)){
                DrawHelper.drawRhombus(posX - keyFrameSize + 1, posY - keyFrameSize + 1,
                        posX + keyFrameSize - 1, posY + keyFrameSize - 1,
                        DrawHelper.colorInt(255, 255, 0, 200));
            }
        }else {
            DrawHelper.drawRhombus(posX - keyFrameSize, posY - keyFrameSize,
                    posX + keyFrameSize, posY + keyFrameSize,
                    DrawHelper.colorInt(0, 150, 0, 255));
            DrawHelper.drawRhombus(posX - keyFrameSize + 1, posY - keyFrameSize + 1,
                    posX + keyFrameSize - 1, posY + keyFrameSize - 1,
                    DrawHelper.colorInt(0, 220, 0, 255));
        }
    }

    private void drawFollowLine(int mouseX, int mouseY){
        if(RootView.isInside(this,mouseX,mouseY) && (
                        MathHelper.approximate(linearLine,mouseY,keyFrameSize)||
                        MathHelper.approximate(scaleLine,mouseY,keyFrameSize)||
                        MathHelper.approximate(rotationLine,mouseY,keyFrameSize))) {
            final long time = getSelectTime(mouseX);

            drawVerticalLine(mouseX, 1, getHeight(), DrawHelper.colorInt(200, 0, 60, 100));

            if (mouseX <= getWidth() - linePadding && mouseX >= linePadding) {
                String timeString = String.valueOf(time);
                if(mouseX + getRoot().getFontRenderer().getStringWidth(timeString) > getWidth()){
                    mouseX -= mouseX + getRoot().getFontRenderer().getStringWidth(timeString) - getWidth();
                }
                drawString(getRoot().getFontRenderer(), timeString, mouseX, 1, DrawHelper.colorInt(255, 255, 255, 255));
                hoverTime = time;
            }else{
                hoverTime = -1;
            }
        }else{
            hoverTime = -1;
        }
    }

    private void drawTimestamp(){
        long total = keyFrameLine.getTotalTick();

        //curTick may larger than totalTick because of not same total time before apply the modify
        if(curTick <= total){
            int x = (int) (curTick/(float)total * (getWidth() - 2 * linePadding));
            drawVerticalLine(x + linePadding, 1, 4, DrawHelper.colorInt(255, 255, 0, 255));
        }
    }

    protected int getSelectTime(int mouseX){
        return getSelectTime(mouseX,0,keyFrameLine.getTotalTick(),linePadding,getWidth() - linePadding);
    }

    private int getCurPosX(long time){
        return getCurPosX(time,0,keyFrameLine.getTotalTick(),linePadding,getWidth() - linePadding);
    }

    private int getSelectTime(int x,long minValue,long maxValue,int start,int end){
        float totalCur = maxValue - minValue;
        float width = (end - start) / totalCur;

        return (int) ((x - start) / width + .5);
    }

    private int getCurPosX(long value,long minValue,long maxValue,int start,int end){
        float totalCur = maxValue - minValue;
        float width = (end - start) / totalCur;

       return (int) (value * width) + start;
    }

    //垃圾代码！
    @Override
    public boolean onClicked(int mouseX, int mouseY, int mouseButton) {

        if(mouseButton == 0){//left click
            long time = getSelectTime(mouseX);
            //fix time
            if(time < 0) time = 0;
            if(time > keyFrameLine.getTotalTick()) time = keyFrameLine.getTotalTick();

            if(MathHelper.approximate(linearLine,mouseY,keyFrameSize)){

                Optional<KeyFrame> optionalKeyFrame =
                        keyFrameLine.getKeyFrame(KeyFrame.KeyFrameType.LINEAR,time);

                if(optionalKeyFrame.isPresent())
                    setSelectFrame(optionalKeyFrame.get());
                else
                    keyFrameLine.addKeyFrame(KeyFrame.KeyFrameType.LINEAR,new LinearTransformNode.LinearKeyFrame(),time);

            }else if(MathHelper.approximate(rotationLine,mouseY,keyFrameSize)){
                Optional<KeyFrame> optionalKeyFrame =
                        keyFrameLine.getKeyFrame(KeyFrame.KeyFrameType.ROTATION,time);

                if(optionalKeyFrame.isPresent())
                    setSelectFrame(optionalKeyFrame.get());
                else {
                    RotationTransformNode.RotationKeyFrame rotationKeyFrame = new RotationTransformNode.RotationKeyFrame();
                    if(prefab != null && prefab.getControlPoint() != null)
                        rotationKeyFrame.center = prefab.getControlPoint();

                    keyFrameLine.addKeyFrame(KeyFrame.KeyFrameType.ROTATION, rotationKeyFrame, time);
                }

            }else if(MathHelper.approximate(scaleLine,mouseY,keyFrameSize)){
                Optional<KeyFrame> optionalKeyFrame =
                        keyFrameLine.getKeyFrame(KeyFrame.KeyFrameType.SCALE,time);

                if(optionalKeyFrame.isPresent())
                    setSelectFrame(optionalKeyFrame.get());
                else
                    keyFrameLine.addKeyFrame(KeyFrame.KeyFrameType.SCALE,new ScaleTransformNode.ScaleKeyFrame(),time);
            }

        }else if(mouseButton == 1){//right click
            final long time = getSelectTime(mouseX);

            if(MathHelper.approximate(linearLine,mouseY,keyFrameSize)){
                keyFrameLine.getKeyFrame(KeyFrame.KeyFrameType.LINEAR,time)
                        .ifPresent(new Consumer<KeyFrame>() {
                            @Override
                            public void accept(KeyFrame keyFrame) {
                                if(keyFrame == selectFrame){
                                    setSelectFrame(null);
                                }
                                keyFrameLine.deleteKeyFrame(KeyFrame.KeyFrameType.LINEAR,keyFrame);
                            }
                        });
            }else if(MathHelper.approximate(rotationLine,mouseY,keyFrameSize)){
                keyFrameLine.getKeyFrame(KeyFrame.KeyFrameType.ROTATION,time)
                        .ifPresent(new Consumer<KeyFrame>() {
                            @Override
                            public void accept(KeyFrame keyFrame) {
                                if(keyFrame == selectFrame){
                                    setSelectFrame(null);
                                }
                                keyFrameLine.deleteKeyFrame(KeyFrame.KeyFrameType.ROTATION,keyFrame);
                            }
                        });
            }else if(MathHelper.approximate(scaleLine,mouseY,keyFrameSize)){
                keyFrameLine.getKeyFrame(KeyFrame.KeyFrameType.SCALE,time)
                        .ifPresent(new Consumer<KeyFrame>() {
                            @Override
                            public void accept(KeyFrame keyFrame) {
                                if(keyFrame == selectFrame){
                                    setSelectFrame(null);
                                }
                                keyFrameLine.deleteKeyFrame(KeyFrame.KeyFrameType.SCALE,keyFrame);
                            }
                        });

            }

        }

        return true;
    }

    public int getTimeLinePixelLength(){
        return getWidth() - (linePadding << 1);
    }

    public void setSelectFrame(KeyFrame selectFrame) {
        if(selectFrame != this.selectFrame) {
            if(selectKeyFrameChange != null){
                selectKeyFrameChange.accept(selectFrame);
            }
            this.selectFrame = selectFrame;
        }
    }

    public KeyFrame getSelectFrame() {
        return selectFrame;
    }

    @Override
    public boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {

        return true;
    }

    public void setSelectKeyFrameChange(Consumer<KeyFrame> selectKeyFrameChange) {
        this.selectKeyFrameChange = selectKeyFrameChange;
    }

    public long getCurTick() {
        return curTick;
    }

    public void setCurTick(long curTick) {
        this.curTick = curTick;
    }
}
