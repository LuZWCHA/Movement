package com.nowandfuture.mod.core.transformers;

import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.utils.math.Matrix4f;

public final class RootTransformNode extends AbstractTransformNode {
    public final static Long DEFAULT_TYPE = 0L;
    public final static String DEFAULT_NAME = "RootTransformNode";

    static RootTransformNode create(){
        RootTransformNode rootMovementPart = new RootTransformNode();
        rootMovementPart.setTypeId(DEFAULT_TYPE);
        rootMovementPart.setName(DEFAULT_NAME);
        return rootMovementPart;
    }

    private RootTransformNode() {
        super();
    }

    @Override
    protected boolean isAcceptKeyFarm(KeyFrame keyFrame) {
        return false;
    }

    @Override
    protected void transform(Matrix4f renderer, float p, KeyFrame preKey, KeyFrame key) {

    }

    @Override
    public void transformMatrix(Matrix4f renderer, float p, KeyFrame preKey, KeyFrame key) {

    }

    @Override
    protected void transformPost(Matrix4f renderer, float p, KeyFrame preKey, KeyFrame key) {

    }

    @Override
    public void update(KeyFrame list) {
        //do nothing
    }
}
