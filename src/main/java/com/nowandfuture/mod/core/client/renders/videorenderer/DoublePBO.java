package com.nowandfuture.mod.core.client.renders.videorenderer;

import com.nowandfuture.mod.Movement;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
//never used
public class DoublePBO {
    PixelBufferObject[] pbos;
    private int textureId;
    private long tag;

    private int swapIndex = 0;

    public DoublePBO(){
        pbos = new PixelBufferObject[2];
        PixelBufferObject pbo1 = new PixelBufferObject();
        PixelBufferObject pbo2 = new PixelBufferObject();

        pbos[0] = pbo1;
        pbos[1] = pbo2;
    }

    public void bindNextPBO(int target){
        bindPBO(swapIndex,target);
        swapIndex = (swapIndex + 1)%2;
    }

    public void bindPBO(int index,int target){
        pbos[index].bindPBO(target);
    }

    public void unbindPBO(int target){
        GL15.glBindBuffer(target, 0);
        checkError();
    }

    public ByteBuffer mapCurrentPBO(int target,int access,ByteBuffer oldBuffer){
        return pbos[swapIndex].mapPBO(target, access, oldBuffer);
    }

    public ByteBuffer mapCurrentPBO(int target,int access,long length,ByteBuffer oldBuffer){
        return pbos[swapIndex].mapPBO(target, access, length, oldBuffer);
    }

    public void pboByteData(int target, int size,int usage){
        pbos[swapIndex].pboByteData(target, size, usage);
    }

    public boolean unmapPBO(int target){
        return pbos[swapIndex].unmapPBO(target);
    }

    public void bindTexture(){
        if(textureId == -1){
            textureId = GL11.glGenTextures();
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        checkError();
    }

    public void deletePBOs(){
        pbos[0].delete();
        pbos[1].delete();
    }

    public int getTextureId() {
        return textureId;
    }

    public long getTag() {
        return tag;
    }

    public void setTag(long tag) {
        this.tag = tag;
    }

    private void checkError(){
        int error = GL11.glGetError();
        if(error != GL11.GL_NO_ERROR){
            Movement.logger.warn("OpenGL Error:" + error);
        }
    }
}
