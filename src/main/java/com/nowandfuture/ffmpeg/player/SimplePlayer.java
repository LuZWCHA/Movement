package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.FrameGrabber;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import org.bytedeco.ffmpeg.global.avutil;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimplePlayer implements IMediaPlayer{
    private FFmpegFrameGrabber grabber;

    private BlockingQueue<Frame> imageCache;
    private BlockingQueue<Frame> audioCache;

    private DecodeThread decodeThread;
    private DisplayThread displayThread;
    private AudioPlayThread audioPlayThread;
    private final IMediaPlayer.SyncInfo syncInfo;

    public SimplePlayer(){
        imageCache = new LinkedBlockingQueue<>(100);
        audioCache = new LinkedBlockingQueue<>(100);
        syncInfo = new IMediaPlayer.SyncInfo();
    }

    @Override
    public void prepare(PlayHandler videoHandler, PlayHandler audioHandler){
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
            displayThread.setImageCache(null);
            displayThread.interrupt();
        }

        if(audioPlayThread != null && audioPlayThread.isAlive()){
            audioPlayThread.setAudioCache(null);
            audioPlayThread.interrupt();
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
        if(grabber == null) {
            cleanup();
            try {
                grabber = FFmpegFrameGrabber.createDefault(url);
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
                return false;
            }
            grabber.setVideoOption("threads", "0");
            grabber.setAudioOption("threads", "0");
            grabber.setAudioChannels(2);
            decodeThread.setGrabber(grabber);
        }else{
            try {
                grabber.release();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
                return false;
            }
            decodeThread.setGrabber(null);
            grabber = null;
            cleanup();
        }

        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
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

        audioPlayThread.interrupt();
        displayThread.interrupt();
        decodeThread.interrupt();

        if(grabber != null){
            grabber.close();
            grabber.release();
        }
        cleanup();
    }

    private void cleanup(){
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

    public Frame getCurImageFrame(){
        if(displayThread == null || !displayThread.isAlive())
            return null;
        return displayThread.getCurFrame();
    }

    @Override
    public long getTotalTime(){
        return grabber.getFormatContext() == null ? 0:grabber.getLengthInTime() / avutil.AV_TIME_BASE;
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
    public void getInfo(Map<String, String> metadata) {

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
