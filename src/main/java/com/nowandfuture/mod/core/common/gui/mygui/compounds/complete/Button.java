package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyButton;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Button extends View {
    private MyButton button;

    private ActionListener actionListener;

    public Button(@Nonnull RootView rootView) {
        super(rootView);
        button = new MyButton(0,0,0,"");
        button.setAsMyGui(true);
    }

    public Button(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        button = new MyButton(0,0,0,"");
        button.setAsMyGui(true);
    }

    public Button setText(String text){
        button.displayString = text;
        return this;
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

    //fixed minecraft button absolute location drawable
    @Override
    public void setX(int x) {
        super.setX(x);
        button.setX(0);
    }

    //fixed minecraft button absolute location drawable
    @Override
    public void setY(int y) {
        super.setY(y);
        button.setY(0);
    }

    public void setEnable(boolean enable){
        button.enabled = enable;
    }

    public boolean isEnabled(){
        return button.enabled;
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        button.setHovered(isHovering());
        button.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        button.playPressSound(Minecraft.getMinecraft().getSoundHandler());
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

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return true;
    }

    public void setImageLocation(@Nullable ResourceLocation location){
        button.setImageLocation(location);
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }
}
