package com.nowandfuture.mod.core.client.renders;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.Java2DFrameConverter;
import com.nowandfuture.ffmpeg.player.OpenGLDisplayHandler;
import com.nowandfuture.mod.Movement;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.image.BufferedImage;

public class MinecraftOpenGLDisplayHandler extends OpenGLDisplayHandler {

    private ImageFrame imageFrame = new ImageFrame();
    private long last = -1;
    private Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();

    @Override
    public void handle(Frame frame) {
        if(frame != null && frame.timestamp != last){
            imageFrame.image = java2DFrameConverter.getBufferedImage(frame);
            imageFrame.timestamp = frame.timestamp;
            last = frame.timestamp;
        }

    }

    @Override
    public void destroy() {
        Movement.proxy.addScheduledTaskClient(new Runnable() {
            @Override
            public void run() {
                GlStateManager.deleteTexture(id);
            }
        });
    }

    @Override
    public Object getFrameObj() {
        return imageFrame;
    }

    public static class ImageFrame{
        public BufferedImage image;
        public long timestamp;
    }
}
