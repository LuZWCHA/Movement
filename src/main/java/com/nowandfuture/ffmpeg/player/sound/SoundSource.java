package com.nowandfuture.ffmpeg.player.sound;

import com.nowandfuture.ffmpeg.player.Utils;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.vector.Vector3f;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.AL_STREAMING;

public class SoundSource {

    private static final int MAX_BUFFERS = 100;
    private final int sourceId;
    private int gain;
    private int sourceVolume;

    public SoundSource(boolean loop, boolean relative,int type) {
        this.sourceId = alGenSources();
        if (loop) {
            alSourcei(sourceId, AL_LOOPING, AL_TRUE);
        }
        if (relative) {
            alSourcei(sourceId, AL_SOURCE_RELATIVE, AL_TRUE);
        }

        alSourcef(sourceId, AL_GAIN,2);
        alSourcef( sourceId, AL_ROLLOFF_FACTOR, 1 );
        alSourcef(sourceId,AL_INVERSE_DISTANCE_CLAMPED ,6);
        alSourcef(sourceId, AL_SOURCE_TYPE, type);
    }

    public void setBuffer(int bufferId) {
//        stop();
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
        alDeleteSources(sourceId);
    }

    public int getSourceId() {
        return sourceId;
    }
}