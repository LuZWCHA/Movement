package com.nowandfuture.mod.core.common.gui.mygui.compounds;

public class TopView extends ViewGroup {

    TopView(RootView rootView){
        super(rootView);
    }

    @Override
    protected boolean checkMouseInside(int mouseX, int mouseY, float partialTicks) {
        return true;
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY, int state) {
        return super.mousePressed(mouseX, mouseY, state);
    }

    @Override
    protected void onLayout(int parentWidth, int parentHeight) {

    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return false;
    }
}
