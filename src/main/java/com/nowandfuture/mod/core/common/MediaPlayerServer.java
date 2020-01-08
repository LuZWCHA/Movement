package com.nowandfuture.mod.core.common;

import com.nowandfuture.ffmpeg.FrameGrabber;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.PlayHandler;

import java.util.Map;

public class MediaPlayerServer implements IMediaPlayer {

    SyncInfo syncInfo;
    private long totalTime;

    public MediaPlayerServer(){
        syncInfo = new SyncInfo();
    }

    @Override
    public void prepare(PlayHandler videoHandler, PlayHandler audioHandler) {

    }

    @Override
    public boolean touchSource(String url) throws FrameGrabber.Exception {
        return false;
    }

    @Override
    public void play() throws FrameGrabber.Exception {

    }

    @Override
    public void end() throws FrameGrabber.Exception {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public boolean seekTo(long time) {
        return true;
    }

    @Override
    public void getInfo(Map<String, String> metadata) {

    }

    @Override
    public void updateTotalTime(long time) {
        totalTime = time;
    }

    @Override
    public SyncInfo getSyncInfo() {
        return syncInfo;
    }

    public long getTotalTime() {
        return totalTime;
    }
}
