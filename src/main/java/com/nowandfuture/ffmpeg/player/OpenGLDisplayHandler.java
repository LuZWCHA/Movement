package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import net.minecraft.client.renderer.GlStateManager;

public class OpenGLDisplayHandler implements PlayHandler {
    private int id;

    public OpenGLDisplayHandler() {
        id = GlStateManager.generateTexture();
    }

    @Override
    public void init(IMediaPlayer.SyncInfo info) {

    }

    @Override
    public void handle(Frame frame) {

    }

    @Override
    public void flush() {

    }

    @Override
    public void destroy() {
        GlStateManager.deleteTexture(id);
    }
}
