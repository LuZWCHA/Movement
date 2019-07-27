package com.nowandfuture.mod.core.client.renders.tiles;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.nowandfuture.asm.RenderHook;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.common.entities.TileEntityTimelineEditor;
import com.nowandfuture.mod.handler.RenderHandler;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityTimeLineEditorRenderer extends TileEntityModuleRenderer<TileEntityTimelineEditor> {

    @Override
    public void render(TileEntityTimelineEditor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        super.render(te,x + 1,y,z,partialTicks,destroyStage,alpha);

        if(te.isRenderValid() && te.getModuleBase().getPrefab() != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 1,y,z);
            DrawHelper.drawOutlinedBoundingBox(te.getModuleBase().getPrefab().getTransformedBounding());
            DrawHelper.drawCoordinateAxis();
            GlStateManager.popMatrix();
            RenderHandler.addRenderer(te);
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTimelineEditor te) {
        return true;
    }
}
