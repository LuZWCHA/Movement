package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.utils.DrawHelper;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class SliderView extends View {

    private int sliderHalfWidth = 3;
    private int sliderHalfHeight = 4;
    private boolean isVertical = false;

    private boolean enable;

    private int sliderX,sliderY;
    private boolean drag;

    private float lastProgress = 0;
    private float lastProgress2 = 0;

    private Consumer<Float> progressChanged;//trigger after not dragging
    private Consumer<Float> progressChanging;//if progress changed and is dragging
    private float MAX_VALUE,MIN_VALUE,progress,DEFAULT_PROGRESS;

    public SliderView(@Nonnull RootView rootView) {
        super(rootView);
    }

    public SliderView(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    @Override
    protected void onLoad() {
        setProgress(DEFAULT_PROGRESS);
    }

    public SliderView setRange(float Max,float Min,float defaultValue){
        MIN_VALUE = Min;
        MAX_VALUE = Max;
        if(defaultValue <= Max && defaultValue >= Min)
            DEFAULT_PROGRESS = defaultValue;
        else{
            DEFAULT_PROGRESS = MIN_VALUE;
        }
        progress = DEFAULT_PROGRESS;
        return this;
    }

    public SliderView setProgress(float progress) {
        this.progress = progress;
        final float range = MAX_VALUE - MIN_VALUE;
        progress -= MIN_VALUE;

        float length = isVertical ? (getHeight() - (sliderHalfHeight) << 1): (getWidth() - (sliderHalfWidth << 1));
        if(isVertical){
            sliderY = (int) (length * progress / range + sliderHalfHeight);
            sliderX = getWidth() >> 1;
        }else{
            sliderX = (int) (length * progress / range + sliderHalfWidth);
            sliderY = getHeight() >> 1;
        }
        return this;
    }

    public boolean isDrag() {
        return drag;
    }

    public float getProgress() {
        return progress;
    }

    private float getRealProgress() {
        final float range = MAX_VALUE - MIN_VALUE;
        float length = isVertical ? (getHeight() - (sliderHalfHeight << 1)) : (getWidth() - (sliderHalfWidth << 1));
        if(isVertical){
            return (sliderY - sliderHalfHeight) / length * range + MIN_VALUE;
        }else {
            return (sliderX - sliderHalfWidth) / length * range + MIN_VALUE;
        }
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        drawBackground();
        drawLine();
        drawSlider();
    }

    protected void drawBackground(){

    }

    protected void drawLine(){
        drawHorizontalLine(sliderHalfWidth,getWidth() - sliderHalfWidth,getHeight() / 2,DrawHelper.colorInt(180,180,180,180));
    }

    protected void drawSlider(){
        drawRect(sliderX - sliderHalfWidth,sliderY - sliderHalfHeight,sliderX + sliderHalfWidth,sliderY + sliderHalfHeight,
                DrawHelper.colorInt(80,80,80,200));
        drawRect(sliderX - sliderHalfWidth + 1,sliderY - sliderHalfHeight + 1,sliderX + sliderHalfWidth - 1,sliderY + sliderHalfHeight - 1,
                DrawHelper.colorInt(220,220,220,255));
        if(drag){
            drawRect(sliderX - sliderHalfWidth + 1,sliderY - sliderHalfHeight + 1,sliderX + sliderHalfWidth - 1,sliderY + sliderHalfHeight - 1,
                    DrawHelper.colorInt(180,180,180,180));
        }
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {
        drag = false;
        final float curProgress = getRealProgress();
        progress = curProgress;
        if(curProgress != lastProgress){
            onProgressChanged(curProgress);
        }
        super.onReleased(mouseX, mouseY, state);
    }

    @Override
    protected boolean onMousePressedMove(int mouseX, int mouseY, int state) {
        if(!isVertical) {
            sliderX = fixMouse(mouseX,false);
        }
        else {
            sliderY = fixMouse(mouseY,true);
        }
        final float curProgress = getRealProgress();
        progress = curProgress;
        if(curProgress != lastProgress2) {
            onProgressChanging(curProgress);
            lastProgress2 = curProgress;
        }
        return drag;
    }

    private int fixMouse(int mouse,boolean isVertical){
        if(isVertical){
            if(mouse < sliderHalfHeight){
                mouse = sliderHalfHeight;
            }else if(mouse > getHeight() - sliderHalfHeight){
                mouse = getHeight() - sliderHalfHeight;
            }
        }else{
            if(mouse < sliderHalfWidth){
                mouse = sliderHalfWidth;
            }else if(mouse > getWidth() - sliderHalfWidth){
                mouse = getWidth() - sliderHalfWidth;
            }
        }
        return mouse;
    }

    private void onProgressChanged(float progress){
        if(progressChanged != null){
            progressChanged.accept(progress);
        }
    }

    private void onProgressChanging(float progress){
        if(progressChanging != null){
            progressChanging.accept(progress);
        }
    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        if(isInSlider(mouseX,mouseY) && state == 0){
            if(!isVertical) {
                sliderX = fixMouse(mouseX,false);
            }
            else {
                sliderY = fixMouse(mouseY,true);
            }

            drag = true;
            lastProgress = getProgress();

            return true;
        }
        return false;
    }

    private boolean isInSlider(int mouseX,int mouseY){
        return mouseX <= sliderX + sliderHalfWidth && mouseX >= sliderX - sliderHalfWidth
                && mouseY <= sliderY + sliderHalfHeight && mouseY >= sliderY - sliderHalfHeight;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return onClicked(mouseX, mouseY, mouseButton);
    }

    public void setProgressChanged(Consumer<Float> progressChanged) {
        this.progressChanged = progressChanged;
    }

    public void setProgressChanging(Consumer<Float> progressChanging) {
        this.progressChanging = progressChanging;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
