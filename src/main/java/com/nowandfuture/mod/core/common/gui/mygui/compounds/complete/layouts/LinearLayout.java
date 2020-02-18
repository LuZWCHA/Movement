package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.layouts;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.AbstractLayout;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.LayoutParameter;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;

import javax.annotation.Nonnull;
import java.util.List;

//not finished
public class LinearLayout extends AbstractLayout<LinearLayout.LinearLayoutParameter> {

    public LinearLayout(@Nonnull RootView rootView) {
        super(rootView);
    }

    public LinearLayout(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    public LinearLayout(@Nonnull RootView rootView, ViewGroup parent, @Nonnull List<LinearLayoutParameter> list) {
        super(rootView, parent, list);
    }

    @Override
    public void onLayout(int parentWidth, int parentHeight) {
        super.onLayout(parentWidth, parentHeight);
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
    public void onWidthChanged(int old, int cur) {
    }

    @Override
    public void onHeightChanged(int old, int cur) {
    }

    public static class LinearLayoutParameter extends LayoutParameter {

    }
}
