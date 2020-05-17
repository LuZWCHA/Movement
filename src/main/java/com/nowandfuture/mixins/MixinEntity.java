package com.nowandfuture.mixins;

import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.handler.CollisionHandler;
import com.nowandfuture.mod.utils.collision.CollisionHelper;
import com.nowandfuture.mod.utils.collision.CollisionInfo;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow public World world;

    @Shadow public boolean collidedHorizontally;
    @Shadow public boolean collidedVertically;

    @Shadow public abstract void setEntityBoundingBox(AxisAlignedBB bb);

    @Shadow @Nullable public abstract AxisAlignedBB getCollisionBoundingBox();

    @Shadow public abstract AxisAlignedBB getEntityBoundingBox();

    @Shadow public abstract void resetPositionToBB();

    @Shadow public boolean onGround;
    @Shadow public boolean collided;

    @Shadow protected abstract void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos);

    @Shadow public double motionX;
    @Shadow public double motionZ;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;
    @Shadow public float distanceWalkedModified;
    @Shadow public float distanceWalkedOnStepModified;
    @Shadow private int nextStepDistance;

    @Shadow public abstract boolean isInWater();

    @Shadow public abstract boolean isBeingRidden();

    @Shadow @Nullable public abstract Entity getControllingPassenger();

    @Shadow protected Random rand;

    @Shadow protected abstract SoundEvent getSwimSound();

    @Shadow public abstract void playSound(SoundEvent soundIn, float volume, float pitch);

    @Shadow protected abstract void playStepSound(BlockPos pos, Block blockIn);

    @Shadow protected abstract boolean makeFlySound();

    @Shadow private float nextFlap;

    @Shadow protected abstract float playFlySound(float p_191954_1_);

    @Shadow protected abstract void doBlockCollisions();

    @Shadow public abstract void addEntityCrashInfo(CrashReportCategory category);

    @Shadow protected abstract boolean canTriggerWalking();

    @Shadow public abstract boolean isSneaking();

    @Shadow public abstract boolean isRiding();

    @Shadow public double motionY;
    @Shadow public boolean noClip;
    @Shadow private long pistonDeltasGameTime;
    @Shadow @Final private double[] pistonDeltas;
    @Shadow protected boolean isInWeb;
    @Shadow public float stepHeight;

    @Shadow public abstract boolean isWet();

    @Shadow protected abstract void dealFireDamage(int amount);

    @Shadow private int fire;

    @Shadow public abstract void setFire(int seconds);

    @Shadow protected abstract int getFireImmuneTicks();

    @Shadow public abstract boolean isBurning();

    private double ox,oy,oz;
    private AxisAlignedBB org;

//    @Inject(
//            method = "move",
//            at = @At("TAIL"),
//            locals = LocalCapture.CAPTURE_FAILSOFT
//    )
//    private void inject_move_after(MoverType type, double x, double y, double z, CallbackInfo callbackInfo){
//        Entity it = (Entity)((Object)this);
//
//        if(ox != 0 || oy != 0 || oz != 0){
//
//            setEntityBoundingBox(org);
//
//
//            i = 0;
//            Vector3f v = moveIn(type,x,y,z);
//
//            x = v.x;
//            y = v.y;
//            z = v.z;
//
//            resetPositionToBB();
//
//            this.onGround = this.collidedVertically && oy < 0.0D;
//            this.collided = this.collidedHorizontally || this.collidedVertically;
//
//            int j6 = MathHelper.floor(this.posX);
//            int i1 = MathHelper.floor(this.posY - 0.20000000298023224D);
//            int k6 = MathHelper.floor(this.posZ);
//            BlockPos blockpos = new BlockPos(j6, i1, k6);
//            IBlockState iblockstate = this.world.getBlockState(blockpos);
//
//            if (iblockstate.getMaterial() == Material.AIR)
//            {
//                BlockPos blockpos1 = blockpos.down();
//                IBlockState iblockstate1 = this.world.getBlockState(blockpos1);
//                Block block1 = iblockstate1.getBlock();
//
//                if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate)
//                {
//                    iblockstate = iblockstate1;
//                    blockpos = blockpos1;
//                }
//            }
//
//            this.updateFallState(y, this.onGround, iblockstate, blockpos);
//
//            if (ox != x)
//            {
//                this.motionX = x;
//            }
//
//            if (oz != z)
//            {
//                this.motionZ = z;
//            }
//
//            if(oy != y){
//
//            }
//        }
//    }


    private Vector3f calculateDisplacement(Vector3f displacement,Vector3f impactAxis){

        if (impactAxis.lengthSquared() != 0) {
            subVOnAxis(displacement, impactAxis, 1);
        }

        return displacement;
    }

    private void subVOnAxis(Vector3f v,Vector3f axis,float factor){
        float f = Vector3f.dot(axis,v);
        Vector3f vn = new Vector3f(axis.x * f * factor, axis.y * f * factor, axis.z * f * factor);
        Vector3f v1 = Vector3f.sub(v,vn,new Vector3f());
        v.set(v1.x,v1.y,v1.z);
    }

    private void push(CollisionInfo collisionInfo,List<CollisionInfo> collisionInfos){
        for (CollisionInfo info :
                collisionInfos) {
            if (collisionInfo.getImpactAxis().equals(info.getImpactAxis())) {
                return;
            }
        }
        collisionInfos.add(collisionInfo);
    }

//    @Inject(
//            method = "move",
//            at = @At("HEAD"),
//            locals = LocalCapture.CAPTURE_FAILSOFT
//    )
//    private void inject_move_before(MoverType type, double x, double y, double z, CallbackInfo callbackInfo){
//        Entity it = (Entity)((Object)this);
//
//        ox = x;
//        oy = y;
//        oz = z;
//
//        org = it.getEntityBoundingBox();
//
//        d10 = this.posX;
//        d11 = this.posY;
//        d1 = this.posZ;
//    }


    @Inject(
            method = "move",
            at = @At("HEAD"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void overwrite_move(MoverType type, double x, double y, double z, CallbackInfo callbackInfo){
        Entity it = (Entity)((Object)this);

        if (this.noClip)
        {
            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
            this.resetPositionToBB();
        }
        else
        {
            if (type == MoverType.PISTON)
            {
                long i = this.world.getTotalWorldTime();

                if (i != this.pistonDeltasGameTime)
                {
                    Arrays.fill(this.pistonDeltas, 0.0D);
                    this.pistonDeltasGameTime = i;
                }

                if (x != 0.0D)
                {
                    int j = EnumFacing.Axis.X.ordinal();
                    double d0 = MathHelper.clamp(x + this.pistonDeltas[j], -0.51D, 0.51D);
                    x = d0 - this.pistonDeltas[j];
                    this.pistonDeltas[j] = d0;

                    if (Math.abs(x) <= 9.999999747378752E-6D)
                    {
                        return;
                    }
                }
                else if (y != 0.0D)
                {
                    int l4 = EnumFacing.Axis.Y.ordinal();
                    double d12 = MathHelper.clamp(y + this.pistonDeltas[l4], -0.51D, 0.51D);
                    y = d12 - this.pistonDeltas[l4];
                    this.pistonDeltas[l4] = d12;

                    if (Math.abs(y) <= 9.999999747378752E-6D)
                    {
                        return;
                    }
                }
                else
                {
                    if (z == 0.0D)
                    {
                        return;
                    }

                    int i5 = EnumFacing.Axis.Z.ordinal();
                    double d13 = MathHelper.clamp(z + this.pistonDeltas[i5], -0.51D, 0.51D);
                    z = d13 - this.pistonDeltas[i5];
                    this.pistonDeltas[i5] = d13;

                    if (Math.abs(z) <= 9.999999747378752E-6D)
                    {
                        return;
                    }
                }
            }

            this.world.profiler.startSection("move");
            double d10 = this.posX;
            double d11 = this.posY;
            double d1 = this.posZ;

            if (this.isInWeb)
            {
                this.isInWeb = false;
                x *= 0.25D;
                y *= 0.05000000074505806D;
                z *= 0.25D;
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
            }

            double d2 = x;
            double d3 = y;
            double d4 = z;

            if ((type == MoverType.SELF || type == MoverType.PLAYER) && this.onGround && this.isSneaking() && it instanceof EntityPlayer)
            {
                for (; x != 0.0D && this.world.getCollisionBoxes(it, this.getEntityBoundingBox().offset(x, (double)(-this.stepHeight), 0.0D)).isEmpty(); d2 = x)
                {
                    if (x < 0.05D && x >= -0.05D)
                    {
                        x = 0.0D;
                    }
                    else if (x > 0.0D)
                    {
                        x -= 0.05D;
                    }
                    else
                    {
                        x += 0.05D;
                    }
                }

                for (; z != 0.0D && this.world.getCollisionBoxes(it, this.getEntityBoundingBox().offset(0.0D, -this.stepHeight, z)).isEmpty(); d4 = z)
                {
                    if (z < 0.05D && z >= -0.05D)
                    {
                        z = 0.0D;
                    }
                    else if (z > 0.0D)
                    {
                        z -= 0.05D;
                    }
                    else
                    {
                        z += 0.05D;
                    }
                }

                for (; x != 0.0D && z != 0.0D && this.world.getCollisionBoxes(it, this.getEntityBoundingBox().offset(x, -this.stepHeight, z)).isEmpty(); d4 = z)
                {
                    if (x < 0.05D && x >= -0.05D)
                    {
                        x = 0.0D;
                    }
                    else if (x > 0.0D)
                    {
                        x -= 0.05D;
                    }
                    else
                    {
                        x += 0.05D;
                    }

                    d2 = x;

                    if (z < 0.05D && z >= -0.05D)
                    {
                        z = 0.0D;
                    }
                    else if (z > 0.0D)
                    {
                        z -= 0.05D;
                    }
                    else
                    {
                        z += 0.05D;
                    }
                }
            }

            i = 0;

            float oldX = (float) x,oldY = (float) y,oldZ = (float) z;

            Vector3f v = moveIn(type, x, y, z);
            x = v.x;
            y = v.y;
            z = v.z;

            if(oldX != v.x)
                motionX = 0;
            if(oldZ != v.z)
                motionZ = 0;

            double orgX = x,orgY = y,orgZ = z;

            setEntityBoundingBox(getEntityBoundingBox().offset(v.x,v.y,v.z));
//            List<AxisAlignedBB> list1 = this.world.getCollisionBoxes(it, this.getEntityBoundingBox().expand(x, y, z));
//            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();

//            if (y != 0.0D)
//            {
//                int k = 0;
//
//                for (int l = list1.size(); k < l; ++k)
//                {
//                    y = list1.get(k).calculateYOffset(this.getEntityBoundingBox(), y);
//                }
//
//                this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
//            }
//
//            if (x != 0.0D)
//            {
//                int j5 = 0;
//
//                for (int l5 = list1.size(); j5 < l5; ++j5)
//                {
//                    x = list1.get(j5).calculateXOffset(this.getEntityBoundingBox(), x);
//                }
//
//                if (x != 0.0D)
//                {
//                    this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
//                }
//            }
//
//            if (z != 0.0D)
//            {
//                int k5 = 0;
//
//                for (int i6 = list1.size(); k5 < i6; ++k5)
//                {
//                    z = list1.get(k5).calculateZOffset(this.getEntityBoundingBox(), z);
//                }
//
//                if (z != 0.0D)
//                {
//                    this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
//                }
//            }

            boolean flag = this.onGround || d3 != y && d3 < 0.0D;

//            if (this.stepHeight > 0.0F && flag && (d2 != x || d4 != z))
//            {
//                double d14 = x;
//                double d6 = y;
//                double d7 = z;
//                AxisAlignedBB axisalignedbb1 = this.getEntityBoundingBox();
//                this.setEntityBoundingBox(org);
//                y = this.stepHeight;
//                List<AxisAlignedBB> list = this.world.getCollisionBoxes(it, this.getEntityBoundingBox().expand(d2, y, d4));
//                AxisAlignedBB axisalignedbb2 = this.getEntityBoundingBox();
//                AxisAlignedBB axisalignedbb3 = axisalignedbb2.expand(d2, 0.0D, d4);
//                double d8 = y;
//                int j1 = 0;
//
//                for (int k1 = list.size(); j1 < k1; ++j1)
//                {
//                    d8 = list.get(j1).calculateYOffset(axisalignedbb3, d8);
//                }
//
//                axisalignedbb2 = axisalignedbb2.offset(0.0D, d8, 0.0D);
//                double d18 = d2;
//                int l1 = 0;
//
//                for (int i2 = list.size(); l1 < i2; ++l1)
//                {
//                    d18 = list.get(l1).calculateXOffset(axisalignedbb2, d18);
//                }
//
//                axisalignedbb2 = axisalignedbb2.offset(d18, 0.0D, 0.0D);
//                double d19 = d4;
//                int j2 = 0;
//
//                for (int k2 = list.size(); j2 < k2; ++j2)
//                {
//                    d19 = list.get(j2).calculateZOffset(axisalignedbb2, d19);
//                }
//
//                axisalignedbb2 = axisalignedbb2.offset(0.0D, 0.0D, d19);
//                AxisAlignedBB axisalignedbb4 = this.getEntityBoundingBox();
//                double d20 = y;
//                int l2 = 0;
//
//                for (int i3 = list.size(); l2 < i3; ++l2)
//                {
//                    d20 = list.get(l2).calculateYOffset(axisalignedbb4, d20);
//                }
//
//                axisalignedbb4 = axisalignedbb4.offset(0.0D, d20, 0.0D);
//                double d21 = d2;
//                int j3 = 0;
//
//                for (int k3 = list.size(); j3 < k3; ++j3)
//                {
//                    d21 = list.get(j3).calculateXOffset(axisalignedbb4, d21);
//                }
//
//                axisalignedbb4 = axisalignedbb4.offset(d21, 0.0D, 0.0D);
//                double d22 = d4;
//                int l3 = 0;
//
//                for (int i4 = list.size(); l3 < i4; ++l3)
//                {
//                    d22 = list.get(l3).calculateZOffset(axisalignedbb4, d22);
//                }
//
//                axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d22);
//                double d23 = d18 * d18 + d19 * d19;
//                double d9 = d21 * d21 + d22 * d22;
//
//                if (d23 > d9)
//                {
//                    x = d18;
//                    z = d19;
//                    y = -d8;
//                    this.setEntityBoundingBox(axisalignedbb2);
//                }
//                else
//                {
//                    x = d21;
//                    z = d22;
//                    y = -d20;
//                    this.setEntityBoundingBox(axisalignedbb4);
//                }
//
//                int j4 = 0;
//
//                for (int k4 = list.size(); j4 < k4; ++j4)
//                {
//                    y = list.get(j4).calculateYOffset(this.getEntityBoundingBox(), y);
//                }
//
//                this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
//
//                if (d14 * d14 + d7 * d7 >= x * x + z * z)
//                {
//                    x = d14;
//                    y = d6;
//                    z = d7;
//                    this.setEntityBoundingBox(axisalignedbb1);
//                }
//            }

            this.world.profiler.endSection();
            this.world.profiler.startSection("rest");
            this.resetPositionToBB();
            this.collidedHorizontally = orgX != x || orgZ != z;
//            this.collidedHorizontally = isSameAsFloat(d2,x) || isSameAsFloat(d4,z);
            this.collidedVertically = d3 != y;
            this.onGround = this.collidedVertically && d3 < 0.0D;
            this.collided = this.collidedHorizontally || this.collidedVertically;
            int j6 = MathHelper.floor(this.posX);
            int i1 = MathHelper.floor(this.posY - 0.20000000298023224D);
            int k6 = MathHelper.floor(this.posZ);
            BlockPos blockpos = new BlockPos(j6, i1, k6);
            IBlockState iblockstate = this.world.getBlockState(blockpos);

            if (iblockstate.getMaterial() == Material.AIR)
            {
                BlockPos blockpos1 = blockpos.down();
                IBlockState iblockstate1 = this.world.getBlockState(blockpos1);
                Block block1 = iblockstate1.getBlock();

                if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate)
                {
                    iblockstate = iblockstate1;
                    blockpos = blockpos1;
                }
            }

            this.updateFallState(y, this.onGround, iblockstate, blockpos);

            if (x != orgX)
            {
                this.motionX = 0.0D;
            }

            if (z != orgZ)
            {
                this.motionZ = 0.0D;
            }

            Block block = iblockstate.getBlock();

            if (!isSameAsFloat(y,d3))
            {
                block.onLanded(this.world, it);
            }

            if (this.canTriggerWalking() && (!this.onGround || !this.isSneaking() || !(it instanceof EntityPlayer)) && !this.isRiding())
            {
                double d15 = this.posX - d10;
                double d16 = this.posY - d11;
                double d17 = this.posZ - d1;

                if (block != Blocks.LADDER)
                {
                    d16 = 0.0D;
                }

                if (block != null && this.onGround)
                {
                    block.onEntityWalk(this.world, blockpos, it);
                }

                this.distanceWalkedModified = (float)((double)this.distanceWalkedModified + (double)MathHelper.sqrt(d15 * d15 + d17 * d17) * 0.6D);
                this.distanceWalkedOnStepModified = (float)((double)this.distanceWalkedOnStepModified + (double)MathHelper.sqrt(d15 * d15 + d16 * d16 + d17 * d17) * 0.6D);

                if (this.distanceWalkedOnStepModified > (float)this.nextStepDistance && iblockstate.getMaterial() != Material.AIR)
                {
                    this.nextStepDistance = (int)this.distanceWalkedOnStepModified + 1;

                    if (this.isInWater())
                    {
                        Entity entity = this.isBeingRidden() && this.getControllingPassenger() != null ? this.getControllingPassenger() : it;
                        float f = entity == it ? 0.35F : 0.4F;
                        float f1 = MathHelper.sqrt(entity.motionX * entity.motionX * 0.20000000298023224D + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ * 0.20000000298023224D) * f;

                        if (f1 > 1.0F)
                        {
                            f1 = 1.0F;
                        }

                        this.playSound(this.getSwimSound(), f1, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                    }
                    else
                    {
                        this.playStepSound(blockpos, block);
                    }
                }
                else if (this.distanceWalkedOnStepModified > this.nextFlap && this.makeFlySound() && iblockstate.getMaterial() == Material.AIR)
                {
                    this.nextFlap = this.playFlySound(this.distanceWalkedOnStepModified);
                }
            }

            try
            {
                this.doBlockCollisions();
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
                this.addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            boolean flag1 = this.isWet();

            if (this.world.isFlammableWithin(this.getEntityBoundingBox().shrink(0.001D)))
            {
                this.dealFireDamage(1);

                if (!flag1)
                {
                    ++this.fire;

                    if (this.fire == 0)
                    {
                        this.setFire(8);
                    }
                }
            }
            else if (this.fire <= 0)
            {
                this.fire = -this.getFireImmuneTicks();
            }

            if (flag1 && this.isBurning())
            {
                this.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                this.fire = -this.getFireImmuneTicks();
            }

            this.world.profiler.endSection();
        }


        callbackInfo.cancel();
    }

    private int i = 0;

    private Vector3f moveIn(MoverType type, double x, double y, double z){

        Entity it = (Entity)((Object)this);
        List<OBBox> obBoxes = new ArrayList<>();
        Vector3f displacement = new Vector3f(x,y,z);
        CollisionHandler.collectOBBoxes(obBoxes,it,displacement);
        List <AxisAlignedBB> axisAlignedBBList = world.getCollisionBoxes(it,getEntityBoundingBox().expand(x,y,z));

        List<CollisionInfo> collisionInfos = new ArrayList<>();
        CollisionHandler.collisionWithOBBoxes(it,displacement,obBoxes,collisionInfos);
        CollisionHandler.collisionWithAABBoxes(it,displacement,axisAlignedBBList,collisionInfos);

        collisionInfos.sort(CollisionHelper.getComparator());

        Vector3f v = new Vector3f(displacement);

        if(!collisionInfos.isEmpty()) {

            int contactNum = 0;
            for (CollisionInfo info :
                    collisionInfos) {
                if (Math.abs(info.getImpactTime()) == 0) {
                    contactNum++;
                }
            }

            if(contactNum < 1) {
                CollisionInfo collisionInfo = collisionInfos.get(0);

                Vector3f vector3f = new Vector3f(v);

                double impactTime = collisionInfo.getImpactTime();
//                Vector3f ia = collisionInfo.getImpactAxis();
                vector3f.scale((float) (1 - impactTime));
                v.scale((float) impactTime * 0.9f);
//                boolean test = false;
//                if(!obBoxes.isEmpty()) {
//                    test = OBBox.Collision.intersect(new OBBox(getEntityBoundingBox().offset(v.x,v.y,v.z)), obBoxes.get(0));
//                    System.out.println(test);
//                }
                if(v.lengthSquared() > 0)
                    setEntityBoundingBox(getEntityBoundingBox().offset(v.x, v.y, v.z));
//                if(!obBoxes.isEmpty()) {
//                    test = OBBox.Collision.intersect(new OBBox(getEntityBoundingBox()), obBoxes.get(0));
//                    System.out.println(test + String.valueOf(i));
//                }
                v = vector3f;
            }else if(contactNum == 1){
                CollisionInfo collisionInfo = collisionInfos.get(0);
                Vector3f ia = collisionInfo.getImpactAxis();
                calculateDisplacement(v,ia);
            } else if(contactNum == 2){
                Vector3f axis2 = Vector3f.cross(collisionInfos.get(0).getImpactAxis(),
                        collisionInfos.get(1).getImpactAxis(), new Vector3f());
                if (axis2.lengthSquared() != 0) {
                    axis2.normalise();
                    float f = Vector3f.dot(v, axis2) * 0.9f;
                    v = new Vector3f(axis2.x * f, axis2.y * f, axis2.z * f);
                }else{
                    v = new Vector3f(0,0,0);
                }
            }else{
                v = new Vector3f(0,0,0);
            }

            if(v.lengthSquared() > 3E-12) {
                if(i++ > 6){
                    v = new Vector3f(0,0,0);
                }
                else return moveIn(type, v.x, v.y, v.z);
            }else{
                v = new Vector3f(0,0,0);
            }
        }

        return v;
    }

    private boolean isSameAsFloat(double x,double y){
        return (float)x == (float)y;
    }
}
