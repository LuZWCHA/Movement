package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.FrameGrabber;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import org.bytedeco.ffmpeg.global.avutil;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_S16;

public class SimplePlayer implements IMediaPlayer{
    private Exception lastException;
    private FFmpegFrameGrabber grabber;
    private int channels;

    private BlockingQueue<Frame> imageCache;
    private BlockingQueue<Frame> audioCache;

    private DecodeThread decodeThread;
    private DisplayThread displayThread;
    private AudioPlayThread audioPlayThread;
    private final IMediaPlayer.SyncInfo syncInfo;

    private PlayHandler.DisplayHandler videoHandler;
    private PlayHandler.SoundPlayHandler audioHandler;

    private volatile boolean isLoading;
    private float volume;

    public SimplePlayer(){
        imageCache = new LinkedBlockingQueue<>(100);
        audioCache = new LinkedBlockingQueue<>(100);
        syncInfo = new IMediaPlayer.SyncInfo();
        channels = 1;
        volume = 1;
    }

    public void setHandlers(PlayHandler.DisplayHandler videoHandler, PlayHandler.SoundPlayHandler audioHandler){
        this.videoHandler = videoHandler;
        this.audioHandler = audioHandler;
    }

    @Override
    public void prepare(){
        if(syncInfo.isPause()){
            syncInfo.setPause(false);
        }
        syncInfo.setDecodeFinished(false);
        //check threads
        if(decodeThread != null && decodeThread.isAlive()){
            decodeThread.setAudioCache(null);
            decodeThread.setImageCache(null);
            decodeThread.interrupt();
        }

        if(displayThread != null && displayThread.isAlive()){
            displayThread.interrupt();
            displayThread.setImageCache(null);
        }

        if(audioPlayThread != null && audioPlayThread.isAlive()){
            audioPlayThread.interrupt();
            audioPlayThread.setAudioCache(null);
        }

        decodeThread = new DecodeThread(syncInfo);
        displayThread = new DisplayThread(syncInfo);
        audioPlayThread = new AudioPlayThread(syncInfo);

        displayThread.setImageCache(imageCache);
        audioPlayThread.setAudioCache(audioCache);
        decodeThread.setImageCache(imageCache);
        decodeThread.setAudioCache(audioCache);

        audioPlayThread.setVol(volume);

        displayThread.setHandler(videoHandler);
        audioPlayThread.setHandler(audioHandler);
    }

    public Exception getLastException() {
        return lastException;
    }

    public boolean touchSource(String url) {

        lastException = null;
        //check grabber
        if(grabber != null) {
            try {
                end();
            } catch (Exception e) {
                e.printStackTrace();
                lastException = e;
            }
            grabber = null;
            prepare();
        }
        cleanup();

        isLoading = true;
        try {
            grabber = FFmpegFrameGrabber.createDefault(url);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            lastException = e;
            isLoading = false;
        }

        if(lastException != null) return false;

        grabber.setVideoOption("threads", "0");
        grabber.setAudioOption("threads", "0");
        grabber.setOption("hwaccel", "videotoolbox");
        grabber.setSampleFormat(AV_SAMPLE_FMT_S16);
        grabber.setAudioChannels(channels);

        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            lastException = e;
        }finally {
            isLoading = false;
        }

        if(lastException != null) return false;

        info(grabber);
        updateTotalTime(getTotalTime());
        return true;
    }

    @Override
    public void play() throws FrameGrabber.Exception {

        syncInfo.setDecodeFinished(false);
        resume();

        decodeThread.setGrabber(grabber);
        decodeThread.start();

        audioPlayThread.setGrabber(grabber);
        audioPlayThread.start();

//        if(grabber.getLengthInVideoFrames() == 1)
//            displayThread.setBaseDelay((long) (1000d / grabber.getVideoFrameRate()));
//        else
        displayThread.setBaseDelay(!grabber.hasVideo()? (long)(1000d / grabber.getAudioFrameRate()) : (long) (1000d / grabber.getVideoFrameRate()));

//        displayThread.setBaseDelay((long) (1000d / grabber.getVideoFrameRate()));
        displayThread.start();
    }

    public void info(FFmpegFrameGrabber grabber){
        grabber.getMetadata();
    }

    @Override
    public void end() throws Exception {
        syncInfo.setDecodeFinished(true);
        resume();

        if(audioPlayThread != null) {
            audioPlayThread.interrupt();
            audioPlayThread.setHandler(null);
        }
        if(displayThread != null) {
            displayThread.interrupt();
            displayThread.setHandler(null);
        }
        if(decodeThread != null) {
            decodeThread.interrupt();
            decodeThread.setImageCache(null);
            decodeThread.setAudioCache(null);
        }

        cleanup();

        syncInfo.setDecodeFinished(false);
        syncInfo.setAudioClock(0);
        syncInfo.sysStartTime = -1;

        isLoading = false;
    }

    private void cleanup(){
        for (Frame frame:
                imageCache){
            SoundUtils.cloneFrameDeallocate(frame);
        }

        for (Frame frame:
                audioCache){
            SoundUtils.cloneFrameDeallocate(frame);
        }

        imageCache.clear();
        audioCache.clear();
    }

    public void seekToTimestamp(long ts) throws FrameGrabber.Exception {
        if(grabber.getFormatContext() != null && ts <= grabber.getLengthInTime() && ts >= 0) {
            decodeThread.setTimestamp(ts);
            syncInfo.sysStartTime = -1;
        }
    }

    public void setOffsetOfAV(long offset){
        syncInfo.offset = offset;
    }

    public void setVolume(float volume){
        this.volume = volume;
        if(audioPlayThread != null)
            audioPlayThread.setVol(volume);
    }

    public float getVolume(){
        return volume;
    }

    @Override
    public void pause(){
        syncInfo.setPause(true);
    }

    @Override
    public void resume(){
        synchronized (syncInfo) {
            if(syncInfo.isPause()) {
                syncInfo.setDecodeFinished(false);
                syncInfo.setPause(false);
                syncInfo.notifyAll();
                syncInfo.sysStartTime = -1;
            }
        }
    }

    public void setChannels(int channels){
        this.channels = channels;
        if(grabber != null){
            grabber.setAudioChannels(channels);
        }
    }

    public int getChannels() {
        return channels;
    }

    public Object getCurImageObj(){
        if(displayThread == null || !displayThread.isAlive() || displayThread.getPlayHandler() == null)
            return null;
        return displayThread.getPlayHandler().getFrameObj();
    }

    @Override
    public long getTotalTime(){
        return (grabber == null || grabber.getFormatContext() == null) ? 0:grabber.getLengthInTime() / avutil.AV_TIME_BASE;
    }

    public BlockingQueue<Frame> getAudioCache() {
        return audioCache;
    }

    public BlockingQueue<Frame> getImageCache() {
        return imageCache;
    }

    @Override
    public boolean seekTo(long time){
        try {
            seekToTimestamp(time * avutil.AV_TIME_BASE);
            return true;
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            lastException = e;
        }
        return false;
    }

    @Override
    public int getWidth(){
        if(grabber != null){
            grabber.getImageWidth();
        }
        return -1;
    }

    @Override
    public int getHeight(){
        if(grabber != null){
            grabber.getImageHeight();
        }
        return -1;
    }

    @Override
    public boolean isLoading(){
        return isLoading;
    }

    @Override
    public void getInfo(Map<String, String> metadata) {
        if(grabber != null){
            grabber.getMetadata();
        }
    }

    @Override
    public void updateTotalTime(long time) {

    }

    public FFmpegFrameGrabber getGrabber() {
        return grabber;
    }

    @Override
    public SyncInfo getSyncInfo() {
        return syncInfo;
    }

}
