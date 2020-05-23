package com.nowandfuture.mod.core.transformers.arithmetics;

import com.nowandfuture.mod.core.transformers.RotationTransformNode;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.utils.math.MathHelper;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Quaternion;
import com.nowandfuture.mod.utils.math.Vector3f;

public class QuaternionSlerpInterpolation implements IInterpolationAlgorithm<RotationTransformNode.RotationKeyFrame> {

    private final Vector3f vector3f = new Vector3f();

    @Override
    public int getID() {
        return 2;
    }

    @Override
    public void calculate(Matrix4f renderer, KeyFrameLine line, float p, RotationTransformNode.RotationKeyFrame preKey, RotationTransformNode.RotationKeyFrame key) {
        vector3f.set(key.center.getX() + .5f,key.center.getY() + .5f,key.center.getZ() + .5f);
        renderer.translate(vector3f);

        Quaternion res;

        //prevent Quaternion's calculation error over 180 degrees which caused model's shaking
        if(preKey.rotX == key.rotX && preKey.rotY == key.rotY && preKey.rotZ == key.rotZ){
            res = MathHelper.eulerAnglesToQuaternion(key.rotX,key.rotY,key.rotZ);
        }else {
            res = MathHelper.interpolate(MathHelper.eulerAnglesToQuaternion(preKey.rotX, preKey.rotY, preKey.rotZ),
                    MathHelper.eulerAnglesToQuaternion(key.rotX, key.rotY, key.rotZ), p);
        }
        MathHelper.mul(renderer, res);

        renderer.translate(vector3f.negate());
    }
}
