package com.nowandfuture.mod.core.transformers;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.utils.MathHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector3f;

import javax.vecmath.AxisAngle4f;
import java.nio.FloatBuffer;

public class RotationTransformNode extends AbstractTransformNode<RotationTransformNode.RotationKeyFrame> {

    private final Vector3f vector3f = new Vector3f();

    public RotationTransformNode(){
        super();
        setTypeId(2);
    }

    @Override
    protected void transform(AbstractPrefab recipe, float p, RotationKeyFrame preKey, RotationKeyFrame key) {
        transformMatrix(recipe, p, preKey, key);
    }

    @Override
    public void transformMatrix(AbstractPrefab recipe, float p, RotationKeyFrame preKey, RotationKeyFrame key) {
        vector3f.set(key.center.getX() + .5f,key.center.getY() + .5f,key.center.getZ() + .5f);
        recipe.getModelMatrix().translate(vector3f);
        final Vec3d mid = MathHelper.Lerp(p,
                new Vec3d(preKey.axisAngle4f.x,preKey.axisAngle4f.y,preKey.axisAngle4f.z),
                new Vec3d(key.axisAngle4f.x,key.axisAngle4f.y,key.axisAngle4f.z));

        final float angle = MathHelper.Lerp(p,preKey.axisAngle4f.angle,key.axisAngle4f.angle);
        vector3f.set((float) mid.x,(float) mid.y,(float) mid.z);
        recipe.getModelMatrix().rotate((float) (angle/180*Math.PI),vector3f.normalise(vector3f));

        vector3f.set(-key.center.getX() - .5f,-key.center.getY() - .5f,-key.center.getZ() - .5f);
        recipe.getModelMatrix().translate(vector3f);
    }

    @Override
    protected void transformPost(AbstractPrefab recipe, float p, RotationKeyFrame pre, RotationKeyFrame key) {
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
        public final String NBT_ATTRIBUTE_ROT_Angle = "RotVetAngle";

        //useless
        public final String NBT_ATTRIBUTE_RADIUS = "Radius";

        public final String NBT_ATTRIBUTE_CENTER_X = "CenterX";
        public final String NBT_ATTRIBUTE_CENTER_Y = "CenterY";
        public final String NBT_ATTRIBUTE_CENTER_Z = "CenterZ";

        public AxisAngle4f axisAngle4f;

        public BlockPos center;

        @Override
        public KeyFrame<RotationTransformNode> clone() {
            RotationKeyFrame keyFrame = new RotationKeyFrame();
            keyFrame.setBeginTick(getBeginTick());
            keyFrame.center = new BlockPos(center);
            keyFrame.axisAngle4f = new AxisAngle4f(axisAngle4f);

            return keyFrame;
        }

        public RotationKeyFrame(){
            type = 1;
            center = new BlockPos(0,0,0);
            axisAngle4f = new AxisAngle4f(0,1,0,0);
        }

        public RotationKeyFrame(AxisAngle4f axisAngle4f){
            this(axisAngle4f,BlockPos.ORIGIN);
        }

        public RotationKeyFrame(AxisAngle4f axisAngle4f,BlockPos center){
            this();
            this.axisAngle4f = axisAngle4f;
            this.center = center;
        }

        public NBTTagCompound writeParametersToNBT(NBTTagCompound compound) {
            compound.setFloat(NBT_ATTRIBUTE_ROT_X, axisAngle4f.x);
            compound.setFloat(NBT_ATTRIBUTE_ROT_Y, axisAngle4f.y);
            compound.setFloat(NBT_ATTRIBUTE_ROT_Z, axisAngle4f.z);
            compound.setFloat(NBT_ATTRIBUTE_ROT_Angle, axisAngle4f.angle);

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
            float angle = compound.getFloat(NBT_ATTRIBUTE_ROT_Angle);
            axisAngle4f = new AxisAngle4f(rotX,rotY,rotZ,angle);

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
