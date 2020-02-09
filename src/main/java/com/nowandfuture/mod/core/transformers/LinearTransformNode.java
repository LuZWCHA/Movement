package com.nowandfuture.mod.core.transformers;

import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.utils.math.MathHelper;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

public class LinearTransformNode extends AbstractTransformNode<LinearTransformNode.LinearKeyFrame> {

    private Vec3d temp;

    public LinearTransformNode(){
        super();
        setTypeId(1);
    }

    @Override
    protected void transform(Matrix4f renderer, float p, LinearKeyFrame preKey, LinearKeyFrame key) {
        transformMatrix(renderer, p, preKey, key);
    }

    @Override
    public void transformMatrix(Matrix4f renderer, float p, LinearKeyFrame preKey, LinearKeyFrame key) {
        temp = MathHelper.Lerp(p,preKey.curPos, key.curPos);
        renderer.translate(new Vector3f((float) temp.x,(float)temp.y,(float)temp.z));
    }

    @Override
    protected void transformPost(Matrix4f renderer, float p, LinearKeyFrame preKey, LinearKeyFrame key) {
    }

    @Override
    protected boolean isAcceptKeyFarm(KeyFrame keyFrame) {
        return keyFrame instanceof LinearKeyFrame;
    }

    @Override
    public void update(LinearKeyFrame linearKeyFarm) {

    }

    public static class LinearKeyFrame extends KeyFrame<LinearTransformNode> {

        public final String NBT_ATTRIBUTE_OFFSET_X = "PosX";
        public final String NBT_ATTRIBUTE_OFFSET_Y = "PosY";
        public final String NBT_ATTRIBUTE_OFFSET_Z = "PosZ";

        public Vec3d curPos;

        @Override
        public KeyFrame<LinearTransformNode> clone() {
            LinearKeyFrame keyFrame = new LinearKeyFrame();
            keyFrame.setBeginTick(this.getBeginTick());
            keyFrame.curPos = new Vec3d(curPos.x,curPos.y,curPos.z);
            return keyFrame;
        }

        public LinearKeyFrame(){
            type = 0;
            curPos = new Vec3d(0,0,0);
        }

        public LinearKeyFrame(Vec3d curPos) {
            this.curPos = curPos;
        }

        public LinearKeyFrame(BlockPos curPos) {
            this.curPos = new Vec3d(curPos);
        }

        @Override
        public void visit(LinearTransformNode visitor) {
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

        public static LinearKeyFrame create(NBTTagCompound compound){
            LinearKeyFrame linearKeyFrame = new LinearKeyFrame();
            linearKeyFrame.readParametersFromNBT(compound);
            return linearKeyFrame;
        }
    }
}
