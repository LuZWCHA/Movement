package com.nowandfuture.mod.core.common.gui.mygui;

import java.io.IOException;

public interface MyGui {
    int getId();
    int getX();
    int getY();
    void setX(int x);
    void setY(int y);
    int getWidth();
    int getHeight();
    void setWidth(int width);
    void setHeight(int height);
    void draw(int mouseX, int mouseY, float partialTicks);

    boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException;
    boolean mouseLongClicked(int mouseX, int mouseY, int mouseButton) throws IOException;

    void mouseReleased(int mouseX, int mouseY, int state);
    boolean mousePressed(int mouseX, int mouseY, int state);

    boolean handleMouseInput(int mouseX, int mouseY);
}
