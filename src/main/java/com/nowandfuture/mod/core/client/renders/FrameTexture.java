package com.nowandfuture.mod.core.client.renders;

import com.nowandfuture.mod.Movement;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import sun.nio.ch.DirectBuffer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;

public class FrameTexture extends DynamicTexture {
    private static final int BYTES_PER_PIXEL = 4;
    private int width,height;
    private int aw,ah;//sub image size;
    protected long id;

    public FrameTexture(BufferedImage p_i1270_1_) {
        super(p_i1270_1_);
        width = p_i1270_1_.getWidth();
        height = p_i1270_1_.getHeight();
    }

    public FrameTexture(int p_i1271_1_, int p_i1271_2_) {
        super(p_i1271_1_, p_i1271_2_);
        width = p_i1271_1_;
        height = p_i1271_2_;
    }

    public void updateBufferedImage(BufferedImage image,long id){
        this.id = id;
        GlStateManager.bindTexture(glTextureId);
        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL)
                .put(buffer.getData().clone());
        byteBuffer.flip();

        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_BGR, GL_UNSIGNED_BYTE,byteBuffer);
        ((DirectBuffer)byteBuffer).cleaner().clean();

        int error = GL11.glGetError();
        if(error != GL_NO_ERROR){
            System.out.println("error:" + error);
        }
    }

    public void subBufferedImage(BufferedImage image,int offsetX,int offsetY,long id){
        if(id == this.id) return;
        this.id = id;
        GlStateManager.bindTexture(glTextureId);

        DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL)
                .put(buffer.getData().clone());
        byteBuffer.flip();

        //Send texel data to OpenGL
        glTexSubImage2D(GL_TEXTURE_2D, 0, offsetX,offsetY, image.getWidth(), image.getHeight(), GL_BGR, GL_UNSIGNED_BYTE,byteBuffer);

        ((DirectBuffer)byteBuffer).cleaner().clean();

        int error = GL11.glGetError();
        if(error != GL_NO_ERROR){
            Movement.logger.warn("OpenGL error:" + error);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getId() {
        return id;
    }

    public int getRealHeight() {
        return ah;
    }

    public int getRealWidth() {
        return aw;
    }

    public void setRealHeight(int ah) {
        this.ah = ah;
    }

    public void setRealWidth(int aw) {
        this.aw = aw;
    }
}
