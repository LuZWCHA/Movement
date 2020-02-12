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
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.vector.Vector3f;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.libraries.ChannelLWJGLOpenAL;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Deprecated
public class MinecraftOpenALSoundHandler extends OpenALSoundHandler {

    private Entity entity;

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
    }

    @Override
    public void handle(Frame frame) throws InterruptedException {
        updateListener();
        super.handle(frame);
    }

    public void updateListener(){
        Vector3f pos = new Vector3f(((float) entity.posX), ((float) entity.posY), ((float) entity.posZ));
        Vector3f motion = new Vector3f(((float) entity.motionX), ((float) entity.motionY), ((float) entity.motionZ));
        soundManager.getListener().setPosition(pos);
        soundManager.getListener().setSpeed(motion);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
