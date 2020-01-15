package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyButton;

import javax.annotation.Nonnull;

public class Button extends View {
    MyButton button;

    private ActionListener actionListener;

    public Button(@Nonnull RootView rootView) {
        super(rootView);
        button = new MyButton(0,0,0,"");
    }

    public Button(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        button = new MyButton(0,0,0,"");
    }

    public void setText(String text){
        button.displayString = text;
    }

    public String getText(){
        return button.displayString;
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        button.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        button.setHeight(height);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        button.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        button.setY(y);
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        button.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        if (actionListener != null)
            actionListener.onClicked(this);
        return true;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        if(actionListener != null)
            actionListener.onLongClicked(this);
        return true;
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }
}
