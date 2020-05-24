package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.FrameGrabber;
import com.nowandfuture.ffmpeg.IMediaPlayer;

import java.util.concurrent.BlockingQueue;

public class DecodeThread extends Thread {

    private FFmpegFrameGrabber grabber;
    private BlockingQueue<Frame> imageCache;
    private BlockingQueue<Frame> audioCache;

    private long curFrameTimestamp;

    private boolean seek;
    private long nextTimestamp;

    private final IMediaPlayer.SyncInfo syncInfo;

    DecodeThread(IMediaPlayer.SyncInfo syncInfo){
        this.syncInfo = syncInfo;
    }

    public FFmpegFrameGrabber getGrabber() {
        return grabber;
    }

    public void setGrabber(FFmpegFrameGrabber grabber) {
        this.grabber = grabber;
    }

    @Override
    public void run() {
        Frame frame = null;
        try {
            double fps = grabber.getFrameRate();

            long delay = (long) (1000 / fps);

            while (!isInterrupted()){

                if(seek){
                    imageCache.clear();
                    audioCache.clear();
                    grabber.setTimestamp(nextTimestamp);
                    seek =false;
                }

                synchronized (syncInfo) {
                    while (syncInfo.isPause()) {
                        syncInfo.wait();
                    }
                }

                frame = grabber.grab();

                if(grabber.getFormatContext() == null || grabber.getFormatContext().isNull())
                    break;

                if(frame == null){
                    if(grabber.getTimestamp() < grabber.getLengthInTime()) {
                        if(syncInfo.isDecodeFinished()) {
                            syncInfo.setDecodeFinished(false);
                            break;
                        }
                        continue;
                    }
                    else{
                        syncInfo.setDecodeFinished(true);
                        syncInfo.setPause(true);
                        continue;
                    }
                }else{
                    frame = frame.clone();
                }

                if(grabber.hasVideo() && frame.image != null) {
                    if(!syncInfo.isVideoFrameGet){
                        syncInfo.isVideoFrameGet = true;
                    }
                    imageCache.put(frame);
                    curFrameTimestamp = frame.timestamp;
                    if(!grabber.hasAudio()){
                        Frame fakeAudioFrame = new Frame();
                        fakeAudioFrame.timestamp = curFrameTimestamp;
                        audioCache.put(fakeAudioFrame);
                    }
                }else if(grabber.hasAudio()&& frame.samples != null) {
                    if(!syncInfo.isAudioFrameGet){
                        syncInfo.isAudioFrameGet = true;
                    }
                    audioCache.put(frame);
                    curFrameTimestamp = frame.timestamp;
                    if(!grabber.hasVideo()){
                        Frame fakeVideoFrame = new Frame();
                        fakeVideoFrame.timestamp = curFrameTimestamp;
                        fakeVideoFrame.samples = frame.samples.clone();
                        fakeVideoFrame.image = null;
                        imageCache.put(fakeVideoFrame);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(frame != null)
                    SoundUtils.cloneFrameDeallocate(frame);

                if(grabber != null) {
                    grabber.stop();
                    grabber.release();
                    syncInfo.setDecodeFinished(true);
                }

            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void setImageCache(BlockingQueue<Frame> imageCache) {
        this.imageCache = imageCache;
    }

    public void setAudioCache(BlockingQueue<Frame> audioCache) {
        this.audioCache = audioCache;
    }

    public synchronized void setTimestamp(long timestamp){
        seek = true;
        nextTimestamp = timestamp;
    }
}
