package com.nowandfuture.mod.core.client.renders;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;
import sun.nio.ch.DirectBuffer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;

public class PBOFrameTexture extends FrameTexture {
    private PixelBuffer pbo;

    public PBOFrameTexture(BufferedImage p_i1270_1_) {
        super(p_i1270_1_);

    }

    public PBOFrameTexture(int p_i1271_1_, int p_i1271_2_) {
        super(p_i1271_1_, p_i1271_2_);
        pbo = new PixelBuffer();
    }

    public void updateBufferedImage(BufferedImage image,long id){
        pbo.setTag(id);
        BufferedImage bufferedimage = new BufferedImage(image.getWidth(),image.getHeight(),image.getType());
        DataBufferByte buffer = (DataBufferByte) bufferedimage.getRaster().getDataBuffer();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(bufferedimage.getWidth() * bufferedimage.getHeight() * 4).put(buffer.getData());
        byteBuffer.flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, bufferedimage.getWidth(), bufferedimage.getHeight(), 0, GL_BGR, GL_UNSIGNED_BYTE,byteBuffer);
        ((DirectBuffer)byteBuffer).cleaner().clean();
        bufferedimage.getGraphics().dispose();
    }

    @Override
    public void subBufferedImage(BufferedImage image, int offsetX, int offsetY, long id) {
        if(id == pbo.getTag()) return;
        pbo.setTag(id);
        GlStateManager.bindTexture(glTextureId);

        pbo.bindPBO(GL21.GL_PIXEL_UNPACK_BUFFER);
        DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
        pbo.pboByteData(GL21.GL_PIXEL_UNPACK_BUFFER,
                image.getWidth() * image.getHeight() * 4, GL15.GL_STREAM_DRAW);
        ByteBuffer b = pbo.mapPBO(GL21.GL_PIXEL_UNPACK_BUFFER, GL15.GL_WRITE_ONLY, null);
        if (b != null && b.hasRemaining()) {
            b.put(buffer.getData());
            pbo.unmapPBO(GL21.GL_PIXEL_UNPACK_BUFFER);
        }

        //Send texel data to OpenGL
        glTexSubImage2D(GL_TEXTURE_2D, 0, offsetX, offsetY, image.getWidth(), image.getHeight(), GL_BGR, GL_UNSIGNED_BYTE, 0);
        pbo.unbindPBO(GL21.GL_PIXEL_UNPACK_BUFFER);
    }

    @Override
    public void deleteGlTexture() {
        pbo.delete();
        super.deleteGlTexture();
    }
}
