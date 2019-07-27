package com.nowandfuture.mod.core.client.renders.tiles;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.nowandfuture.asm.RenderHook;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.handler.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.client.ForgeHooksClient;

public class TileEntityModuleRenderer<T extends TileEntityModule> extends TileEntitySpecialRenderer<T> {
    @Override
    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if(te.isRenderValid() && te.getModuleBase().getPrefab() != null) {
//            Minecraft.getMinecraft().entityRenderer.disableLightmap();//use inside lightmap-setting

            GlStateManager.pushMatrix();
            GlStateManager.translate(x,y,z);
            te.renderTileEntities(partialTicks);
            GlStateManager.popMatrix();
            RenderHandler.addRenderer(te);

//            Minecraft.getMinecraft().entityRenderer.enableLightmap();
        }
    }

    @Override
    public boolean isGlobalRenderer(T te) {
        return true;
    }
}
