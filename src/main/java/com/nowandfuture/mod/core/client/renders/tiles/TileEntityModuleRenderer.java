package com.nowandfuture.mod.core.client.renders.tiles;

import com.nowandfuture.mod.core.client.renders.CubesRenderer;
import com.nowandfuture.mod.core.client.renders.ModuleRenderManager;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.handler.RenderHandler;
import com.nowandfuture.mod.utils.DrawHelper;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityModuleRenderer<T extends TileEntityModule> extends TileEntitySpecialRenderer<T> {
    @Override
    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if(te.isEnable() && te.getPrefab() != null) {
            CubesRenderer renderer = ModuleRenderManager.INSTANCE.getRenderer(te.getPrefab());
            if(renderer != null){
                if(renderer.isBuilt()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x, y, z);

                    renderer.resetMatrix();
                    te.getModuleBase().transformPre(partialTicks, renderer.getModelMatrix());
                    te.getModuleBase().transformPost(partialTicks, renderer.getModelMatrix());

                    if(Minecraft.getMinecraft().gameSettings.showDebugInfo) {
                        OBBox obBox = new OBBox(te.getMinAABB());
                        Matrix4f matrix4f = te.getModuleBase().getTransRes();
                        obBox.mulMatrix(matrix4f);

                        DrawHelper.preDraw();
                        DrawHelper.drawOutlinedBoundingBox(obBox);
                        DrawHelper.postDraw();

                        if(te.getImpactAxis() != null){
                            DrawHelper.drawLine(0,0,0,
                                    te.getImpactAxis().x,te.getImpactAxis().y,te.getImpactAxis().z,1,0,0);
                        }


                    }

                    renderer.renderTileEntity(partialTicks);
                    GlStateManager.popMatrix();
                    RenderHandler.addRenderer(renderer);
                }else {
                    renderer.build();
                }
            }
        }
    }

    @Override
    public boolean isGlobalRenderer(T te) {
        return true;
    }
}
