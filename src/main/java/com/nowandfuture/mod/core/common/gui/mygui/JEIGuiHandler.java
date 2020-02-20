package com.nowandfuture.mod.core.common.gui.mygui;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraft.client.gui.inventory.GuiContainer;

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
