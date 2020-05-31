package com.nowandfuture.mod.core.client.renderers.tiles;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityConstructor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SelectAreaRenderer extends TileEntitySpecialRenderer<TileEntityConstructor> {
    private static final ResourceLocation BACKGROUND_GUI_TEXTURE =
            new ResourceLocation(Movement.MODID,"textures/gui/background.png");

    public SelectAreaRenderer(){
        super();
    }

    @Override
    public void render(TileEntityConstructor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 1,y,z);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(4.0F);
        GlStateManager.disableTexture2D();
//        Minecraft.getMinecraft().renderEngine.bindTexture(BACKGROUND_GUI_TEXTURE);

        RenderGlobal.drawSelectionBoundingBox(te.getAABBSelectArea().getBox(),1,1,1,1);
//        DrawHelper.render(te.getAABBSelectArea(),0,0,0);
//            GlStateManager.depthMask(false);
//            DrawHelper.drawOutlinedBoundingBox(te.getModuleBase().getPrefab().getTransformedBounding());
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityConstructor te) {
        return true;
    }
}
