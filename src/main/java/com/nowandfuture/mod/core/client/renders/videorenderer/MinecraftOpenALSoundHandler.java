package com.nowandfuture.mod.core.client.renders.videorenderer;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.OpenALSoundHandler;
import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.ffmpeg.player.sound.SoundListener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.util.vector.Vector3f;

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
