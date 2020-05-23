package com.nowandfuture.mod.core.transformers;

import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.core.transformers.arithmetics.IInterpolationAlgorithm;
import com.nowandfuture.mod.utils.math.Matrix4f;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

//default linear transform
public class LocationTransformNode extends AbstractTransformNode<LocationTransformNode.LocationKeyFrame> {
    private KeyFrameLine line;

    public LocationTransformNode(){
        super();
        setTypeId(1);
        setArithmeticId(1);
    }

    @Override
    protected void transform(Matrix4f renderer, float p, LocationKeyFrame preKey, LocationKeyFrame key) {
        IInterpolationAlgorithm<LocationKeyFrame> algorithm = getAlgorithm(getArithmeticId());
        algorithm.calculate(renderer,line,p,preKey,key);
    }

    @Override
    protected void transformPost(Matrix4f renderer, float p, LocationKeyFrame preKey, LocationKeyFrame key) {
    }

    @Override
    public void prepare(KeyFrameLine frameLine) {
        this.line = frameLine;
    }

    @Override
    protected boolean isAcceptKeyFarm(KeyFrame keyFrame) {
        return keyFrame instanceof LocationKeyFrame;
    }

    @Override
    public void update(LocationKeyFrame linearKeyFarm) {

    }

    public static class LocationKeyFrame extends KeyFrame<LocationTransformNode> {

        public final String NBT_ATTRIBUTE_OFFSET_X = "PosX";
        public final String NBT_ATTRIBUTE_OFFSET_Y = "PosY";
        public final String NBT_ATTRIBUTE_OFFSET_Z = "PosZ";

        public Vec3d curPos;

        @Override
        public KeyFrame<LocationTransformNode> clone() {
            LocationKeyFrame keyFrame = new LocationKeyFrame();
            keyFrame.setBeginTick(this.getBeginTick());
            keyFrame.curPos = new Vec3d(curPos.x,curPos.y,curPos.z);
            return keyFrame;
        }

        public LocationKeyFrame(){
            type = 0;
            curPos = new Vec3d(0,0,0);
        }

        public LocationKeyFrame(Vec3d curPos) {
            this.curPos = curPos;
        }

        public LocationKeyFrame(BlockPos curPos) {
            this.curPos = new Vec3d(curPos);
        }

        @Override
        public void visit(LocationTransformNode visitor) {
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

        public static LocationKeyFrame create(NBTTagCompound compound){
            LocationKeyFrame locationKeyFrame = new LocationKeyFrame();
            locationKeyFrame.readParametersFromNBT(compound);
            return locationKeyFrame;
        }
    }
}
