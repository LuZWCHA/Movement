package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import org.bytedeco.ffmpeg.global.avutil;

import java.util.concurrent.BlockingQueue;

public class AudioPlayThread extends Thread {

    private BlockingQueue<Frame> audioCache;
    private FFmpegFrameGrabber grabber;

    private PlayHandler.SoundPlayHandler playHandler;
    private long baseDelay;

    float vol = 1;

    private long factor = 0;

    private final IMediaPlayer.SyncInfo syncInfo;

    AudioPlayThread(IMediaPlayer.SyncInfo syncInfo){
        this.syncInfo = syncInfo;
    }

    @Override
    public void run() {

        init();
        Frame frame = null;

            try {

                while (!isInterrupted()) {

                    synchronized (syncInfo) {

                        if (syncInfo.isPause()) {
                            syncInfo.wait();
                        }
                    }

//                if(!grabber.hasAudio()){
//                    long timestamp = System.currentTimeMillis() * avutil.AV_TIME_BASE / 1000;
//
//                    if(syncInfo.sysStartTime == -1) {
//                        syncInfo.sysStartTime = System.currentTimeMillis() - timestamp * 1000 / avutil.AV_TIME_BASE;
//                    }
//                    long time  = (System.currentTimeMillis() - syncInfo.sysStartTime) * avutil.AV_TIME_BASE / 1000;
//                    setting(time);
//                    sleep(baseDelay);
//                    continue;
//                }

                    frame = audioCache.poll();
                    if (frame == null) {

                        if (!syncInfo.isDecodeFinished()) {
//                        sleep(baseDelay);
                        } else {
                            syncInfo.setPause(true);
                        }
                        continue;
                    }

                    long timestamp = frame.timestamp;

                    if (syncInfo.sysStartTime == -1) {
                        syncInfo.sysStartTime = System.currentTimeMillis() - timestamp * 1000 / avutil.AV_TIME_BASE;
                    }

                    long time = (System.currentTimeMillis() - syncInfo.sysStartTime) * avutil.AV_TIME_BASE / 1000;

                    if (timestamp > time) {
                        if (timestamp - time > baseDelay * 1000 / avutil.AV_TIME_BASE ||
                                timestamp - time > IMediaPlayer.SyncInfo.MAX_VIDEO_DIFF * 1000 / avutil.AV_TIME_BASE) {
                            factor += 2;
                        } else {
                            sleep((timestamp - time) / 1000);
                            factor = -baseDelay;
                        }
                    } else {
                        factor = -baseDelay;
                    }

                    if (factor < -baseDelay) factor = -baseDelay;
                    if (factor > IMediaPlayer.SyncInfo.MAX_AUDIO_DIFF) factor = IMediaPlayer.SyncInfo.MAX_AUDIO_DIFF;

                    setting(timestamp);
                    play(frame);
                    sleep(baseDelay + factor);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(frame != null)
                    SoundUtils.cloneFrameDeallocate(frame);
            }


        finished();
    }

    protected void init() {
        baseDelay = grabber.hasAudio() ? (avutil.AV_TIME_BASE/ grabber.getSampleRate()) :
                (long) (avutil.AV_TIME_BASE / grabber.getFrameRate());
        if(playHandler!= null){
            playHandler.init(syncInfo);
        }
    }

    protected void finished(){
        if(playHandler!= null){
            playHandler.destroy();
        }
        if(grabber != null)
            grabber = null;
    }

    public void setAudioCache(BlockingQueue<Frame> audioCache) {
        this.audioCache = audioCache;
    }

    protected void play(Frame frame) throws InterruptedException {
        if(playHandler != null){
            playHandler.handle(frame);
        }
    }

    public void setHandler(PlayHandler.SoundPlayHandler audioHandler) {
        if(audioHandler == null && this.playHandler != null) this.playHandler.destroy();
        this.playHandler = audioHandler;
    }

    private void setting(long timestamp){
        syncInfo.setAudioClock(timestamp);;
        syncInfo.setLastTime(System.currentTimeMillis());
    }

    public void setVol(float vol){
        this.vol = vol;
        if(playHandler != null)
            playHandler.setVolume(vol);
    }

    public float getVol() {
        return vol;
    }

    public void setGrabber(FFmpegFrameGrabber fg) {
        this.grabber = fg;
    }

}
