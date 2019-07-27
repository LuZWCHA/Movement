package com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible;

import com.nowandfuture.mod.core.common.gui.mygui.MyGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import java.io.IOException;

public class MyButton extends GuiButtonExt implements MyGui {

    public MyButton(int id, int x, int y, String displayString) {
        super(id, x, y, displayString);
    }

    public MyButton(int id, int x, int y, int width, int height, String displayString) {
        super(id, x, y, width, height, displayString);
    }

    @Override
    public int getId() {
        return id;
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
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        drawButton(Minecraft.getMinecraft(),mouseX,mouseY,partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        return false;
    }

    @Override
    public boolean mouseLongClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        mouseReleased(mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(int mouseX, int mouseY,int state) {
        return mousePressed(Minecraft.getMinecraft(),mouseX,mouseY);
    }

    @Override
    public boolean handleMouseInput(int mouseX, int mouseY) {
        return false;
    }

}
