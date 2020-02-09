package com.nowandfuture.mod.core.client.renders;

import com.nowandfuture.ffmpeg.FFmpegFrameGrabber;
import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.OpenALSoundHandler;
import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.ffmpeg.player.Utils;
import com.nowandfuture.ffmpeg.player.sound.SoundListener;
import com.nowandfuture.ffmpeg.player.sound.SoundSource;
import com.nowandfuture.mod.Movement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.lwjgl.util.vector.Vector3f;
import paulscode.sound.SoundSystem;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

public class MinecraftOpenALSoundHandler extends OpenALSoundHandler {


    private SoundSystem soundSystem;

    private Entity entity;
    private int cached = 0;

    public MinecraftOpenALSoundHandler(SimplePlayer player, String name, BlockPos pos) {
        super(player);
        entity = Minecraft.getMinecraft().player;
        setName(name);
        setPos(new Vector3f(pos.getX(),pos.getY(),pos.getZ()));
        soundManager.setListener(new SoundListener());
    }

    @Override
    protected void initSoundManager() {
        //do nothing
//        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
//        SoundManager soundManager = ReflectionHelper.getPrivateValue(SoundHandler.class,soundHandler,"sndManager");
//        soundSystem = ReflectionHelper.getPrivateValue(SoundManager.class,soundManager,"sndSystem");
    }

    @Override
    public void init(IMediaPlayer.SyncInfo info) {
        super.init(info);
//        FFmpegFrameGrabber grabber = simplePlayer.getGrabber();
//        sampleFormat = grabber.getSampleFormat();
//        sampleRate = grabber.getSampleRate();
//        audioChannel = grabber.getAudioChannels();
//        soundSystem.rawDataStream(Utils.getAudioFormat(sampleFormat,sampleRate ,audioChannel,sampleRate),
//                true,name,pos.x,pos.y,pos.z,1,1);
//        soundSystem.setVolume(name,1);

    }

    @Override
    public void handle(Frame frame) throws InterruptedException {
        updateListener();
//        soundSystem.moveListener((float) entity.posX,(float)entity.posY,(float)entity.posZ);

        super.handle(frame);
//        byte[] buffer = Utils.getAudio(frame.samples,simplePlayer.getVolume(),sampleFormat);
//        AudioFormat format = Utils.getAudioFormat(sampleFormat,sampleRate,audioChannel,sampleRate);
//
////        System.out.println(format.toString());
//        soundSystem.feedRawAudioData(name,buffer);
//        soundSystem.activate(name);
//        soundSystem.unloadSound(name);
//        soundSystem.loadSound(buffer,format,name);
//        soundSystem.play(name);
//        soundSystem.play(name);
//        SoundSource soundSource = soundManager.getSoundSource(name);
//        if(soundSource != null && soundSource.isPlaying()){
//            soundManager.flushProcessed(name);
//        }
//        soundManager.feedRawData(name,buffer,format);

//        soundSystem.flush(name);
//        soundSystem.feedRawAudioData(name,buffer);
//        Minecraft.getMinecraft().getSoundHandler().playSound(new StreamSound(name,1,));
    }

    public void updateListener(){
        Vector3f pos = new Vector3f(((float) entity.posX), ((float) entity.posY), ((float) entity.posZ));
        Vector3f motion = new Vector3f(((float) entity.motionX), ((float) entity.motionY), ((float) entity.motionZ));
        soundManager.getListener().setPosition(pos);
        soundManager.getListener().setSpeed(motion);
//        soundManager.getListener().setOrientation(
//                new Vector3f(entity.rotationPitch,entity.rotationYaw,0),
//                new Vector3f(0,1,0));
    }

    @Override
    public void destroy() {
//        soundSystem.flush(name);
        soundSystem.removeSource(name);
        super.destroy();
    }
}
