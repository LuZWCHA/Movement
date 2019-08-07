package com.nowandfuture.mod.core.client.renders.tiles;

import com.nowandfuture.mod.core.common.entities.TileEntityShowModule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TileEntityModuleShowerRenderer extends TileEntityModuleRenderer<TileEntityShowModule>{

    private BufferBuilder bufferBuilder = new BufferBuilder(2097152);
    private WorldVertexBufferUploader worldVertexBufferUploader = new WorldVertexBufferUploader();

    @Override
    public void render(TileEntityShowModule te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        if(te.isShowBlock()) {
            renderBlock(te, x, y, z);
        }
    }

    public void renderBlock(TileEntityShowModule te, double x, double y, double z){
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x-te.getPos().getX(),y-te.getPos().getY(),z-te.getPos().getZ());
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

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

        bufferBuilder.finishDrawing();
        worldVertexBufferUploader.draw(bufferBuilder);

        GlStateManager.popMatrix();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();

    }
}
