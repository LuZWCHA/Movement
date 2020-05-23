package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.sound.SoundManager;
import com.nowandfuture.ffmpeg.player.sound.SoundSource;
import org.lwjgl.util.vector.Vector3f;

import javax.sound.sampled.AudioFormat;

//openal has shufftering when playing audio
public class OpenALSoundHandler implements PlayHandler.SoundPlayHandler {
    protected SoundManager soundManager;
    protected SimplePlayer simplePlayer;
    protected int sampleFormat;
    protected float sampleRate;
    protected int audioChannels;
    private long lastTime = 0;
    private IMediaPlayer.SyncInfo syncInfo;
    protected String name;
    protected Vector3f pos;
    protected int OpenAlQueueMaxSize = 20;

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
//        try {
//            soundManager.init();
//        } catch (SoundSystemException | LWJGLException e) {
//            e.printStackTrace();
//        }
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

        byte[] mono = SoundUtils.getAudio(frame.samples,1f,sampleFormat);

        AudioFormat format = SoundUtils.getAudioFormat(sampleFormat, sampleRate, audioChannels, sampleRate);

        //+1 is unneccessary, but is not wrong! when a piece of sound is playing, the hardware may play the Nth byte
        //in the array,so the operation of +1 is just believe the sound played by your machine will take more time than
        //the calculated result.
        //more queued bytes mean greater delay of time.
        int queued = soundManager.checkQueued(name) + 1;

        //because of the cache of OpenAL Queue, current byte array that is playing by sound card is not this
        //frame's sound bytes,we need to calculate the "offset"(the delay of video to aligned with sound)
        //[mono.length / sampleRate] means the time of duration of a piece of the sound(one sound byte array -> mono[])
        simplePlayer.getSyncInfo().offset = - (long) (queued * mono.length / sampleRate) * 1000;
        //clear processed arrays to make room for the next sound pieces
        soundManager.flushProcessed(name);

        //discard this frame if the queue of sound arrays is too long
        if(queued > OpenAlQueueMaxSize) return;
        //input new sound bytes
        soundManager.feedRawData(name, mono, format);

        lastTime = System.currentTimeMillis();
    }

    @Override
    public void flush() {
        soundManager.flush(name);
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

    @Override
    public void setVolume(float volume) {
        if(soundManager != null) {
            SoundSource soundSource = soundManager.getSoundSource(name);
            if(soundSource != null){
                soundSource.setGain(volume);
            }
        }
    }
}
