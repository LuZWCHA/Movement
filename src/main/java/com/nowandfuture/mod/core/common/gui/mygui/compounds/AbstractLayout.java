package com.nowandfuture.mod.core.common.gui.mygui.compounds;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLayout<T extends LayoutParameter> extends ViewGroup{

    private List<T> layoutParameters;

    public AbstractLayout(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        layoutParameters = new ArrayList<>();
    }

    public AbstractLayout(@Nonnull RootView rootView, ViewGroup parent,@Nonnull List<T> list) {
        super(rootView, parent);
        this.layoutParameters = list;
    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return false;
    }

    @Override
    public void onLayout(int parentWidth, int parentHeight) {

    }

    @Override
    protected void onChildrenLayout() {
        super.onChildrenLayout();
    }
}
