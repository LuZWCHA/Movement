package com.nowandfuture.mod.core.transformers.arithmetics;

import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.utils.math.Matrix4f;

import javax.annotation.Nonnull;

public interface IInterpolationAlgorithm<T extends KeyFrame> {
    int getID();
    void calculate(Matrix4f renderer, @Nonnull KeyFrameLine line, float p, T preKey, T key);
}
