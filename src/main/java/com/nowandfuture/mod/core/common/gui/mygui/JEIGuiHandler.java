package com.nowandfuture.mod.core.common.gui.mygui;

import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

@Optional.Interface(iface = "mezz.jei.api.gui.IAdvancedGuiHandler", modid = "jei")
public abstract class JEIGuiHandler<T extends AbstractGuiContainer> implements IAdvancedGuiHandler<T> {

    @Nullable
    @Override
    @Optional.Method(modid = "jei")
    public List<Rectangle> getGuiExtraAreas(T guiContainer) {
        return null;
    }

    @Nullable
    @Override
    @Optional.Method(modid = "jei")
    public Object getIngredientUnderMouse(T guiContainer, int mouseX, int mouseY) {
        return null;
    }
}
