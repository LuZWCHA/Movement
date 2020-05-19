package com.nowandfuture.ffmpeg.player.sound;

import org.lwjgl.openal.AL10;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class SoundSource {

    private static final int MAX_BUFFERS = 100;
    private final int sourceId;
    private int gain;
    private int sourceVolume;

    public SoundSource(boolean loop, boolean relative) {
        this.sourceId = alGenSources();
        if (loop) {
            alSourcei(sourceId, AL_LOOPING, AL_TRUE);
        }
        if (relative) {
            alSourcei(sourceId, AL_SOURCE_RELATIVE, AL_TRUE);
        }
    }

    public void setBuffer(int bufferId) {
        alSourcei(sourceId, AL_BUFFER, bufferId);
    }

    public void setPosition(Vector3f position) {
        alSource3f(sourceId, AL_POSITION, position.x, position.y, position.z);
    }

    public void setSpeed(Vector3f speed) {
        alSource3f(sourceId, AL_VELOCITY, speed.x, speed.y, speed.z);
    }

    public void setGain(float gain) {
        alSourcef(sourceId, AL_GAIN, gain);
    }

    public float getMaxGain(){
       return AL10.alGetSourcef(sourceId, AL10.AL_MAX_GAIN);
    }

    public void setProperty(int param, float value) {
        alSourcef(sourceId, param, value);
    }

    public void play() {
        alSourcePlay(sourceId);
    }

    public boolean isPlaying() {
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public void pause() {
        alSourcePause(sourceId);
    }

    public void stop() {
        alSourceStop(sourceId);
    }

    public void cleanup() {
        try
        {
            AL10.alSourceStop(sourceId);
            AL10.alGetError();
        }
        catch( Exception e )
        {}
        try
        {
            // Delete the source:
            AL10.alDeleteSources(sourceId);
            AL10.alGetError();
        }
        catch( Exception e )
        {}
    }

    public int getSourceId() {
        return sourceId;
    }
}