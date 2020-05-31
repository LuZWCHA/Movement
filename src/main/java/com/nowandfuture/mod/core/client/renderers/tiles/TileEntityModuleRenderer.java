package com.nowandfuture.mod.core.client.renderers.tiles;

import com.nowandfuture.mod.core.client.renderers.cubes.CubesRenderer;
import com.nowandfuture.mod.core.client.renderers.ModuleRenderManager;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class TileEntityModuleRenderer<T extends TileEntityModule> extends TileEntitySpecialRenderer<T> {
    private static Random RANDOM = new Random();

    @Override
    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        AbstractPrefab prefab = te.getPrefab();

        if(te.isEnable() && prefab != null) {
            CubesRenderer renderer = ModuleRenderManager.INSTANCE.getRenderer(prefab);
            if(renderer != null){
                if(te.getRenderRealtime() >= 0 && RANDOM.nextInt(te.getRenderRealtime() + 1) == 0){
                    renderer.forceUpdateAll();
                }

                if(renderer.isBuilt()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x, y, z);

                    renderer.getModelMatrix().setIdentity();
                    te.doTransform(partialTicks,renderer.getModelMatrix());
                    renderer.renderTileEntity(partialTicks);
                    //RenderHook render it later
                    addToRenderQueue(renderer);

                    if(Minecraft.getMinecraft().gameSettings.showDebugInfo) {
                        OBBox obBox = new OBBox(te.getMinAABB().grow(0.002));

                        DrawHelper.preDraw();
                        DrawHelper.drawOutlinedBoundingBox(obBox);
                        DrawHelper.postDraw();

                    }

                    GlStateManager.popMatrix();

                }else {
                    renderer.build();
                }
            }
        }
    }

    private void addToRenderQueue(CubesRenderer renderer){
        ModuleRenderManager.INSTANCE.getRenderQueue().add(renderer);
    }

    @Override
    public boolean isGlobalRenderer(T te) {
        return true;
    }
}