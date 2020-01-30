package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;

import java.util.concurrent.BlockingQueue;

public class DisplayThread extends Thread{
    private long factor;
    private long baseDelay;
    private BlockingQueue<Frame> imageCache;

    private long lastTime;
    private Frame curFrame;
    private final IMediaPlayer.SyncInfo syncInfo;

    private PlayHandler playHandler;

    DisplayThread(IMediaPlayer.SyncInfo syncInfo){
        this.syncInfo = syncInfo;
        lastTime = -1;
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
        while (!isInterrupted()){
            synchronized (syncInfo) {

                if (syncInfo.isPause()) {
                    try {
                        syncInfo.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
//                if(syncInfo.isAudioFrameGet)
                render();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

    private void checkDiff(Frame frame,long time) {
        long diff = time - (lastTime == -1? time : lastTime);
        System.out.println("delay:" + diff);

        long timestamp = frame.timestamp;

        long diff2 = timestamp - syncInfo.getRealAudioClock(time);
        long delay2 = Math.floorDiv(diff2,1000);

        if(diff2 > 0) {
            if (delay2 > IMediaPlayer.SyncInfo.MAX_VIDEO_DIFF) delay2 = IMediaPlayer.SyncInfo.MAX_VIDEO_DIFF;
            try {
                sleep(delay2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            factor = -delay2;
        }else{
            factor = delay2;
        }

//        if(diff - baseDelay > SimplePlayer.SyncInfo.MAX_VIDEO_DIFF){
//            factor --;
//        }else if(diff - baseDelay < -SimplePlayer.SyncInfo.MAX_VIDEO_DIFF){
//            factor ++;
//        }

        if(factor < -baseDelay) factor = -baseDelay;
//        if(factor > baseDelay) factor = baseDelay;
    }

    protected void render() throws InterruptedException {
        if(imageCache != null) {
            Frame frame;

            frame = imageCache.poll();
            long time = System.currentTimeMillis();
            if(frame == null){
                if(!syncInfo.isDecodeFinished())
                    return;
                else {
                    syncInfo.setPause(true);
                    return;
                }
            }
            curFrame = frame;
            checkDiff(frame,time);
            time = System.currentTimeMillis();
            System.out.println("av diff:" + frame.timestamp + "," + syncInfo.getRealAudioClock(time) + "," + (syncInfo.getAudioClock()));

            draw(frame);
            Thread.sleep(baseDelay + factor);
            lastTime = time;

//            System.out.println("video delay:" + (factor + baseDelay));

        }
    }

    protected void draw(Frame frame) throws InterruptedException {
        if(playHandler != null){
            playHandler.handle(frame);
        }
    }

    public void setHandler(PlayHandler playHandler){
        this.playHandler = playHandler;
    }

    public void setRun(boolean run) {
    }

    public Frame getCurFrame() {
        return curFrame;
    }
}