package com.nowandfuture.mod;

import com.nowandfuture.mod.core.common.gui.GuiModule;
import com.nowandfuture.mod.core.common.gui.GuiTimelineEditor;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public class MovementJEICompact implements IModPlugin {
    @Override
    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(GuiTimelineEditor.getJEIGuiHandler(), GuiModule.getJEIGuiHandler());
    }
}
