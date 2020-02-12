package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.FrameGrabber;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import org.bytedeco.javacpp.PointerScope;

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
        Frame frame = null;
        try {
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


                frame = imageCache.poll();
//          if(syncInfo.isAudioFrameGet)
                render(frame);

                if(frame != null)
                    Utils.cloneFrameDeallocate(frame);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if(frame != null)
                Utils.cloneFrameDeallocate(frame);
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
        long diff = time - (lastTime == -1? time : lastTime);

        long timestamp = frame.timestamp;

        long diff2 = timestamp - syncInfo.getRealAudioClock(time);
        long delay2 = Math.floorDiv(diff2,1000);

        if(diff2 > 0) {
            if (delay2 > IMediaPlayer.SyncInfo.MAX_VIDEO_DIFF) delay2 = IMediaPlayer.SyncInfo.MAX_VIDEO_DIFF;
            sleep(delay2);
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

    protected void render(Frame frame) throws InterruptedException {

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
//            System.out.println("av diff:" + frame.timestamp + "," + syncInfo.getRealAudioClock(time) + "," + (syncInfo.getAudioClock()));

            draw(frame);
            Thread.sleep(baseDelay + factor);
            lastTime = time;

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
