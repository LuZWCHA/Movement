package com.nowandfuture.mod.core.client.renders.tiles;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.Java2DFrameConverter;
import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.mod.core.client.renders.FrameTexture;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

import static org.lwjgl.opengl.GL11.*;

public class VideoRenderer extends TileEntitySpecialRenderer<TileEntitySimplePlayer> {

    private long lastTimeStamp;
    private FrameTexture imageTexture;


    @Override
    public void render(TileEntitySimplePlayer te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        SimplePlayer simplePlayer = (SimplePlayer) te.getSimplePlayer();
        if(simplePlayer == null) return;
        Frame frame = simplePlayer.getCurImageFrame();
        if(frame != null){
            if(frame.timestamp != lastTimeStamp) {
                lastTimeStamp = frame.timestamp;
                BufferedImage image = new BufferedImage(frame.imageWidth, frame.imageHeight, BufferedImage.TYPE_3BYTE_BGR);
                Java2DFrameConverter.copy(frame, image);
                if(imageTexture == null)
                    imageTexture = new FrameTexture(image);
                else{
                    imageTexture.updateBufferedImage(image);
                }
            }

            this.setLightmapDisabled(true);

            TextureManager textureManager = Minecraft.getMinecraft().renderEngine;
            ResourceLocation location =
                    textureManager.getDynamicTextureLocation("video",imageTexture);

            textureManager.bindTexture(location);

            BufferBuilder var2 = Tessellator.getInstance().getBuffer();
            Tessellator tessellator = Tessellator.getInstance();
            GlStateManager.color(1,1,1,1);
            glPushMatrix();
            var2.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            var2.pos(0,0,0).tex(1,1).endVertex();
            var2.pos(0,2,0).tex(1,0).endVertex();
            var2.pos(2,2,0).tex(0,0).endVertex();
            var2.pos(2,0,0).tex(0,1).endVertex();
            var2.pos(0,0,0).tex(1,1).endVertex();
            tessellator.draw();

            glPopMatrix();
            textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        }
        GlStateManager.popMatrix();
        this.setLightmapDisabled(false);
    }

}
