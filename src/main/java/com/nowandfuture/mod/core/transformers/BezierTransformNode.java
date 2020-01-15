package com.nowandfuture.mod.core.transformers;

import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.utils.math.Matrix4f;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

//not finished
// TODO: 2020/1/14 finish bezier curves
public class BezierTransformNode extends AbstractTransformNode<BezierTransformNode.BezierKeyFrame> {


    @Override
    protected boolean isAcceptKeyFarm(KeyFrame keyFrame) {
        return keyFrame instanceof BezierKeyFrame;
    }

    @Override
    protected void transform(Matrix4f renderer, float p, BezierKeyFrame preKey, BezierKeyFrame key) {

    }

    @Override
    public void transformMatrix(Matrix4f renderer, float p, BezierKeyFrame preKey, BezierKeyFrame key) {

    }

    @Override
    protected void transformPost(Matrix4f renderer, float p, BezierKeyFrame preKey, BezierKeyFrame key) {

    }

    @Override
    public void update(BezierKeyFrame list) {

    }

    public static class BezierKeyFrame extends KeyFrame<BezierTransformNode> {

        public final String NBT_ATTRIBUTE_OFFSET_X = "PosX";
        public final String NBT_ATTRIBUTE_OFFSET_Y = "PosY";
        public final String NBT_ATTRIBUTE_OFFSET_Z = "PosZ";

        public Vec3d curPos;

        @Override
        public KeyFrame<BezierTransformNode> clone() {
            BezierKeyFrame keyFrame = new BezierKeyFrame();
            keyFrame.setBeginTick(this.getBeginTick());
            keyFrame.curPos = new Vec3d(curPos.x,curPos.y,curPos.z);
            return keyFrame;
        }

        public BezierKeyFrame(){
            type = 5;
            curPos = new Vec3d(0,0,0);
        }

        public BezierKeyFrame(Vec3d curPos) {
            this.curPos = curPos;
        }

        public BezierKeyFrame(BlockPos curPos) {
            this.curPos = new Vec3d(curPos);
        }

        @Override
        public void visit(BezierTransformNode visitor) {
            visitor.update(this);
        }

        public void readParametersFromNBT(NBTTagCompound compound) {
            super.readParametersFromNBT(compound);
            double x = compound.getDouble(NBT_ATTRIBUTE_OFFSET_X);
            double y = compound.getDouble(NBT_ATTRIBUTE_OFFSET_Y);
            double z = compound.getDouble(NBT_ATTRIBUTE_OFFSET_Z);
            curPos = new Vec3d(x,y,z);
        }

        public NBTTagCompound writeParametersToNBT(@Nonnull NBTTagCompound compound) {
            compound.setDouble(NBT_ATTRIBUTE_OFFSET_X, curPos.x);
            compound.setDouble(NBT_ATTRIBUTE_OFFSET_Y, curPos.y);
            compound.setDouble(NBT_ATTRIBUTE_OFFSET_Z, curPos.z);
            super.writeParametersToNBT(compound);
            return compound;
        }

        public static BezierKeyFrame create(NBTTagCompound compound){
            BezierKeyFrame linearKeyFrame = new BezierKeyFrame();
            linearKeyFrame.readParametersFromNBT(compound);
            return linearKeyFrame;
        }
    }
}
