package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;

public interface PlayHandler {
    void init(IMediaPlayer.SyncInfo info);
    void handle(Frame frame) throws InterruptedException;
    void flush();
    void destroy();
    Object getFrameObj();
}
