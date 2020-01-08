package com.nowandfuture.ffmpeg;

import com.nowandfuture.ffmpeg.player.PlayHandler;

import java.util.Map;

public interface IMediaPlayer {
    void prepare(PlayHandler videoHandler, PlayHandler audioHandler);

    boolean touchSource(String url) throws FrameGrabber.Exception;

    void play() throws FrameGrabber.Exception;

    void end() throws Exception;

    void pause();

    void resume();

    long getTotalTime();

    boolean seekTo(long time);

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

        public long offset;

        private long audioClock;
        private long lastTime;
        public long sysStartTime;
        private boolean isPause;
        private boolean decodeFinished;

        public static long MAX_AUDIO_DIFF = 10;
        public static long MAX_VIDEO_DIFF = 100;

        public long getRealAudioClock(long curSysTime) {
            return lastTime == -1 ? 0 : (audioClock + ((curSysTime - lastTime)) * 1000 + offset);
        }

        public void end(){
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

        public synchronized long getAudioClock() {
            return audioClock;
        }

        public synchronized void setLastTime(long lastTime) {
            this.lastTime = lastTime;
        }

        public synchronized long getLastTime() {
            return lastTime;
        }

        public synchronized boolean isPause() {
            return isPause;
        }

        public synchronized void setPause(boolean pause) {
            isPause = pause;
        }

        public synchronized boolean isDecodeFinished() {
            return decodeFinished;
        }

        public synchronized void setDecodeFinished(boolean decodeFinished) {
            this.decodeFinished = decodeFinished;
        }
    }
}
