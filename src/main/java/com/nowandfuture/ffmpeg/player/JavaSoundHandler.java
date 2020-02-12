package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.sun.media.sound.SoftMixingDataLine;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemLogger;
import paulscode.sound.libraries.ChannelJavaSound;

import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class JavaSoundHandler implements PlayHandler {
    SourceDataLine dataLine;
    Mixer mixer;
    SimplePlayer simplePlayer;
    int sampleFormat;
    ChannelJavaSound channelJavaSound;

    public JavaSoundHandler(IMediaPlayer player){
        simplePlayer = (SimplePlayer) player;
    }

    @Override
    public void init(IMediaPlayer.SyncInfo info) {

        FFmpegFrameGrabber grabber = simplePlayer.getGrabber();

        dataLine = (SourceDataLine) Utils.init(grabber.getSampleFormat(),grabber.getSampleRate(), (float) grabber.getSampleRate(),grabber.getAudioChannels(),false);
        mixer = (Mixer) Utils.init(grabber.getSampleFormat(),grabber.getSampleRate(), (float) grabber.getSampleRate(),grabber.getAudioChannels(),true);

        sampleFormat = grabber.getSampleFormat();
        SoundSystemConfig.setLogger(new SoundSystemLogger());
        channelJavaSound = new ChannelJavaSound(1,mixer);
        channelJavaSound.resetStream(Utils.getAudioFormat(sampleFormat,grabber.getSampleRate(),grabber.getAudioChannels(), (float) grabber.getSampleRate()));
    }

    @Override
    public void handle(Frame frame) {
        if(channelJavaSound != null && channelJavaSound.sourceDataLine != null)
            channelJavaSound.queueBuffer(Utils.getAudio(frame.samples,updateVolume(),sampleFormat));
    }

    protected float updateVolume(){
        float v = simplePlayer.getVolume();
        return v;
    }

    protected void setGain(float gain){
        if(channelJavaSound != null && channelJavaSound.sourceDataLine.isOpen())
            channelJavaSound.setGain(gain);
    }

    @Override
    public void flush() {
        if(channelJavaSound != null && channelJavaSound.sourceDataLine != null)
            channelJavaSound.flush();
    }

    @Override
    public void destroy() {
        try {
            if(channelJavaSound != null) {
                if(channelJavaSound.sourceDataLine != null){
                    channelJavaSound.sourceDataLine.flush();

                    if(channelJavaSound.sourceDataLine.isOpen())
                        channelJavaSound.sourceDataLine.close();
                }
                channelJavaSound.cleanup();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public Object getFrameObj() {
        return null;
    }
}
