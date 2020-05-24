package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.pcmconvert.PCMConvert;
import net.minecraft.client.renderer.GlStateManager;

import java.nio.ByteBuffer;

public class OpenGLDisplayHandler implements PlayHandler.DisplayHandler {
    protected int id;
    private Frame frame;
    private PCMConvert pcmConvert;
    private SimplePlayer simplePlayer;
    private int sampleFormat;
    private int sampleRate;

    public OpenGLDisplayHandler(SimplePlayer simplePlayer) {
        id = GlStateManager.generateTexture();
        this.simplePlayer = simplePlayer;
    }

    @Override
    public void init(IMediaPlayer.SyncInfo info) {
        pcmConvert = new PCMConvert(1 << 8);
        FFmpegFrameGrabber grabber = simplePlayer.getGrabber();
        sampleFormat = grabber.getSampleFormat();
        sampleRate = grabber.getSampleRate();
    }

    @Override
    public void handle(Frame frame) {
        if(frame != null && frame.image == null && frame.samples != null){
            byte[] mono = SoundUtils.getAudio(frame.samples,1f,sampleFormat);
            byte[] result = pcmConvert.readyDataByte(mono,mono.length / 32, sampleRate);
            frame.data = ByteBuffer.wrap(result);
        }

        this.frame = frame;
    }

    @Override
    public void flush() {

    }

    @Override
    public void destroy() {
//        GlStateManager.deleteTexture(id);
    }

    @Override
    public Object getFrameObj() {
        return frame;
    }

    @Override
    public void setGamma(float gamma) {

    }
}
