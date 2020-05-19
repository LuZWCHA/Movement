package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import net.minecraft.client.renderer.GlStateManager;

public class OpenGLDisplayHandler implements PlayHandler.DisplayHandler {
    protected int id;
    private Frame frame;

    public OpenGLDisplayHandler() {
        id = GlStateManager.generateTexture();
    }

    @Override
    public void init(IMediaPlayer.SyncInfo info) {

    }

    @Override
    public void handle(Frame frame) {
        this.frame = frame;
    }

    @Override
    public void flush() {

    }

    @Override
    public void destroy() {
        GlStateManager.deleteTexture(id);
    }

    @Override
    public Object getFrameObj() {
        return frame;
    }

    @Override
    public void setGamma(float gamma) {

    }
}
