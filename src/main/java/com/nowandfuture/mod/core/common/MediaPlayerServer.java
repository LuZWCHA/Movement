package com.nowandfuture.mod.core.common;

import com.nowandfuture.ffmpeg.FrameGrabber;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.PlayHandler;

import java.util.Map;

public class MediaPlayerServer implements IMediaPlayer {

    SyncInfo syncInfo;
    private long totalTime;
    private boolean enable;

    public MediaPlayerServer(){
        syncInfo = new SyncInfo();
    }

    @Override
    public void prepare() {
        if(syncInfo.isPause()){
            syncInfo.setPause(false);
        }
    }

    @Override
    public boolean touchSource(String url) throws FrameGrabber.Exception {
        return false;
    }

    @Override
    public void play() throws FrameGrabber.Exception {
        enable = true;
    }

    @Override
    public void end(){
        seekTo(0);
        enable = false;
    }

    @Override
    public void pause() {
        if(!syncInfo.isPause()){
            syncInfo.setPause(true);
        }
    }

    @Override
    public void resume() {
        if(syncInfo.isPause()){
            syncInfo.setPause(false);
        }
    }

    @Override
    public boolean seekTo(long time) {
        syncInfo.setAudioClock(time);
        return true;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public void getInfo(Map<String, String> metadata) {

    }

    @Override
    public void updateTotalTime(long time) {
        seekTo(0);
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
