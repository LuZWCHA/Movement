package com.nowandfuture.ffmpeg;

import java.util.Map;

public interface IMediaPlayer {
    void prepare();

    boolean touchSource(String url) throws FrameGrabber.Exception;

    void play() throws FrameGrabber.Exception;

    void end() throws Exception;

    void pause();

    void resume();

    long getTotalTime();

    boolean seekTo(long time);

    int getWidth();

    int getHeight();

    boolean isLoading();

    Exception getLastException();

    void getInfo(Map<String,String> metadata);

    void updateTotalTime(long time);

    SyncInfo getSyncInfo();

    class SyncInfo{

        public SyncInfo(){
            sysStartTime = -1;
            audioClock = 0;
            lastTime = -1;
            offset = 0;
            isVideoFrameGet =false;
            isAudioFrameGet =false;
            decodeFinished = false;
            isPause = false;
        }

        public boolean isVideoFrameGet;
        public boolean isAudioFrameGet;

        public volatile long offset;

        private long audioClock;
        private long lastTime;
        public long sysStartTime;
        private boolean isPause;
        private boolean decodeFinished;

        public static volatile long MAX_AUDIO_DIFF = 10;
        public static volatile long MAX_VIDEO_DIFF = 100;

        public long getRealAudioClock(long curSysTime) {
            return lastTime == -1 ? 0 : (audioClock + ((curSysTime - lastTime)) * 1000 + offset);
        }

        public synchronized void end(){
            sysStartTime = -1;
            audioClock = 0;
            lastTime = -1;
            offset = 0;
            isVideoFrameGet =false;
            isAudioFrameGet =false;
            isPause = false;
            decodeFinished = true;
        }

        public synchronized void setAudioClock(long audioClock) {
            this.audioClock = audioClock;
        }

        public long getAudioClock() {
            return audioClock;
        }

        public synchronized void setLastTime(long lastTime) {
            this.lastTime = lastTime;
        }

        public long getLastTime() {
            return lastTime;
        }

        public boolean isPause() {
            return isPause;
        }

        public synchronized void setPause(boolean pause) {
            isPause = pause;
        }

        public boolean isDecodeFinished() {
            return decodeFinished;
        }

        public synchronized void setDecodeFinished(boolean decodeFinished) {
            this.decodeFinished = decodeFinished;
        }
    }
}
