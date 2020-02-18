package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.layouts;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.AbstractLayout;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.LayoutParameter;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;

import javax.annotation.Nonnull;

public class FrameLayout extends AbstractLayout<FrameLayout.FrameLayoutParameter> {

    public FrameLayout(@Nonnull RootView rootView) {
        super(rootView);
    }

    @Override
    public void onLayout(int parentWidth, int parentHeight) {
        //do no thing
    }

    public FrameLayout(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        super.onDraw(mouseX, mouseY, partialTicks);
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return true;
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

    public static class FrameLayoutParameter extends LayoutParameter{

    }
}
