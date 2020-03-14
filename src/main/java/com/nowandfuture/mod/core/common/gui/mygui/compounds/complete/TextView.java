package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyLabel;
import com.nowandfuture.mod.utils.DrawHelper;
import joptsimple.internal.Strings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import javax.annotation.Nonnull;

// TODO: 2020/2/12 to finish textview to replace label
public class TextView extends View {
    private MyLabel label;
    private String text = Strings.EMPTY;

    public TextView(@Nonnull RootView rootView) {
        super(rootView);
        label = new MyLabel(getRoot().getFontRenderer(),0,0,0,0,0,-1);
        label.setBackColor(0);
        label.setBorderColor(0);
        label.setBorderWidth(0);
        label.setFirst(Strings.EMPTY);
        setClickable(false);
    }

    public TextView(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        label = new MyLabel(getRoot().getFontRenderer(),0,0,0,0,0,-1);
        label.setBackColor(0);
        label.setBorderColor(0);
        label.setBorderWidth(0);
        label.setFirst(Strings.EMPTY);
        setClickable(false);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        label.setX(0);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        label.setY(0);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        label.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        label.setHeight(height);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        label.visible = visible;
    }

    public TextView setText(@Nonnull String string){
        if(!string.equals(text)) {
            layoutText(string);
            text = string;
        }
        return this;
    }

    @Override
    public void onLayout(int parentWidth, int parentHeight) {
    }

    @Override
    public void onWidthChanged(int old, int cur) {
        super.onWidthChanged(old, cur);
        layoutText(text);
    }

    //re-layout text in minecraft's label
    private void layoutText(String string){
        label.empty();
        FontRenderer fr = getRoot().getFontRenderer();
        int stringLength = fr.getStringWidth(string);
        if(getWidth() > stringLength || string.isEmpty()) {
            label.setFirst(string);
        } else{
            String curLine;
            String temp = string;
            int start;
            int lineNum = 0;
            while(!temp.isEmpty()){
                curLine = fr.trimStringToWidth(temp,getWidth());
                if(curLine.isEmpty()){
                    curLine = String.valueOf(temp.charAt(0));
                }

                start = curLine.length();
                if(start == 0){
                    //at least one world per line,but may not be rendered
                    start = 1;
                }
                label.setLine(lineNum,curLine);
                lineNum ++;
                temp = temp.substring(start);
            }

        }
    }

    public void setCentered(boolean centered){
        label.setCentered(centered);
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        label.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    public void setBackgroundColor(int color){
        label.setBackColor(color);
    }

    public void setTextColor(int color){
        label.setTextColor(color);
    }
}
