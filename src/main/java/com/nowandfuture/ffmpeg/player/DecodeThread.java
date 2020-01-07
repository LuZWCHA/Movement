package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.FrameGrabber;
import com.nowandfuture.ffmpeg.IMediaPlayer;

import java.util.concurrent.*;

public class DecodeThread extends Thread {

    private FFmpegFrameGrabber grabber;
    private BlockingQueue<Frame> imageCache;
    private BlockingQueue<Frame> audioCache;

    private long curFrameTimestamp;

    private boolean seek;
    private long nextTimestamp;

    private long delay = 1;

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
        try {
            double fps = grabber.getFrameRate();

            long delay = (long) (1000 / fps);
            Frame frame;
            while (!isInterrupted()){

                if(seek){
                    imageCache.clear();
                    audioCache.clear();
                    grabber.setTimestamp(nextTimestamp);
                    seek =false;
                }

                synchronized (syncInfo) {
                    if (syncInfo.isPause()) {
                        syncInfo.wait();
                    }
                }

                frame = grabber.grabFrame(true, true, true, false);

                if(grabber.getFormatContext() == null || grabber.getFormatContext().isNull())
                    break;

                if(frame == null){
                    if(grabber.getTimestamp() < grabber.getLengthInTime()) {
                        if(syncInfo.isDecodeFinished())
                            syncInfo.setDecodeFinished(false);
                        break;
                    }
                    else{
                        syncInfo.setDecodeFinished(true);
                        syncInfo.setPause(true);
                        continue;
                    }
                }

                if(grabber.hasVideo() && frame.image != null) {
                    if(!syncInfo.isVideoFrameGet){
                        syncInfo.isVideoFrameGet = true;
                    }
                    imageCache.put(frame.clone());
                }else if(grabber.hasAudio()&& frame.samples != null) {
                    System.out.println("sample frame" + frame.timestamp);
                    audioCache.put(frame.clone());
                    curFrameTimestamp = frame.timestamp;
                }


//                System.out.println("--------------------------------");
//                System.out.println(audioCache.size());
//                System.out.println(imageCache.size());
//                if(audioCache.remainingCapacity() <= 0 || imageCache.remainingCapacity() <= 0){
//                    sleep(delay++);
//                }else{
//                    delay --;
//                    if(delay < 0) delay = 0;
//                }
            }
        } catch (FrameGrabber.Exception | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                grabber.stop();
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
