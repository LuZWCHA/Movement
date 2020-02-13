package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.gui.Gui;

import javax.annotation.Nonnull;

// TODO: 2020/2/12 to finish textview to replace label
public class TextView extends View {

    public TextView(@Nonnull RootView rootView) {
        super(rootView);
    }

    public TextView(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }
}
