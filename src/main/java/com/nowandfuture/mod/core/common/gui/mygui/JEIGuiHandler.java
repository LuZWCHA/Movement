package com.nowandfuture.mod.core.common.gui.mygui;

import mezz.jei.api.gui.IAdvancedGuiHandler;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public abstract class JEIGuiHandler<T extends AbstractGuiContainer> implements IAdvancedGuiHandler<T> {

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(T guiContainer) {
        return null;
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(T guiContainer, int mouseX, int mouseY) {
        return null;
    }
}
