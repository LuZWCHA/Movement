package com.nowandfuture.mod.core.transformers.arithmetics;

import com.nowandfuture.mod.core.transformers.LocationTransformNode;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.utils.math.MathHelper;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.util.math.Vec3d;

public class LinearInterpolation implements IInterpolationAlgorithm<LocationTransformNode.LocationKeyFrame> {
    private Vec3d temp;

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public void calculate(Matrix4f renderer, KeyFrameLine line, float p, LocationTransformNode.LocationKeyFrame preKey, LocationTransformNode.LocationKeyFrame key) {
        temp = MathHelper.Lerp(p,preKey.curPos, key.curPos);
        renderer.translate(new Vector3f((float) temp.x,(float)temp.y,(float)temp.z));
    }
}
