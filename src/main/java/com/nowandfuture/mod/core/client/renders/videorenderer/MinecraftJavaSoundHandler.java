package com.nowandfuture.mod.core.client.renders.videorenderer;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.JavaSoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class MinecraftJavaSoundHandler extends JavaSoundHandler {

    private Entity listener;
    private BlockPos pos;
    private float roll = 16;
    private float gain = 1;

    public MinecraftJavaSoundHandler(IMediaPlayer player, Entity listener, BlockPos pos) {
        super(player);
        this.listener = listener;
        this.pos = pos;
    }

    @Override
    public void init(IMediaPlayer.SyncInfo info) {
        super.init(info);
    }

    @Override
    public long handle(Frame frame) {
        final float distance = (float) pos.distanceSq(listener.getPosition());
        if(distance < 256d)
            super.handle(frame);
        return 0;
    }

    @Override
    protected float updateVolume() {
        final float distance = (float) pos.distanceSq(listener.getPosition());

        return distance > roll ? 3 * gain / (distance - roll + 1) : gain;
    }
}
