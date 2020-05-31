/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info
 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package com.nowandfuture.mod.core.client.renderers.particulars;

import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

//I merged the particleBase and Trail
public class ParticularsTrail extends Particle {
    protected boolean dimAsAge;
    protected Random colorRand = new Random();
    private IParticularTarget target;

    protected ParticularsTrail(World worldIn, double posXIn, double posYIn, double posZIn,IParticularTarget target,long colorSeed) {
        super(worldIn, posXIn, posYIn, posZIn);
        this.target = target;

        multipleParticleScaleBy(0.5f);

        colorRand.setSeed(colorSeed);
        this.particleRed = colorRand.nextFloat() * 0.8F + 0.2F;
        this.particleGreen = colorRand.nextFloat() * 0.8F + 0.2F;
        this.particleBlue = colorRand.nextFloat() * 0.8F + 0.2F;
        float variant = rand.nextFloat() * 0.6F + 0.4F;
        this.particleRed *= variant;
        this.particleGreen *= variant;
        this.particleBlue *= variant;
        this.particleMaxAge = 2000;
        this.canCollide = false;
        this.dimAsAge = true;
        setParticleTextureIndex((int) (Math.random() * 8.0D));
    }

    private void calculateVector() {
        Vec3d endPoint = target.getPos();
        Vec3d vecParticle = new Vec3d(posX, posY, posZ);

        Vec3d vel = vecParticle.subtract(endPoint);
        vel = vel.normalize();

        float velScale = -0.1f;
        this.motionX = vel.x * velScale;
        this.motionY = vel.y * velScale;
        this.motionZ = vel.z * velScale;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = posX;
        this.prevPosY = posY;
        this.prevPosZ = posZ;

        if (target.isDead()) {
            setExpired();
            return;
        }

        if (particleAge >= particleMaxAge) {
            setExpired();
            return;
        }
        this.particleAge++;

        if (getPos().squareDistanceTo(target.getPos()) <= 0.1) {
            setExpired();
            return;
        }

        if(target.isMovable())
            calculateVector();

        move(motionX, motionY, motionZ);
    }

    protected Vec3d getPos(){
        return new Vec3d(posX,posY,posZ);
    }

    public void setParticleGravity(float particleGravity) {
        this.particleGravity = particleGravity;
    }

    @Override
    public int getBrightnessForRender(float par1) {
        if (dimAsAge) {
            int var2 = super.getBrightnessForRender(par1);
            float var3 = (float) particleAge / (float) particleMaxAge;
            var3 *= var3;
            var3 *= var3;
            int var4 = var2 & 255;
            int var5 = var2 >> 16 & 255;
            var5 += (int) (var3 * 15.0F * 16.0F);

            if (var5 > 240) {
                var5 = 240;
            }

            return var4 | var5 << 16;
        }
        return super.getBrightnessForRender(par1);
    }

    public static class Factory{
        public static Particle create(World worldIn, double posXIn, double posYIn, double posZIn,IParticularTarget target,long colorSeed){
            return new ParticularsTrail(worldIn, posXIn, posYIn, posZIn, target, colorSeed);
        }

        public static Particle create(World worldIn, double posXIn, double posYIn, double posZIn,IParticularTarget target){
            return new ParticularsTrail(worldIn, posXIn, posYIn, posZIn, target, 0);
        }

        public static Particle create(World worldIn, Vec3d start, IParticularTarget target){
            return new ParticularsTrail(worldIn, start.x, start.y, start.z, target, 0);
        }

        public static Particle create(World worldIn, Vec3d start, IParticularTarget target,long colorSeed){
            return new ParticularsTrail(worldIn, start.x, start.y, start.z, target, colorSeed);
        }
    }

}
