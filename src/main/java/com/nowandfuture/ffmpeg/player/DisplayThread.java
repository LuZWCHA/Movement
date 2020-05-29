package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;

import java.util.concurrent.BlockingQueue;

public class DisplayThread extends Thread{
    private long factor;
    private long baseDelay;
    private BlockingQueue<Frame> imageCache;

    private final IMediaPlayer.SyncInfo syncInfo;

    private PlayHandler playHandler;

    DisplayThread(IMediaPlayer.SyncInfo syncInfo){
        this.syncInfo = syncInfo;
        factor = 0;
    }

    public void setImageCache(BlockingQueue<Frame> imageCache) {
        this.imageCache = imageCache;
    }

    public void setBaseDelay(long delay) {
        this.baseDelay = delay;
    }

    @Override
    public void run() {
        init();
        Frame frame = null;
        try {
            while (!isInterrupted()){
                synchronized (syncInfo) {
                    while (syncInfo.isPause()) {
                        syncInfo.wait();
                    }
                }

                frame = imageCache.poll();

                render(frame);

                if(frame != null)
                    SoundUtils.cloneFrameDeallocate(frame);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if(frame != null)
                SoundUtils.cloneFrameDeallocate(frame);
        }
        finished();
    }

    private void init() {
        if(playHandler != null){
            playHandler.init(syncInfo);
        }
    }

    public void finished(){
        if(playHandler != null){
            playHandler.destroy();
        }
    }

    private void checkDiff(Frame frame,long time) throws InterruptedException {
        final long timestamp = frame.timestamp;

        final long diff2 = timestamp - syncInfo.getRealAudioClock(time);
        long delay2 = Math.floorDiv(diff2,1000);

        if(diff2 > 0) {
            if (delay2 > IMediaPlayer.SyncInfo.MAX_VIDEO_DIFF) delay2 = IMediaPlayer.SyncInfo.MAX_VIDEO_DIFF;
            sleep(delay2);
            factor = -delay2;
        }else{
            factor = delay2;
        }

        if(factor < -baseDelay) factor = -baseDelay;
    }

    protected void render(Frame frame) throws InterruptedException {
        final long time = System.currentTimeMillis();
        if(frame == null) {
            if (syncInfo.isDecodeFinished()) {
                syncInfo.setPause(true);
            }

            return;
        }
        checkDiff(frame,time);

        draw(frame);
        Thread.sleep(baseDelay + factor);
    }

    protected void draw(Frame frame) throws InterruptedException {
        if(playHandler != null){
            playHandler.handle(frame);
        }
    }

    public void setHandler(PlayHandler playHandler){
        if(playHandler == null && this.playHandler != null) this.playHandler.destroy();
        this.playHandler = playHandler;
    }

    public void setRun(boolean run) {
    }

    public PlayHandler getPlayHandler(){
        return this.playHandler;
    }
}
