package com.nowandfuture.mod.core.client.renders.tiles;

import com.google.common.collect.Lists;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.Java2DFrameConverter;
import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.client.renders.FrameTexture;
import com.nowandfuture.mod.core.client.renders.MinecraftOpenGLDisplayHandler;
import com.nowandfuture.mod.core.client.renders.PixelBuffer;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import com.nowandfuture.mod.handler.RenderHandler;
import com.nowandfuture.mod.utils.MathHelper;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.GL12;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraft.tileentity.TileEntity.INFINITE_EXTENT_AABB;
import static org.lwjgl.opengl.ARBBufferObject.GL_STREAM_DRAW_ARB;
import static org.lwjgl.opengl.ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB;
import static org.lwjgl.opengl.ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;

// TODO: 2020/2/1  visual focus algorithm and cull of unseeing panel
//PBO is not support... so ,no improve...
public class VideoRenderer extends TileEntitySpecialRenderer<TileEntitySimplePlayer> {

    //improve performance ,value 0~100 is valid
    public static int DrawFrameNum = 0;
    public static double LookDistance = 12;
    private Map<BlockPos,FrameTexture> frameCache = new HashMap<>();
    //unused
    private Map<BlockPos,PixelBuffer> frameCache2 = new HashMap<>();
    private Random r = new Random();

    private FrameTexture loadingTexture;
    //unused
    private static final ResourceLocation LOADING_GUI_TEXTURE = new ResourceLocation(Movement.MODID,"textures/gui/loading.png");


    public VideoRenderer(){
    }

    public void clear(){
        frameCache.forEach(new BiConsumer<BlockPos, FrameTexture>() {
            @Override
            public void accept(BlockPos pos, FrameTexture texture) {
                texture.deleteGlTexture();
            }
        });
        frameCache.clear();
        if(loadingTexture != null){
            loadingTexture.deleteGlTexture();
        }
    }

    public void clear(TileEntitySimplePlayer player){
        frameCache.entrySet().removeIf(new Predicate<Map.Entry<BlockPos, FrameTexture>>() {
            @Override
            public boolean test(Map.Entry<BlockPos, FrameTexture> blockPosFrameTextureEntry) {
                return blockPosFrameTextureEntry.getKey().equals(player.getPos());
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

        BufferedImage image = (frame == null ? null : frame.image);

        if(image == null){
            try {
                if(simplePlayer.getGrabber() != null && simplePlayer.getGrabber().getFormatContext() != null)
                {
                    if(loadingTexture == null){
                        IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(LOADING_GUI_TEXTURE);
                        BufferedImage bufferedimage = TextureUtil.readBufferedImage(iresource.getInputStream());

                        loadingTexture = new FrameTexture(bufferedimage.getWidth(),bufferedimage.getHeight());
                        loadingTexture.updateBufferedImage(bufferedimage,0);
                    }
                    drawFrame(loadingTexture,te,x,y,z);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            Integer frameLimit = RenderHandler.getScores().get(te.getPos());
            if(frameLimit == null) frameLimit = 128;
            if(r.nextInt(100) <= 100 - DrawFrameNum &&
                    r.nextInt(128) <= frameLimit) {
                uploadTexture(te,frame);
            }
            //if pbo supported,I will try again
//            PixelBuffer pixelBuffer = frameCache2.get(te.getPos());
//            drawFrame(pixelBuffer,te,x,y,z);

            FrameTexture texture = frameCache.get(te.getPos());
            if(texture != null)
                drawFrame(texture,te,x,y,z);
        }

    }

    //unused
    private void uploadTextureUsePBO(TileEntitySimplePlayer te, MinecraftOpenGLDisplayHandler.ImageFrame frame){

        BufferedImage image = frame.image;

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

        PixelBuffer pixelBuffer;
        if(!frameCache2.containsKey(te.getPos())){
            pixelBuffer = new PixelBuffer();
            frameCache2.put(te.getPos(),pixelBuffer);
        }
        pixelBuffer = frameCache2.get(te.getPos());

        pixelBuffer.bindPBO(GL_PIXEL_UNPACK_BUFFER_ARB);
        DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        byteBuffer.flip();
        pixelBuffer.pboByteData(GL_PIXEL_UNPACK_BUFFER_ARB,0,GL_STREAM_DRAW_ARB);
        ByteBuffer b = pixelBuffer.mapPBO(GL_PIXEL_UNPACK_BUFFER_ARB, ARBBufferObject.GL_READ_WRITE_ARB,byteBuffer);
        if(b != null){
            b.put(buffer.getData());
            pixelBuffer.unmapPBO(GL_PIXEL_UNPACK_BUFFER_ARB);
        }

        //Send texel data to OpenGL
        pixelBuffer.bindTexture();
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0,0, image.getWidth(), image.getHeight(), GL_BGR, GL_UNSIGNED_BYTE,0);
        pixelBuffer.unbindPBO(GL_PIXEL_UNPACK_BUFFER_ARB);
    }

    private void uploadTexture(TileEntitySimplePlayer te, MinecraftOpenGLDisplayHandler.ImageFrame frame){

        BufferedImage image = frame.image;

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
            imageTexture.updateBufferedImage(bufferedimage,frame.timestamp);
            imageTexture.setRealHeight(image.getHeight());
            imageTexture.setRealWidth(image.getWidth());
            frameCache.put(te.getPos(),imageTexture);
        }else {
            frameCache.get(te.getPos()).subBufferedImage(image, offsetX, offsetY, frame.timestamp);
        }
    }

    private void drawFrame(FrameTexture texture,TileEntitySimplePlayer te,double x,double y,double z){

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


    private void drawFrame(PixelBuffer texture,TileEntitySimplePlayer te,double x,double y,double z){

        Vec3d[] panel = new Vec3d[4];
        panel[0] = new Vec3d(0,0,0);
        panel[1] = new Vec3d(0,te.getHeight(),0);
        panel[2] = new Vec3d(te.getWidth(),te.getHeight(),0);
        panel[3] = new Vec3d(te.getWidth(),0,0);

        transformPanel(panel,te.getFacing());

        TextureManager textureManager = Minecraft.getMinecraft().renderEngine;
        texture.bindTexture();

        this.setLightmapDisabled(false);
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
