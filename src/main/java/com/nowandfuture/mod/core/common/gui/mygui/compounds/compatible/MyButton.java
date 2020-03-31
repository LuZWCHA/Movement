package com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible;

import com.nowandfuture.mod.core.common.gui.mygui.api.MyGui;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.io.IOException;

public class MyButton extends GuiButtonExt implements MyGui {

    private boolean asView;
    private ResourceLocation location;

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
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        drawButton(Minecraft.getMinecraft(),mouseX,mouseY,partialTicks);
    }

    public boolean isHovered(){
        return hovered;
    }

    public void setHovered(boolean hovered){
        this.hovered = hovered;
    }

    public void setAsMyGui(boolean gui){
        asView = gui;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partial) {
        if (this.visible)
        {
            if(!asView) hovered = mouseX > getX() && mouseX < getX() + getWidth() && mouseY > getY() && mouseY < getY() + getHeight();

            int k = this.getHoverState(this.hovered);
            GuiUtils.drawContinuousTexturedBox(BUTTON_TEXTURES, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.zLevel);
            this.mouseDragged(mc, mouseX, mouseY);
            int color = 14737632;

            if (packedFGColour != 0)
            {
                color = packedFGColour;
            }
            else if (!this.enabled)
            {
                color = 10526880;
            }
            else if (this.hovered)
            {
                color = 16777120;
            }

            String buttonText = this.displayString;
            int strWidth = mc.fontRenderer.getStringWidth(buttonText);
            int ellipsisWidth = mc.fontRenderer.getStringWidth("...");

            if (strWidth > width - 6 && strWidth > ellipsisWidth)
                buttonText = mc.fontRenderer.trimStringToWidth(buttonText, width - 6 - ellipsisWidth).trim() + "...";

            this.drawForeground();
            this.drawCenteredString(mc.fontRenderer, buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
        }
    }

    private void drawForeground() {
        if (location != null) {
            Minecraft.getMinecraft().renderEngine.bindTexture(location);
            DrawHelper.drawTexturedModalRect(4, 4,this.zLevel, 0, 0, 8, 8,8,8);
        }
    }

    public void setImageLocation(ResourceLocation location) {
        this.location = location;
    }

    @Override
    public void draw2(int mouseX, int mouseY, float partialTicks) {

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
