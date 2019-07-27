package com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible;

import com.nowandfuture.mod.core.common.gui.mygui.IUpdate;
import com.nowandfuture.mod.core.common.gui.mygui.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.IType;
import net.minecraft.client.gui.FontRenderer;

import java.util.function.Function;

public class MyTextField extends GuiTextField implements MyGui,IType,IUpdate {

    private Function<Character,Boolean> filter;

    public MyTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height) {
        super(componentId, fontrendererObj, x, y, width, height);
        filter = new Function<Character, Boolean>() {
            @Override
            public Boolean apply(Character character) {
                return true;
            }
        };
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        drawTextBox();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY,int state) {
        return this.getVisible() && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

    @Override
    public boolean handleMouseInput(int mouseX, int mouseY) {
        return false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if(filter != null && filter.apply(typedChar))
            textboxKeyTyped(typedChar, keyCode);
    }

    public void setFilter(Function<Character, Boolean> filter) {
        this.filter = filter;
    }

    @Override
    public void update() {
        if(isFocused() && getVisible())
            updateCursorCounter();
    }
}
