package com.nowandfuture.mod.core.client.renders.videorenderer;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.Java2DFrameConverter;
import com.nowandfuture.ffmpeg.player.OpenGLDisplayHandler;
import com.nowandfuture.ffmpeg.player.SimplePlayer;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class MinecraftOpenGLDisplayHandler extends OpenGLDisplayHandler {

    private ImageFrame imageFrame;
    private long last;
    private Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();

    public MinecraftOpenGLDisplayHandler(SimplePlayer simplePlayer){
        super(simplePlayer);
    }

    @Override
    public void init(IMediaPlayer.SyncInfo info) {
        super.init(info);
        if(id <= 0){
            throw new RuntimeException("never gen a texture !");
        }
        last = -1;
        imageFrame = new ImageFrame();
    }

    @Override
    public long handle(Frame frame) {
        super.handle(frame);

        if(frame != null && frame.image != null && frame.timestamp != last){
            if (imageFrame.image != null) imageFrame.image.getGraphics().dispose();
            imageFrame.image = java2DFrameConverter.getBufferedImage(frame);
            imageFrame.timestamp = frame.timestamp;
            last = frame.timestamp;
        }else if(frame != null && frame.image == null && frame.data != null){
            if (imageFrame.image != null) imageFrame.image.getGraphics().dispose();
            imageFrame.audioData = frame.data;
            imageFrame.image =null;
            imageFrame.timestamp = frame.timestamp;
            last = frame.timestamp;
        }

        return 0;
    }

    @Override
    public void destroy() {
        last = -1;
        super.destroy();
        if(imageFrame != null)
            imageFrame.disposed();
    }

    @Override
    public synchronized Object getFrameObj() {
        return imageFrame;
    }

    public static class ImageFrame{
        private boolean isDisposed;
        public BufferedImage image;
        public ByteBuffer audioData;
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
