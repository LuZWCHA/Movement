package com.nowandfuture.mod.core.client.renders.videorenderer;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.Java2DFrameConverter;
import com.nowandfuture.ffmpeg.player.OpenGLDisplayHandler;
import com.nowandfuture.mod.Movement;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.image.BufferedImage;

public class MinecraftOpenGLDisplayHandler extends OpenGLDisplayHandler {

    private ImageFrame imageFrame = new ImageFrame();
    private long last = -1;
    private Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();

    public MinecraftOpenGLDisplayHandler(){

    }

    @Override
    public void init(IMediaPlayer.SyncInfo info) {
        super.init(info);
        if(id <= 0){
            throw new RuntimeException("never gen a texture !");
        }
    }

    @Override
    public void handle(Frame frame) {
        if(frame != null && frame.timestamp != last){
            if (imageFrame.image != null) imageFrame.image.getGraphics().dispose();
            imageFrame.image = java2DFrameConverter.getBufferedImage(frame);
            imageFrame.timestamp = frame.timestamp;
            last = frame.timestamp;
        }

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object getFrameObj() {
        return imageFrame;
    }

    public static class ImageFrame{
        private boolean isDisposed;
        public BufferedImage image;
        public long timestamp;

        public ImageFrame(){
            isDisposed = false;
        }

        public synchronized boolean isDisposed() {
            return isDisposed;
        }

        public synchronized void disposed(){
            if(image != null){
                isDisposed = true;
                image.getGraphics().dispose();
            }
        }

        public BufferedImage getCloneImage() {
            return Java2DFrameConverter.cloneBufferedImage(image);
        }
    }
}
