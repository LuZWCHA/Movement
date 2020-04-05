package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Button extends View {
    private MyButton button;
    private ResourceLocation location;
    private boolean vanillaStyle = true;

    private Color buttonColor;
    private Color buttonHoverColor;
    private Color disableColor;
    private Color textColor;
    private Color textHoverColor;
    private Color disableTextColor;

    private ActionListener actionListener;
    private int imagePadding = 2;

    public Button(@Nonnull RootView rootView) {
        super(rootView);
        button = new MyButton(0,0,0,"");
        button.setAsMyGui(true);
        init();
    }

    public Button(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        button = new MyButton(0,0,0,"");
        button.setAsMyGui(true);
        init();
    }

    private void init(){
        buttonColor = new Color(128,128,128);
        buttonHoverColor = new Color(200,200,200);
        disableColor = new Color(80,80,80);
        textColor = new Color(225,225,225);
        textHoverColor = new Color(255,255,255);
        disableTextColor = new Color(128,128,128);
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
        if(vanillaStyle) {
            button.setHovered(isHovering());
            button.draw(mouseX, mouseY, partialTicks);
        }else{
            Color buttonColor,textColor;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            if(isEnabled()){
                buttonColor = isHovering() ? buttonHoverColor : this.buttonColor;
                textColor = isHovering() ? textHoverColor : this.textColor;
            }else{
                buttonColor = disableColor;
                textColor = disableTextColor;
            }
            drawRect(0,0,getWidth(),getHeight(),colorInt(buttonColor));

            FontRenderer renderer = getRoot().context.fontRenderer;
            int strWidth = renderer.getStringWidth(getText());
            int ellipsisWidth = renderer.getStringWidth("...");
            String text = getText();

            if (strWidth > getWidth() - 6 && strWidth > ellipsisWidth)
                text = renderer.trimStringToWidth(text, getWidth() - 6 - ellipsisWidth).trim() + "...";

            GlStateManager.color(1,1,1,1);
            this.drawForeground();
            drawCenteredStringWithoutShadow(renderer, text, getWidth() / 2, (getHeight() - 8) / 2, colorInt(textColor));
        }
    }

    private void drawForeground() {
        if (location != null) {
            GlStateManager.enableAlpha();
            Minecraft.getMinecraft().renderEngine.bindTexture(location);

            int size = Math.min(getHeight(), getWidth()) - imagePadding * 2;
            if (size > 0) {
                int offsetX = (getWidth() - size) / 2;
                int offsetY = (getHeight() - size) / 2;
                drawTexturedModalRect(offsetX, offsetY, this.zLevel, 0, 0, size, size, size, size);
            }
            GlStateManager.disableAlpha();
        }
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
        this.location = location;
        button.setImageLocation(location);
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setVanillaStyle(boolean vanillaStyle){
        this.vanillaStyle = vanillaStyle;
    }

    public Color getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(@Nonnull Color buttonColor) {
        this.buttonColor = buttonColor;
    }

    public Color getButtonHoverColor() {
        return buttonHoverColor;
    }

    public void setButtonHoverColor(@Nonnull Color buttonHoverColor) {
        this.buttonHoverColor = buttonHoverColor;
    }

    public Color getDisableColor() {
        return disableColor;
    }

    public void setDisableColor(@Nonnull Color disableColor) {
        this.disableColor = disableColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(@Nonnull Color textColor) {
        this.textColor = textColor;
    }

    public Color getTextHoverColor() {
        return textHoverColor;
    }

    public void setTextHoverColor(@Nonnull Color textHoverColor) {
        this.textHoverColor = textHoverColor;
    }

    public Color getDisableTextColor() {
        return disableTextColor;
    }

    public void setDisableTextColor(@Nonnull Color disableTextColor) {
        this.disableTextColor = disableTextColor;
    }
}
