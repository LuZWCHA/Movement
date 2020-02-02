package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.sound.SoundManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.util.vector.Vector3f;
import paulscode.sound.*;
import paulscode.sound.libraries.ChannelJavaSound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class OpenALSoundHandler implements PlayHandler {
    SoundManager soundManager;
    SimplePlayer simplePlayer;
    int sampleFormat;
    int sampleRate;
    int audioChannel;
    long lastTime = 0;
    private SoundSystem soundSystem = null;
    private IMediaPlayer.SyncInfo syncInfo;
    private int hasT;
    long factor = 0;
    boolean isFirstFrame;
    private ChannelJavaSound channelJavaSound;
    private SourceDataLine dataLine;
    private Mixer mixer;

    public OpenALSoundHandler(SimplePlayer player){
        simplePlayer = player;
        isFirstFrame = true;
        soundManager = new SoundManager();
        try {
//            soundSystem = new SoundSystem();
//            soundSystem.switchLibrary(LibraryLWJGLOpenAL.class);
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
//        soundSystem.rawDataStream(Utils.getAudioFormat(sampleFormat,sampleRate,audioChannel, sampleRate),true,"music",0,0,0,1,SoundSystemConfig.getDefaultRolloff());;

        soundManager.addStream("audio",new Vector3f(),Utils.getAudioFormat(sampleFormat,sampleRate,audioChannel,sampleRate));
    }

    @Override
    public void handle(Frame frame) throws InterruptedException {

        sampleRate = frame.sampleRate;
        audioChannel = frame.audioChannels;

        byte[] buffer = Utils.getAudio(frame.samples,simplePlayer.getVolume(),sampleFormat);
        AudioFormat format = Utils.getAudioFormat(sampleFormat,sampleRate,audioChannel,sampleRate);

        if(soundManager.getSoundSource("audio").isPlaying()){
            soundManager.flushProcessed("audio");
        }
        soundManager.feedRawData("audio",buffer,format);


        System.out.println("audio true delay:"+(System.currentTimeMillis() - lastTime));
        lastTime = System.currentTimeMillis();
    }

    @Override
    public void flush() {

        soundManager.flushProcessed("audio");

//        soundSystem.interruptCommandThread();
//        soundSystem.flush("music");
    }


    @Override
    public void destroy() {
//        soundSystem.cleanup();
        soundManager.cleanup();
    }

    @Override
    public Object getFrameObj() {
        return null;
    }
}
