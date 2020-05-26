package com.nowandfuture.mod.core.client.renders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public enum  ParticularManager {
    INSTANCE;


    Minecraft mc = Minecraft.getMinecraft();
    double TRACKING_DISTANCE = 64;
    Random rand = new Random();

    public void trailEffect(BlockPos start, TileEntity dest) {
        if (mc.player.getDistanceSq(start) > TRACKING_DISTANCE)
            return;
        if (rand.nextInt(3) == 0) {
            double px = start.getX() + 0.5 + rand.nextGaussian() * 0.1;
            double py = start.getY() + 0.5 + rand.nextGaussian() * 0.1;
            double pz = start.getZ() + 0.5 + rand.nextGaussian() * 0.1;
            Particle particle = ParticularsTrail.Factory.create(dest.getWorld(), new Vec3d(px, py, pz), wrap(dest));
            spawnParticle(particle);
        }
    }

    public void trailEffect(BlockPos start, World world, IParticularTarget target,int f) {
        if (mc.player.getDistanceSq(start) > TRACKING_DISTANCE)
            return;
        if (rand.nextInt(f) == 0) {
            double px = start.getX() + 0.5 + rand.nextGaussian() * 0.1;
            double py = start.getY() + 0.5 + rand.nextGaussian() * 0.1;
            double pz = start.getZ() + 0.5 + rand.nextGaussian() * 0.1;
            Particle particle = ParticularsTrail.Factory.create(world, new Vec3d(px, py, pz), target);
            spawnParticle(particle);
        }
    }

    public void trailEffect(BlockPos start, World world, IParticularTarget target,int f,long colorRand) {
        if (mc.player.getDistanceSq(start) > TRACKING_DISTANCE)
            return;
        if (rand.nextInt(f) == 0) {
            double px = start.getX() + 0.5 + rand.nextGaussian() * 0.1;
            double py = start.getY() + 0.5 + rand.nextGaussian() * 0.1;
            double pz = start.getZ() + 0.5 + rand.nextGaussian() * 0.1;
            Particle particle = ParticularsTrail.Factory.create(world, new Vec3d(px, py, pz), target,colorRand);
            spawnParticle(particle);
        }
    }

    protected void spawnParticle(Particle particle) {
        mc.effectRenderer.addEffect(particle);
    }

    public IParticularTarget wrap(TileEntity tileEntity){
        return new IParticularTarget() {
            @Override
            public Vec3d getPos() {
                return new Vec3d(tileEntity.getPos());
            }

            @Override
            public boolean isDead() {
                return tileEntity.isInvalid();
            }

            @Override
            public boolean isMovable() {
                return true;
            }
        };
    }

}
