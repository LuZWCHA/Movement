package com.nowandfuture.ffmpeg.player.sound;


import com.nowandfuture.ffmpeg.player.Utils;
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
        // 复制到缓冲区
        int format = Utils.getOpenALFormat(audioFormat);
        alBufferData(bufferId, format,byteBuffer, (int) audioFormat.getSampleRate());
        isStream = false;
    }

    public SoundBuffer(AudioFormat audioFormat) {
        this.bufferId = AL10.alGenBuffers();
        // 复制到缓冲区
        this.audioFormat = audioFormat;

        int format = Utils.getOpenALFormat(audioFormat);
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