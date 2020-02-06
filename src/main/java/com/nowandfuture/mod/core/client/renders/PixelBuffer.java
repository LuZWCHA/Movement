package com.nowandfuture.mod.core.client.renders;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.ARBPixelBufferObject;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

//target:GL_PIXEL_PACK_BUFFER_ARB or GL_PIXEL_UNPACK_BUFFER_ARB
public class PixelBuffer {
    private int pboId;
    private int bindTextureId;

    public PixelBuffer(){
        pboId = ARBPixelBufferObject.glGenBuffersARB();
        checkError();
        System.out.println("new!!!!!!!!!!!!!!!!!!!!");
        bindTextureId = -1;
    }

    public void unbindPBO(int target){
        if(pboId != -1) {
            ARBPixelBufferObject.glBindBufferARB(target, 0);
            System.out.println("unbind");
            checkError();
        }
    }

    public void bindPBO(int target){
        if(pboId != -1) {
            ARBPixelBufferObject.glBindBufferARB(target, pboId);
            System.out.println("bind");
            checkError();
        }
    }

    //usage:GL_STREAM_DRAW_ARB,GL_STREAM_READ_ARB
    public void pboByteData(int target, ByteBuffer byteBuffer,int usage){
        ARBPixelBufferObject.glBufferDataARB(target,byteBuffer,usage);
    }

    //usage:GL_STREAM_DRAW_ARB,GL_STREAM_READ_ARB
    public void pboByteData(int target, int size,int usage){
        ARBPixelBufferObject.glBufferDataARB(target,size,usage);
        System.out.println("data");
        checkError();
    }

    //access:read data from the PBO (GL_READ_ONLY_ARB), write data to the PBO (GL_WRITE_ONLY_ARB), or both (GL_READ_WRITE_ARB)
    public ByteBuffer mapPBO(int target,int access,ByteBuffer oldBuffer){
        ByteBuffer byteBuffer =  ARBPixelBufferObject.glMapBufferARB(target,access,oldBuffer);
        System.out.println("map");
        checkError();
        return byteBuffer;
    }

    public ByteBuffer mapPBO(int target,int access,long length,ByteBuffer oldBuffer){
        return ARBPixelBufferObject.glMapBufferARB(target,access,length,oldBuffer);
    }

    public boolean unmapPBO(int target){
        boolean t = ARBPixelBufferObject.glUnmapBufferARB(target);
        System.out.println("map");
        checkError();
        return t;
    }

    public void bindTexture(){
        if(bindTextureId == -1){
            bindTextureId = GL11.glGenTextures();
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, bindTextureId);
        System.out.println("bindt");
        checkError();
    }

    public void readPixels(int x,int y,int width,int height,int format,int type,long pixels){
        GL11.glReadPixels(x, y, width, height, format, type, pixels);
        checkError();
    }

    public void delete(){
        if(pboId != -1)
            ARBPixelBufferObject.glDeleteBuffersARB(pboId);
        pboId = -1;

        if(bindTextureId != -1)
            GL11.glDeleteTextures(bindTextureId);

        checkError();
        bindTextureId = -1;
    }

    public int getPBOId() {
        return pboId;
    }

    private void checkError(){
        int error = GL11.glGetError();
        if(error != GL11.GL_NO_ERROR){
            System.out.println("error:" + error);
        }
    }
}
