package com.nowandfuture.mod.core.client.renders.tiles;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.Java2DFrameConverter;
import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.mod.core.client.renders.FrameTexture;
import com.nowandfuture.mod.core.client.renders.MinecraftOpenGLDisplayHandler;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import com.nowandfuture.mod.utils.MathHelper;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL12;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

// TODO: 2020/2/1  visual focus algorithm and cull of unseeing panel
public class VideoRenderer extends TileEntitySpecialRenderer<TileEntitySimplePlayer> {

    //抽帧来提高性能
    public static int DrawFrameNum = 0;
    private Map<BlockPos,FrameTexture> frameCache = new HashMap<>();
    private Random r = new Random();

    @Override
    public void render(TileEntitySimplePlayer te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        Vec3d pos = new Vec3d(rendererDispatcher.entityX,rendererDispatcher.entityY,rendererDispatcher.entityZ);
        Vec3d v = pos.subtract(te.getPos().getX(),te.getPos().getY(),te.getPos().getZ());
        Vec3i d = te.getFacing().getDirectionVec();
        if(v.lengthSquared() > (1<<8) || v.dotProduct(new Vec3d(d.getX(),d.getY(),d.getZ())) < 0){
            return;
        }

        SimplePlayer simplePlayer = (SimplePlayer) te.getSimplePlayer();
        if(simplePlayer == null) return;
        MinecraftOpenGLDisplayHandler.ImageFrame frame = (MinecraftOpenGLDisplayHandler.ImageFrame) simplePlayer.getCurImageObj();

        BufferedImage image = (frame == null ? null : frame.image);

        if(image != null){
            if(r.nextInt(100) <= 100 - DrawFrameNum) {
                long time = System.currentTimeMillis();

//                System.out.println("+"+(System.currentTimeMillis() - time));
                if(frameCache.containsKey(te.getPos())) {
                    frameCache.get(te.getPos()).subBufferedImage(image,frame.timestamp);
                }else{
                    FrameTexture imageTexture = new FrameTexture(image.getWidth(),image.getHeight());
                    imageTexture.updateBufferedImage(image,frame.timestamp);
                    frameCache.put(te.getPos(),imageTexture);
                }
//                System.out.println(System.currentTimeMillis() - time);

            }

            FrameTexture texture = frameCache.get(te.getPos());

            final int videoWidth = texture.getWidth();
            final int videoHeight = texture.getHeight();
            final float w = te.getWidth(),h = te.getHeight();

            float scale = 1;
            if(videoWidth > 0 && videoHeight > 0){
                scale = videoHeight/(float)videoWidth;
            }

            Vec3d[] panel = new Vec3d[4];
            panel[0] = new Vec3d(0,0,0);
            panel[1] = new Vec3d(0,h * scale,0);
            panel[2] = new Vec3d(w,h * scale,0);
            panel[3] = new Vec3d(w,0,0);

            transformPanel(panel,te.getFacing());

            TextureManager textureManager = Minecraft.getMinecraft().renderEngine;
            ResourceLocation location =
                    textureManager.getDynamicTextureLocation("video",texture);

            this.setLightmapDisabled(false);
            textureManager.bindTexture(location);
            RenderHelper.disableStandardItemLighting();

            BufferBuilder var2 = Tessellator.getInstance().getBuffer();
            Tessellator tessellator = Tessellator.getInstance();
            GlStateManager.resetColor();

            GlStateManager.disableAlpha();

            glPushMatrix();
            //-w/2,-h * scale/2
            GlStateManager.translate(x,y + 1,z);
            var2.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
//            GlStateManager.color(1,1,1,1);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

            var2.pos(panel[0].x,panel[0].y,panel[0].z).tex(1,1).endVertex();
            var2.pos(panel[1].x,panel[1].y,panel[1].z).tex(1,0).endVertex();
            var2.pos(panel[2].x,panel[2].y,panel[2].z).tex(0,0).endVertex();
            var2.pos(panel[3].x,panel[3].y,panel[3].z).tex(0,1).endVertex();
            var2.pos(panel[0].x,panel[0].y,panel[0].z).tex(1,1).endVertex();

            tessellator.draw();
            glPopMatrix();
            RenderHelper.enableStandardItemLighting();

            GlStateManager.enableAlpha();
            this.setLightmapDisabled(true);
            textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        }

    }


    private void transformPanel(Vec3d[] panel, EnumFacing facing){
        switch (facing){
            case NORTH:
            case DOWN:
            case UP:
                break;
            case EAST:
                for (int i = 0;i < 4;i++) {
                    panel[i] = MathHelper.rotateAroundVector(panel[i],0,1,0,90 * 0.017453292F);
                }
                break;
            case WEST:
                for (int i = 0;i < 4;i++) {
                    panel[i] = MathHelper.rotateAroundVector(panel[i],0,1,0,-90 * 0.017453292F);
                }
                break;
            case SOUTH:
                for (int i = 0;i < 4;i++) {
                    panel[i] = MathHelper.rotateAroundVector(panel[i],0,1,0,180 * 0.017453292F);
                }
                break;
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntitySimplePlayer te) {
        return true;
    }
}
