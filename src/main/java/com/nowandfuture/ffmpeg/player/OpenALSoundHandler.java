package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.sound.SoundManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.util.vector.Vector3f;
import paulscode.sound.*;

import javax.sound.sampled.AudioFormat;
import java.util.LinkedList;
import java.util.Queue;

//openal has shufftering when playing audio
public class OpenALSoundHandler implements PlayHandler {
    protected SoundManager soundManager;
    protected SimplePlayer simplePlayer;
    protected int sampleFormat;
    protected float sampleRate;
    protected int audioChannels;
    long lastTime = 0;
    private IMediaPlayer.SyncInfo syncInfo;
    protected String name;
    protected Vector3f pos;
    private Queue<byte[]> cache;
    private int processed = 0;

    public OpenALSoundHandler(){
        soundManager = new SoundManager();
        cache = new LinkedList<>();
        name = "audio";
        pos = new Vector3f();
        initSoundManager();
    }

    public OpenALSoundHandler(SimplePlayer player){
        this();
        simplePlayer = player;
    }

    protected void initSoundManager(){
        try {
            soundManager.init();
        } catch (SoundSystemException | LWJGLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(IMediaPlayer.SyncInfo info) {

        this.syncInfo = info;
        FFmpegFrameGrabber grabber = simplePlayer.getGrabber();
        sampleFormat = grabber.getSampleFormat();
        sampleRate = grabber.getSampleRate();
        audioChannels = grabber.getAudioChannels();

        soundManager.addStream(name,pos);
    }

    @Override
    public void handle(Frame frame) throws InterruptedException {
        sampleRate = frame.sampleRate;
        audioChannels = frame.audioChannels;

//        byte[] buffer = SoundUtils.getAudio(frame.samples,simplePlayer.getVolume(),sampleFormat);
        byte[] mono = SoundUtils.getAudio(frame.samples,simplePlayer.getVolume(),sampleFormat);
        AudioFormat format = SoundUtils.getAudioFormat(sampleFormat,sampleRate, audioChannels,sampleRate);

        processed += soundManager.checkProcessed(name);
        int queue = soundManager.checkQueued(name) + 1;

        simplePlayer.getSyncInfo().offset = - (long) (queue * mono.length / sampleRate) * 1000;
        soundManager.flushProcessed(name);

        //discard this frame
        if(queue > 20) {return;}
        soundManager.feedRawData(name,mono,format);

        lastTime = System.currentTimeMillis();
    }

    @Override
    public void flush() {
        soundManager.flush(name);
    }


    @Override
    public void destroy() {
        soundManager.stop(name);
        flush();
        soundManager.cleanup();
    }

    @Override
    public Object getFrameObj() {
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPos(Vector3f pos) {
        this.pos = pos;
    }
}
