package com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible;

import com.google.common.collect.Lists;
import com.nowandfuture.mod.core.common.gui.mygui.api.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import joptsimple.internal.Strings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class MyLabel extends Gui implements MyGui {

    protected int width;
    protected int height;
    public int x;
    public int y;
    private final List<String> labels;
    public int id;
    private boolean horizontalCenter;
    public boolean visible = true;
    private boolean labelBgEnabled;
    private int textColor;
    private int backColor;
    private int ulColor;
    private int brColor;
    private final FontRenderer fontRenderer;
    private int border;
    private boolean drawShadow = true;
    private int maxLines = 1;

    public MyLabel(FontRenderer fontRenderer, int id, int x, int y, int width, int height, int textColor)
    {
        this.fontRenderer = fontRenderer;
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.labels = Lists.<String>newArrayList();
        this.horizontalCenter = false;
        this.labelBgEnabled = true;
        this.textColor = textColor;
        this.backColor = -1;
        this.ulColor = -1;
        this.brColor = -1;
        this.border = 0;
    }

    public MyLabel addLine(String line)
    {
        this.labels.add(I18n.format(line));
        return this;
    }

    public MyLabel setLine(int index,String line)
    {
        while (labels.size() <= index){
            labels.add(Strings.EMPTY);
        }
        this.labels.set(index,I18n.format(line));
        return this;
    }

    public MyLabel empty(){
        labels.clear();
        labels.add("");
        return this;
    }

    public MyLabel setFirst(String line){
        if(labels.isEmpty()) labels.add("");
        setLine(0,I18n.format(line));
        return this;
    }

    public void removeLine(int line)
    {
        this.labels.remove(line);
    }

    public void removeAllLines(int line)
    {
        this.labels.clear();
    }

    /**
     * Sets the Label to be centered
     */
    public MyLabel setHorizontalCenter(boolean horizontalCenter)
    {
        this.horizontalCenter = horizontalCenter;
        return this;
    }

    public MyLabel setBackColor(int color){
        this.backColor = color;
        return this;
    }

    public MyLabel setBorderColor(int color){
        this.brColor = color;
        this.ulColor = color;
        return this;
    }

    public MyLabel setBorderWidth(int width){
        border = width;
        return this;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public MyLabel enableBackDraw(boolean labelBgEnabled){
        this.labelBgEnabled = labelBgEnabled;
        return this;
    }

    public void drawLabel(int mouseX, int mouseY)
    {
        if (this.visible)
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            this.drawLabelBackground(mouseX, mouseY);
            int i = this.y + this.height / 2 + this.border / 2;

            int textHeight = 10;
            int rowNum = getHeight() / textHeight;
            int size = Math.min(rowNum,labels.size());
            size = Math.min(maxLines,size);

            int j = i - size * 10 / 2;

            for (int k = 0; k < size; ++k)
            {
                if (this.horizontalCenter)
                {
                    if(drawShadow)
                        this.drawCenteredString(this.fontRenderer, this.labels.get(k), this.x + this.width / 2, j + k * textHeight, this.textColor);
                    else
                        ViewGroup.drawCenteredStringWithoutShadow(this.fontRenderer, this.labels.get(k), this.x + this.width / 2, j + k * textHeight, this.textColor);
                }
                else
                {
                    if(drawShadow)
                        this.drawString(this.fontRenderer, this.labels.get(k), this.x, j + k * textHeight, this.textColor);
                    else
                        ViewGroup.drawStringWithoutShadow(this.fontRenderer, this.labels.get(k), this.x, j + k * textHeight, this.textColor);
                }
            }
        }
    }

    protected void drawLabelBackground(int mouseX, int mouseY)
    {
        if (this.labelBgEnabled)
        {
            int i = this.width + this.border * 2;
            int j = this.height + this.border * 2;
            int k = this.x - this.border;
            int l = this.y - this.border;
            drawRect(k, l, k + i, l + j, this.backColor);
            this.drawHorizontalLine(k, k + i, l, this.ulColor);
            this.drawHorizontalLine(k, k + i, l + j, this.brColor);
            this.drawVerticalLine(k, l, l + j, this.ulColor);
            this.drawVerticalLine(k + i, l, l + j, this.brColor);
        }
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
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        drawLabel(mouseX,mouseY);
    }

    @Override
    public void draw2(int mouseX, int mouseY, float partialTicks) {

    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    public void setDrawTextShadow(boolean drawTextShadow){
        this.drawShadow = drawTextShadow;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
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
        return this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

    @Override
    public boolean handleMouseInput(int mouseX, int mouseY) {
        return false;
    }
}
