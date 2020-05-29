package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;

public interface PlayHandler {
    void init(IMediaPlayer.SyncInfo info);
    long handle(Frame frame) throws InterruptedException;
    @Deprecated
    void flush();
    void destroy();
    Object getFrameObj();

    interface SoundPlayHandler extends PlayHandler{
        void setVolume(float volume);
    }

    interface DisplayHandler extends PlayHandler{
        void setGamma(float gamma);
    }

}
