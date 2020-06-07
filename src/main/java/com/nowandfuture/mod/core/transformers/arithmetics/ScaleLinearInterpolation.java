package com.nowandfuture.mod.core.transformers.arithmetics;

import com.nowandfuture.mod.core.transformers.ScaleTransformNode;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.utils.math.MathHelper;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;

public class ScaleLinearInterpolation implements IInterpolationAlgorithm<ScaleTransformNode.ScaleKeyFrame>{
    @Override
    public int getID() {
        return 3;
    }

    @Override
    public void calculate(Matrix4f renderer, KeyFrameLine line, float p, ScaleTransformNode.ScaleKeyFrame preKey, ScaleTransformNode.ScaleKeyFrame key) {
        final float s = MathHelper.lerp(p,preKey.scale,key.scale);
        renderer.scale(new Vector3f(s,s,s));
    }
}
