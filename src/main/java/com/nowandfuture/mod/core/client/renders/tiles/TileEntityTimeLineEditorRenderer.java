package com.nowandfuture.mod.core.client.renders.tiles;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.nowandfuture.asm.RenderHook;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.common.entities.TileEntityTimelineEditor;
import com.nowandfuture.mod.handler.RenderHandler;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.optifine.shaders.ShadersRender;

@SideOnly(Side.CLIENT)
public class TileEntityTimeLineEditorRenderer extends TileEntityModuleRenderer<TileEntityTimelineEditor> {

    private static final ResourceLocation BACKGROUND_GUI_TEXTURE =
            new ResourceLocation(Movement.MODID,"textures/gui/background.png");

    @Override
    public void render(TileEntityTimelineEditor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        super.render(te,x + 1,y,z,partialTicks,destroyStage,alpha);

        if(te.isEnable() && te.getModuleBase().getPrefab() != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 1,y,z);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(4.0F);
            GlStateManager.enableTexture2D();
            Minecraft.getMinecraft().renderEngine.bindTexture(BACKGROUND_GUI_TEXTURE);
//            GlStateManager.depthMask(false);
//            DrawHelper.drawOutlinedBoundingBox(te.getModuleBase().getPrefab().getTransformedBounding());
            DrawHelper.drawCoordinateAxis();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();

            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTimelineEditor te) {
        return true;
    }
}
