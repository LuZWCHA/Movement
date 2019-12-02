package com.nowandfuture.mod.core.transformers;

import com.nowandfuture.mod.core.client.renders.CubesRenderer;
import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.utils.MathHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

public class RotationTransformNode extends AbstractTransformNode<RotationTransformNode.RotationKeyFrame> {

    private final Vector3f vector3f = new Vector3f();

    public RotationTransformNode(){
        super();
        setTypeId(2);
    }

    @Override
    protected void transform(CubesRenderer renderer, float p, RotationKeyFrame preKey, RotationKeyFrame key) {
        transformMatrix(renderer, p, preKey, key);
    }

    @Override
    public void transformMatrix(CubesRenderer renderer, float p, RotationKeyFrame preKey, RotationKeyFrame key) {
        vector3f.set(key.center.getX() + .5f,key.center.getY() + .5f,key.center.getZ() + .5f);
        renderer.getModelMatrix().translate(vector3f);

        Quaternion res = MathHelper.interpolate(preKey.quaternion,key.quaternion,p);
        renderer.getModelMatrix().transpose(MathHelper.mult(renderer.getModelMatrix(),res));

        vector3f.set(-key.center.getX() - .5f,-key.center.getY() - .5f,-key.center.getZ() - .5f);
        renderer.getModelMatrix().translate(vector3f);
    }

    @Override
    protected void transformPost(CubesRenderer renderer, float p, RotationKeyFrame pre, RotationKeyFrame key) {
    }

    @Override
    protected boolean isAcceptKeyFarm(KeyFrame keyFrame) {
        return keyFrame instanceof RotationKeyFrame;
    }

    @Override
    public void update(RotationKeyFrame key) {

    }


    public static class RotationKeyFrame extends KeyFrame<RotationTransformNode> {

        public final String NBT_ATTRIBUTE_ROT_X = "RotVetX";
        public final String NBT_ATTRIBUTE_ROT_Y = "RotVetY";
        public final String NBT_ATTRIBUTE_ROT_Z = "RotVetZ";
        public final String NBT_ATTRIBUTE_ROT_W = "RotVetW";

        //useless
        public final String NBT_ATTRIBUTE_RADIUS = "Radius";

        public final String NBT_ATTRIBUTE_CENTER_X = "CenterX";
        public final String NBT_ATTRIBUTE_CENTER_Y = "CenterY";
        public final String NBT_ATTRIBUTE_CENTER_Z = "CenterZ";

        //public AxisAngle4f axisAngle4f;
        public Quaternion quaternion;

        public BlockPos center;

        @Override
        public KeyFrame<RotationTransformNode> clone() {
            RotationKeyFrame keyFrame = new RotationKeyFrame();
            keyFrame.setBeginTick(getBeginTick());
            keyFrame.center = new BlockPos(center);
            keyFrame.quaternion = new Quaternion(quaternion);

            return keyFrame;
        }

        public RotationKeyFrame(){
            type = 1;
            center = new BlockPos(0,0,0);
            quaternion = new Quaternion();
        }

        public RotationKeyFrame(Quaternion quaternion){
            this(quaternion,BlockPos.ORIGIN);
        }

        public RotationKeyFrame(Quaternion quaternion,BlockPos center){
            this();
            this.quaternion = quaternion;
            this.center = center;
        }

        public NBTTagCompound writeParametersToNBT(NBTTagCompound compound) {
            compound.setFloat(NBT_ATTRIBUTE_ROT_X, quaternion.x);
            compound.setFloat(NBT_ATTRIBUTE_ROT_Y, quaternion.y);
            compound.setFloat(NBT_ATTRIBUTE_ROT_Z, quaternion.z);
            compound.setFloat(NBT_ATTRIBUTE_ROT_W, quaternion.w);

            compound.setInteger(NBT_ATTRIBUTE_CENTER_Z,center.getZ());
            compound.setInteger(NBT_ATTRIBUTE_CENTER_Y,center.getY());
            compound.setInteger(NBT_ATTRIBUTE_CENTER_X,center.getX());

            super.writeParametersToNBT(compound);

            return compound;
        }

        public void readParametersFromNBT(NBTTagCompound compound) {
            float rotX = compound.getFloat(NBT_ATTRIBUTE_ROT_X);
            float rotY = compound.getFloat(NBT_ATTRIBUTE_ROT_Y);
            float rotZ = compound.getFloat(NBT_ATTRIBUTE_ROT_Z);
            float angle = compound.getFloat(NBT_ATTRIBUTE_ROT_W);
            quaternion = new Quaternion(rotX,rotY,rotZ,angle);

            int cenX = compound.getInteger(NBT_ATTRIBUTE_CENTER_X);
            int cenY = compound.getInteger(NBT_ATTRIBUTE_CENTER_Y);
            int cenZ = compound.getInteger(NBT_ATTRIBUTE_CENTER_Z);
            center = new BlockPos(cenX,cenY,cenZ);

            super.readParametersFromNBT(compound);
        }

        @Override
        public void visit(RotationTransformNode visitor) {
            visitor.update(this);
        }

        public static RotationKeyFrame create(NBTTagCompound compound){
            RotationKeyFrame rotationKeyFrame = new RotationKeyFrame();
            rotationKeyFrame.readParametersFromNBT(compound);
            return rotationKeyFrame;
        }
    }


}
