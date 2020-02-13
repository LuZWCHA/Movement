package com.nowandfuture.mod.core.common.gui.mygui.compounds;

import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.gui.Gui;
import org.lwjgl.util.Color;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

//not finished
public abstract class AbstractLayout<T extends LayoutParameter> extends ViewGroup{

    private List<T> layoutParameters;
    private Color color;

    public AbstractLayout(@Nonnull RootView rootView){
        super(rootView);
        color = new Color(0,0,0,0);
    }

    public AbstractLayout(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        layoutParameters = new ArrayList<>();
        color = new Color(0,0,0,0);
    }

    public AbstractLayout(@Nonnull RootView rootView, ViewGroup parent,@Nonnull List<T> list) {
        super(rootView, parent);
        this.layoutParameters = list;
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        drawBackground();
    }

    protected void drawBackground(){
        Gui.drawRect(0,0,getWidth(),getHeight(), DrawHelper.colorInt(color));
    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return false;
    }

    @Override
    public void onLayout(int parentWidth, int parentHeight) {

    }

    @Override
    public boolean onKeyType(char typedChar, int keyCode) {
        return false;
    }

    @Override
    protected void onChildrenLayout() {
        super.onChildrenLayout();
    }

    public void setBackgroundColor(Color color){
        this.color = color;
    }

    public Color getBackgroundColor() {
        return color;
    }
}
