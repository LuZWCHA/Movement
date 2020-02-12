package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.FrameGrabber;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.Pointer;

import java.nio.Buffer;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_S16;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_S16P;

public class SimplePlayer implements IMediaPlayer{
    private FFmpegFrameGrabber grabber;

    private BlockingQueue<Frame> imageCache;
    private BlockingQueue<Frame> audioCache;

    private DecodeThread decodeThread;
    private DisplayThread displayThread;
    private AudioPlayThread audioPlayThread;
    private final IMediaPlayer.SyncInfo syncInfo;

    private PlayHandler videoHandler;
    private PlayHandler audioHandler;

    private boolean isLoading;

    public SimplePlayer(){
        imageCache = new LinkedBlockingQueue<>(100);
        audioCache = new LinkedBlockingQueue<>(100);
        syncInfo = new IMediaPlayer.SyncInfo();
    }

    public void setHandlers(PlayHandler videoHandler, PlayHandler audioHandler){
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

        displayThread.setHandler(videoHandler);
        audioPlayThread.setHandler(audioHandler);
    }

    public boolean touchSource(String url) {
        //check grabber
        if(grabber != null) {
            try {
                end();
            } catch (Exception e) {
                e.printStackTrace();
            }
            grabber = null;
            prepare();
        }
        cleanup();

        try {
            isLoading = true;
            grabber = FFmpegFrameGrabber.createDefault(url);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            isLoading = false;
            return false;
        }
        grabber.setVideoOption("threads", "0");
        grabber.setAudioOption("threads", "0");
        grabber.setOption("hwaccel", "videotoolbox");
        grabber.setAudioChannels(1);
        grabber.setSampleFormat(AV_SAMPLE_FMT_S16);

        try {
            grabber.start();
            isLoading = false;
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            isLoading = false;
            return false;
        }
        info(grabber);
        updateTotalTime(getTotalTime());
        return true;
    }

    @Override
    public void play() throws FrameGrabber.Exception {

        decodeThread.setGrabber(grabber);
        decodeThread.start();

        audioPlayThread.setGrabber(grabber);
        audioPlayThread.start();

        displayThread.setBaseDelay((long) (1000d / grabber.getVideoFrameRate()));
        displayThread.start();
    }

    public void info(FFmpegFrameGrabber grabber){
        grabber.getMetadata();
    }

    @Override
    public void end() throws Exception {
        if(syncInfo.isPause()){
            syncInfo.setPause(false);
        }

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

        if(isLoading){
            isLoading = false;
        }
    }

    private void cleanup(){
        for (Frame frame:
                imageCache){
            Utils.cloneFrameDeallocate(frame);
        }

        for (Frame frame:
                audioCache){
            Utils.cloneFrameDeallocate(frame);
        }

        imageCache.clear();
        audioCache.clear();

        System.runFinalization();
        System.gc();
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
        audioPlayThread.setVol(volume);
    }

    public float getVolume(){
        return audioPlayThread.getVol();
    }

    @Override
    public void pause(){
        syncInfo.setPause(true);
    }

    @Override
    public void resume(){
        syncInfo.setPause(false);
        synchronized (syncInfo) {
            syncInfo.notifyAll();
            syncInfo.sysStartTime = -1;
        }
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
