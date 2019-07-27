package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.utils.DrawHelper;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class SliderView extends View {

    private int sliderHalfWidth = 4;
    private int sliderHalfHeight = 6;
    private boolean isVertical = false;

    private boolean enable;

    private int sliderX,sliderY;
    private boolean drag;

    private float lastProgress = 0;
    private float lastProgress2 = 0;

    private Consumer<Float> progressChanged;//trigger after not dragging
    private Consumer<Float> progressChanging;//if progress changed and is dragging

    public SliderView(@Nonnull RootView rootView) {
        super(rootView);
    }

    public SliderView(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    @Override
    protected void onLoad() {
        setProgress(0);
    }

    public void setProgress(float progress) {
        float length = isVertical ? (getHeight() - (sliderHalfHeight) << 1): (getWidth() - (sliderHalfWidth << 1));
        if(isVertical){
            sliderY = (int) (length * progress + sliderHalfHeight);
            sliderX = getWidth() >> 1;
        }else{
            sliderX = (int) (length * progress + sliderHalfWidth);
            sliderY = getHeight() >> 1;
        }
    }

    public boolean isDrag() {
        return drag;
    }

    public float getProgress() {
        float length = isVertical ? (getHeight() - (sliderHalfHeight << 1)) : (getWidth() - (sliderHalfWidth << 1));
        if(isVertical){
            return (sliderY - sliderHalfHeight) / length;
        }else {
            return (sliderX - sliderHalfWidth) / length;
        }
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        drawBackground();
        drawLine();
        drawSlider();
    }

    private void drawBackground(){

    }

    private void drawLine(){
        drawHorizontalLine(sliderHalfWidth,getWidth() - sliderHalfWidth,getHeight() / 2,-1);
    }

    private void drawSlider(){
        drawRect(sliderX - sliderHalfWidth,sliderY - sliderHalfHeight,sliderX + sliderHalfWidth,sliderY + sliderHalfHeight,
                DrawHelper.colorInt(255,255,255,255));
        if(drag){
            drawRect(sliderX - sliderHalfWidth,sliderY - sliderHalfHeight,sliderX + sliderHalfWidth,sliderY + sliderHalfHeight,
                    DrawHelper.colorInt(120,120,120,120));
        }
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {
        drag = false;
        final float curProgress = getProgress();
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
        final float curProgress = getProgress();
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
