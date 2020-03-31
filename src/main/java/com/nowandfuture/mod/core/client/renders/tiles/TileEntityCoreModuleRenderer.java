package com.nowandfuture.mod.core.client.renders.tiles;

import com.nowandfuture.mod.core.client.renders.CubesRenderer;
import com.nowandfuture.mod.core.client.renders.ModuleRenderManager;
import com.nowandfuture.mod.core.common.entities.TileEntityCoreModule;
import com.nowandfuture.mod.core.movecontrol.ModuleNode;
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.utils.DrawHelper;
import com.nowandfuture.mod.utils.math.Matrix4f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TileEntityCoreModuleRenderer extends TileEntitySpecialRenderer<TileEntityCoreModule>{

    @Override
    public void render(TileEntityCoreModule te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        te.doTransform(partialTicks,matrix4f);
        renderModuleTree(te,x, y, z, partialTicks);

        if(te.isShowBlock()) {
            renderBlock(te, x, y, z);
        }
    }


    @SideOnly(Side.CLIENT)
    public void renderModuleTree(ModuleNode node, double x, double y, double z, float partialTicks){
        CubesRenderer renderer = ModuleRenderManager.INSTANCE.getRenderer(node.getPrefab());

        if(renderer != null){
            if(renderer.isBuilt()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);

                renderer.getModelMatrix().load(node.getMatrix4f());
                renderer.renderTileEntity(partialTicks);
                //RenderHook render it later
                ModuleRenderManager.INSTANCE.getRenderQueue().add(renderer);

                if(Minecraft.getMinecraft().gameSettings.showDebugInfo) {
                    OBBox obBox = new OBBox(node.getMinAABB().grow(0.002));

                    DrawHelper.preDraw();
                    DrawHelper.drawOutlinedBoundingBox(obBox);
                    DrawHelper.postDraw();

//                    if(te.getImpactAxis() != null){
//                        DrawHelper.drawLine(0,0,0,
//                                te.getImpactAxis().x,te.getImpactAxis().y,te.getImpactAxis().z,1,0,0);
//                    }

                }
                GlStateManager.popMatrix();

                for (ModuleNode moduleNode :
                        node.getModuleMap().getModules()) {
                    renderModuleTree(moduleNode,x,y,z,partialTicks);
                }

            }else {
                renderer.build();
            }
        }

    }

    public void renderBlock(TileEntityCoreModule te, double x, double y, double z){
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x-te.getPos().getX(),y-te.getPos().getY(),z-te.getPos().getZ());
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        int i = this.getWorld().getCombinedLight(te.getPos(), 0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBlockState blockState = getWorld().getBlockState(te.getPos());
        IBakedModel model = dispatcher.getBlockModelShapes().getModelForState(blockState);
        dispatcher.getBlockModelRenderer().renderModel(getWorld(),model,blockState,
                te.getPos(),bufferBuilder,false);

        tessellator.draw();

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();

    }
}
