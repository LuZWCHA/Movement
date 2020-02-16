package com.nowandfuture.mod.core.client.renders.tiles;

import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.client.renders.videorenderer.FrameTexture;
import com.nowandfuture.mod.core.client.renders.videorenderer.MinecraftOpenGLDisplayHandler;
import com.nowandfuture.mod.core.client.renders.videorenderer.PBOFrameTexture;
import com.nowandfuture.mod.core.client.renders.videorenderer.VideoRendererUtil;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import com.nowandfuture.mod.handler.RenderHandler;
import com.nowandfuture.mod.utils.math.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static org.lwjgl.opengl.GL11.*;

//PBO is supported
public class VideoRenderer extends TileEntitySpecialRenderer<TileEntitySimplePlayer> {

    //improve performance ,value 0~100 is valid
    public static int DRAW_FRAME_NUMBER = 0;
    public static double MAX_DISTANCE = 12;
    private static Map<BlockPos,FrameTexture> frameCache = new HashMap<>();
    private static Map<BlockPos,PBOFrameTexture> frameCache2 = new HashMap<>();
    private Random r = new Random();

    private static FrameTexture loadingTexture;
    private static final ResourceLocation LOADING_GUI_TEXTURE = new ResourceLocation(Movement.MODID,"textures/gui/loading.png");

    private static int GC_COUNTER;

    public VideoRenderer(){
    }

    public static void clear(){
        frameCache.forEach(new BiConsumer<BlockPos, FrameTexture>() {
            @Override
            public void accept(BlockPos pos, FrameTexture texture) {
                texture.deleteGlTexture();
            }
        });
        frameCache2.forEach(new BiConsumer<BlockPos, PBOFrameTexture>() {
            @Override
            public void accept(BlockPos pos, PBOFrameTexture texture) {
                texture.deleteGlTexture();
            }
        });
        frameCache.clear();
        frameCache2.clear();
        if(loadingTexture != null){
            loadingTexture.deleteGlTexture();
        }
    }

    public void clear(TileEntitySimplePlayer player){
        frameCache.entrySet().removeIf(new Predicate<Map.Entry<BlockPos, FrameTexture>>() {
            @Override
            public boolean test(Map.Entry<BlockPos, FrameTexture> blockPosFrameTextureEntry) {
                if(blockPosFrameTextureEntry.getKey().equals(player.getPos())){
                    blockPosFrameTextureEntry.getValue().deleteGlTexture();
                    return true;
                }
                return false;
            }
        });

        frameCache2.entrySet().removeIf(new Predicate<Map.Entry<BlockPos, PBOFrameTexture>>() {
            @Override
            public boolean test(Map.Entry<BlockPos, PBOFrameTexture> blockPosFrameTextureEntry) {
                if(blockPosFrameTextureEntry.getKey().equals(player.getPos())){
                    blockPosFrameTextureEntry.getValue().deleteGlTexture();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(TileEntitySimplePlayer te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Vec3d pos = new Vec3d(rendererDispatcher.entityX,
                rendererDispatcher.entityY + rendererDispatcher.entity.getEyeHeight(),
                rendererDispatcher.entityZ);
        Vec3d v = pos.subtract(te.getPos().getX(),te.getPos().getY(),te.getPos().getZ());
        v.add(te.getScreenAABB().getCenter());
        Vec3i d = te.getFacing().getDirectionVec();

        if(v.lengthSquared() > (1<<8) || v.dotProduct(new Vec3d(d.getX(),d.getY(),d.getZ())) < 0){
            return;
        }

        SimplePlayer simplePlayer = (SimplePlayer) te.getSimplePlayer();
        if(simplePlayer == null) return;

        MinecraftOpenGLDisplayHandler.ImageFrame frame = (MinecraftOpenGLDisplayHandler.ImageFrame) simplePlayer.getCurImageObj();

        BufferedImage image = (frame == null ? null : frame.getCloneImage());

        if(image == null){
            try {
                if(simplePlayer.getGrabber() != null && simplePlayer.getGrabber().getFormatContext() != null)
                {
                    if(loadingTexture == null){
                        IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(LOADING_GUI_TEXTURE);
                        BufferedImage bufferedimage = TextureUtil.readBufferedImage(iresource.getInputStream());

                        loadingTexture = new FrameTexture(bufferedimage.getWidth(),bufferedimage.getHeight());
                        loadingTexture.updateBufferedImage(bufferedimage,0);
                        bufferedimage.getGraphics().dispose();
                    }
                    drawFrame(loadingTexture,te,x,y,z);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            Integer frameLimit = RenderHandler.getScores().get(te.getPos());
            if(frameLimit == null) frameLimit = 0;

            if(r.nextInt(100) <= 100 - DRAW_FRAME_NUMBER &&
                    r.nextInt(VideoRendererUtil.MAX_SCORE) <= frameLimit) {
                uploadTextureUsePBO(te,frame,image);
            }

            PBOFrameTexture pixelBuffer = frameCache2.get(te.getPos());
            if(pixelBuffer != null)
                drawFrame(pixelBuffer,te,x,y,z);
            image.getGraphics().dispose();
        }
    }

    private void uploadTextureUsePBO(TileEntitySimplePlayer te, MinecraftOpenGLDisplayHandler.ImageFrame frame, BufferedImage image){

        final float videoWidth = image.getWidth();
        final float videoHeight = image.getHeight();
        final float w = te.getWidth(),h = te.getHeight();

        float newW = w,newH = h;

        float scale;
        if(videoHeight / videoWidth > h / w){
            newW = videoWidth * h / videoHeight;
            scale = videoHeight / h;
        }else{
            newH = videoHeight * w / videoWidth;
            scale = videoWidth / w;
        }

        int offsetY = (int) ((h - newH) * scale / 2);
        int offsetX = (int) ((w - newW) * scale / 2);

        FrameTexture texture = frameCache2.get(te.getPos());
        if(texture!=null){
            //need update texture size
            if(texture.getRealHeight() != videoHeight || texture.getRealWidth() != videoWidth){
                texture.deleteGlTexture();
                frameCache2.remove(te.getPos());
            }
        }

        if(!frameCache2.containsKey(te.getPos())) {
            BufferedImage bufferedimage;
            bufferedimage = new BufferedImage(((int) (w * scale)), ((int) (h * scale)),image.getType());
            PBOFrameTexture imageTexture = new PBOFrameTexture(bufferedimage.getWidth(),bufferedimage.getHeight());
            bufferedimage.getGraphics().dispose();
            imageTexture.updateBufferedImage(bufferedimage,frame.timestamp);
            imageTexture.setRealHeight(image.getHeight());
            imageTexture.setRealWidth(image.getWidth());
            frameCache2.put(te.getPos(),imageTexture);
        }else {
            frameCache2.get(te.getPos()).subBufferedImage(image, offsetX, offsetY, frame.timestamp);
        }

    }

    @Deprecated
    private void uploadTexture(TileEntitySimplePlayer te, MinecraftOpenGLDisplayHandler.ImageFrame frame,BufferedImage image){

        final float videoWidth = image.getWidth();
        final float videoHeight = image.getHeight();
        final float w = te.getWidth(),h = te.getHeight();

        float newW = w,newH = h;

        float scale;
        if(videoHeight / videoWidth > h / w){
            newW = videoWidth * h / videoHeight;
            scale = videoHeight / h;
        }else{
            newH = videoHeight * w / videoWidth;
            scale = videoWidth / w;
        }

        int offsetY = (int) ((h - newH) * scale / 2);
        int offsetX = (int) ((w - newW) * scale / 2);

        FrameTexture texture = frameCache.get(te.getPos());
        if(texture!=null){
            //need update texture size
            if(texture.getRealHeight() != videoHeight || texture.getRealWidth() != videoWidth){
                texture.deleteGlTexture();
                frameCache.remove(te.getPos());
            }
        }

        if(!frameCache.containsKey(te.getPos())) {
            BufferedImage bufferedimage;
            bufferedimage = new BufferedImage(((int) (w * scale)), ((int) (h * scale)),image.getType());
            FrameTexture imageTexture = new FrameTexture(bufferedimage.getWidth(),bufferedimage.getHeight());
            bufferedimage.getGraphics().dispose();
            imageTexture.updateBufferedImage(bufferedimage,frame.timestamp);
            imageTexture.setRealHeight(image.getHeight());
            imageTexture.setRealWidth(image.getWidth());
            frameCache.put(te.getPos(),imageTexture);
        }else {
            frameCache.get(te.getPos()).subBufferedImage(image, offsetX, offsetY, frame.timestamp);
        }
    }

    private void drawFrame(FrameTexture texture, TileEntitySimplePlayer te, double x, double y, double z){

        Vec3d[] panel = new Vec3d[4];
        panel[0] = new Vec3d(0,0,0);
        panel[1] = new Vec3d(0,te.getHeight(),0);
        panel[2] = new Vec3d(te.getWidth(),te.getHeight(),0);
        panel[3] = new Vec3d(te.getWidth(),0,0);

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

    public void renderFrame(){

    }
}
