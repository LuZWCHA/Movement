package com.nowandfuture.mod.core.client.renders.videorenderer;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.OpenALSoundHandler;
import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.ffmpeg.player.sound.SoundListener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
    }

    @Override
    public void init(IMediaPlayer.SyncInfo info) {
        super.init(info);
    }

    @Override
    public long handle(Frame frame) throws InterruptedException {
        updateListener();
        return super.handle(frame);
    }

    private final Vector3f up = new Vector3f(0,1,0);
    private final Vector3f pos = new Vector3f(),orientation = new Vector3f(),motion = new Vector3f();

    public void updateListener(){
        pos.set(((float) entity.posX), ((float) entity.posY), ((float) entity.posZ));
        motion.set(((float) entity.motionX), ((float) entity.motionY), ((float) entity.motionZ));
        Vec3d lookAt = entity.getLookVec();
        orientation.set(((float) lookAt.x), ((float) lookAt.y), ((float) lookAt.z));
        soundManager.getListener().setPosition(pos);
        soundManager.getListener().setSpeed(motion);
        soundManager.getListener().setOrientation(orientation,up);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
