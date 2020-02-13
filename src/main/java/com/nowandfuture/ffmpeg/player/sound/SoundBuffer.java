package com.nowandfuture.ffmpeg.player.sound;


import com.nowandfuture.ffmpeg.player.SoundUtils;
import org.lwjgl.openal.AL10;

import javax.sound.sampled.AudioFormat;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import static org.lwjgl.openal.AL10.*;

public class SoundBuffer {

    private final int bufferId;
    private final AudioFormat audioFormat;
    private boolean isStream;

    public SoundBuffer(Buffer buffer, AudioFormat audioFormat) {
        this.bufferId = AL10.alGenBuffers();
        ByteBuffer byteBuffer = (ByteBuffer)buffer;
        this.audioFormat = audioFormat;
        int format = SoundUtils.getOpenALFormat(audioFormat);
        alBufferData(bufferId, format,byteBuffer, (int) audioFormat.getSampleRate());
        isStream = false;
    }

    public SoundBuffer(AudioFormat audioFormat) {
        this.bufferId = AL10.alGenBuffers();
        this.audioFormat = audioFormat;

        int format = SoundUtils.getOpenALFormat(audioFormat);
        isStream = true;
        //alBufferData(bufferId, format,byteBuffer, (int) audioFormat.getSampleRate());
    }

    public int getBufferId() {
        return this.bufferId;
    }

    public void cleanup() {
        alDeleteBuffers(this.bufferId);
    }

    public boolean isStream() {
        return isStream;
    }
}