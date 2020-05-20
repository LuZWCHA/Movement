package com.nowandfuture.ffmpeg.player.sound;

import com.nowandfuture.ffmpeg.player.SoundUtils;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.api.Unstable;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import javax.sound.sampled.AudioFormat;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.lwjgl.openal.AL10.*;

public class SoundManager {

    private SoundListener listener;

    private final List<SoundBuffer> soundBufferList;

    private final Map<String, SoundSource> soundSourceMap;

    private final Matrix4f cameraMatrix;

//    private LibraryLWJGLOpenAL libraryLWJGLOpenAL;

    private Logger logger;
    private float millisPreviouslyPlayed = 0;

    public SoundManager() {
        soundBufferList = new ArrayList<>();
        soundSourceMap = new HashMap<>();
        cameraMatrix = new Matrix4f();
        logger = Logger.getLogger(getClass().getSimpleName());
    }

    public void add(String name, Buffer buffer, Vector3f position, AudioFormat af){
        SoundSource soundSource = new SoundSource(false,false);
        soundSource.setPosition(position);
        SoundBuffer soundBuffer = new SoundBuffer(buffer,af);

        soundBufferList.add(soundBuffer);
        soundSource.setBuffer(soundBuffer.getBufferId());
        soundSourceMap.put(name,soundSource);
    }

    public void addStream(String name, Vector3f position){
        SoundSource soundSource = new SoundSource(false,false);
        soundSource.setPosition(position);
        soundSource.setGain(1f);
        soundSourceMap.put(name,soundSource);
    }

    public SoundSource getSoundSource(String name){
        return soundSourceMap.get(name);
    }

    protected void errorMessage(String message) {
        Movement.logger.warn(message);
    }

    private boolean checkALError() {
        switch(AL10.alGetError()) {
            case 0:
                return false;
            case 40961:
                this.errorMessage("Invalid name parameter.");
                return true;
            case 40962:
                this.errorMessage("Invalid parameter.");
                return true;
            case 40963:
                this.errorMessage("Invalid enumerated parameter value.");
                return true;
            case 40964:
                this.errorMessage("Illegal call.");
                return true;
            case 40965:
                this.errorMessage("Unable to allocate memory.");
                return true;
            default:
                this.errorMessage("An unrecognized error occurred.");
                return true;
        }
    }

    public float millisInBuffer(int alBufferi,AudioFormat format) {
        return (float)AL10.alGetBufferi(alBufferi, 8196) / (float)AL10.alGetBufferi(alBufferi, 8195) / ((float)AL10.alGetBufferi(alBufferi, 8194) / 8.0F) / (float)format.getSampleRate() * 1000.0F;
    }

    public float getMillisPreviouslyPlayed() {
        return millisPreviouslyPlayed;
    }

    public int checkProcessed(String name){
        SoundSource soundSource = getSoundSource(name);
        if(soundSource == null) return -1;
        int sourceId = soundSource.getSourceId();
        int processed = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_PROCESSED);
        if(!checkALError()){
            return processed;
        }
        return -1;
    }

    public int checkQueued(String name){
        SoundSource soundSource = getSoundSource(name);
        if(soundSource == null) return -1;
        int sourceId = soundSource.getSourceId();
        int queued = AL10.alGetSourcei(sourceId, AL_BUFFERS_QUEUED);
        if(!checkALError()){
            return queued;
        }
        return -1;
    }

    public int feedRawData(String name, byte[] bytes, AudioFormat af) throws InterruptedException {
        SoundSource soundSource = getSoundSource(name);
        if(soundSource == null) return -1;
        int sourceId = soundSource.getSourceId();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        int processed = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_PROCESSED);
        IntBuffer intBuffer;
        if (processed > 0) {
            intBuffer = BufferUtils.createIntBuffer(1);
            AL10.alGenBuffers(intBuffer);

            if (this.checkALError()) {
                return -1;
            }

            AL10.alSourceUnqueueBuffers(sourceId,intBuffer);

            if (this.checkALError()) {
                return - 1;
            }

            if (AL10.alIsBuffer(intBuffer.get(0))) {
                this.millisPreviouslyPlayed += this.millisInBuffer(intBuffer.get(0),af);
            }

            this.checkALError();
        }else {

            intBuffer = BufferUtils.createIntBuffer(1);
            AL10.alGenBuffers(intBuffer);
            if (this.checkALError()) {
                return -1;
            }
        }

        AL10.alBufferData(intBuffer.get(0), SoundUtils.getOpenALFormat(af), byteBuffer,
                (int) af.getSampleRate());

        if (this.checkALError()) {
            return -1;
        } else {
            AL10.alSourceQueueBuffers(sourceId, intBuffer);
            if (this.checkALError()) {
                return -1;
            } else {
                if (AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) != AL_PLAYING) {
                    AL10.alSourcePlay(sourceId);
                    this.checkALError();
                }

            }
        }

        return processed;
    }

    @Unstable(description = "to be honest, this method has bug! don't use it")
    public boolean queueBuffer(String name, byte[] buffer,IntBuffer intBuffer,AudioFormat format)
    {
       SoundSource soundSource = getSoundSource(name);
       if(soundSource == null) return false;

        ByteBuffer byteBuffer = (ByteBuffer) BufferUtils.createByteBuffer(
                buffer.length ).put( buffer ).flip();

        if(intBuffer == null || intBuffer.capacity() < 1 || !AL10.alIsBuffer(intBuffer.get(0))) {
            int i = AL10.alGenBuffers();
            AL10.alBufferData(1, SoundUtils.getOpenALFormat(format), byteBuffer, (int) format.getSampleRate());
            if( checkALError() )
                return false;

            AL10.alSourceQueueBuffers( soundSource.getSourceId(), i);
            if( checkALError() )
                return false;
        }else {
            AL10.alBufferData(intBuffer.get(0), SoundUtils.getOpenALFormat(format), byteBuffer, (int) format.getSampleRate());
            if( checkALError() )
                return false;

            AL10.alSourceQueueBuffers( soundSource.getSourceId(), intBuffer);
            if( checkALError() )
                return false;
        }

        return true;
    }

    public void play(String name){
        SoundSource soundSource = getSoundSource(name);
        if(soundSource != null){
            int sourceId = soundSource.getSourceId();
            if (this.checkALError()) {
            } else {
                if (AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) != AL_PLAYING) {
                    AL10.alSourcePlay(sourceId);
                    this.checkALError();
                }
            }
        }
    }

    public void stop(String name){
        SoundSource soundSource = getSoundSource(name);
        if(soundSource != null){
            int sourceId = soundSource.getSourceId();
            if (AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) == AL_PLAYING) {
                this.checkALError();
                AL10.alSourceStop(sourceId);
                this.checkALError();
            }
        }
    }

    public void flushProcessed(String name){
        flushIn(name,true);
    }

    public void flush(String name){
        flushIn(name,false);
    }

    public void flushIn(String name,boolean checkProcessed){
        SoundSource soundSource = getSoundSource(name);
        if(soundSource == null) return;
        int sourceId = soundSource.getSourceId();
        int queued = AL10.alGetSourcei(sourceId, AL_BUFFERS_QUEUED);
        int processed = AL10.alGetSourcei(sourceId,AL_BUFFERS_PROCESSED);
        if (!this.checkALError()) {
            for(; queued > 0; --queued) {
                if(checkProcessed) {
                    if (processed-- <= 0) return;
                }
                try
                {
                    AL10.alSourceUnqueueBuffers(sourceId);
                }
                catch( Exception e )
                {
                    return;
                }
                if(checkALError())
                    return;
            }
            this.millisPreviouslyPlayed = 0.0F;
        }
    }

    public void cleanup(){

        for (SoundSource soundSource :
                soundSourceMap.values()) {
            soundSource.cleanup();
        }

        for (SoundBuffer soundBuffer:
             soundBufferList) {
            soundBuffer.cleanup();
        }

        soundSourceMap.clear();
        soundBufferList.clear();
    }

    public SoundListener getListener() {
        return listener;
    }

    public void setListener(SoundListener listener) {
        this.listener = listener;
    }

    public Matrix4f getCameraMatrix() {
        return cameraMatrix;
    }
}
