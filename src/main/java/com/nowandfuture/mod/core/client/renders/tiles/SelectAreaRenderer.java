package com.nowandfuture.mod.core.client.renders.tiles;

import com.nowandfuture.mod.core.common.entities.TileEntityConstructor;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SelectAreaRenderer extends TileEntitySpecialRenderer<TileEntityConstructor> {

    public SelectAreaRenderer(){
        super();
    }

    @Override
    public void render(TileEntityConstructor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 1,y,z);
        DrawHelper.render(te.getAABBSelectArea(),0,0,0);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityConstructor te) {
        return true;
    }
}
