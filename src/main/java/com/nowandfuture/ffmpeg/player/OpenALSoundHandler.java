package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.sound.SoundManager;
import com.nowandfuture.ffmpeg.player.sound.SoundSource;
import org.lwjgl.LWJGLException;
import org.lwjgl.util.vector.Vector3f;
import paulscode.sound.*;
import paulscode.sound.libraries.ChannelJavaSound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;

public class OpenALSoundHandler implements PlayHandler {
    protected SoundManager soundManager;
    protected SimplePlayer simplePlayer;
    protected int sampleFormat;
    protected float sampleRate;
    protected int audioChannel;
    long lastTime = 0;
    private IMediaPlayer.SyncInfo syncInfo;
    protected String name;
    protected Vector3f pos;
    private byte[] cache;

    public OpenALSoundHandler(){
        soundManager = new SoundManager();
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
        audioChannel = grabber.getAudioChannels();

        soundManager.addStream(name,pos);
    }

    @Override
    public void handle(Frame frame) throws InterruptedException {
        sampleRate = frame.sampleRate;
        audioChannel = frame.audioChannels;

        byte[] buffer = Utils.getAudio(frame.samples,simplePlayer.getVolume(),sampleFormat);
        AudioFormat format = Utils.getAudioFormat(sampleFormat,sampleRate,audioChannel,sampleRate);

        SoundSource soundSource = soundManager.getSoundSource(name);
        if(soundSource != null && soundSource.isPlaying()){
            soundManager.flushProcessed(name);
        }
        if(buffer.length > 0)
            soundManager.feedRawData(name,buffer,format);


        lastTime = System.currentTimeMillis();
    }

    @Override
    public void flush() {
        soundManager.flushProcessed(name);
    }


    @Override
    public void destroy() {
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
