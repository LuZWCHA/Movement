package com.nowandfuture.mod.core.transformers.animation;

public interface IKeyFarmVisitor<T extends KeyFrame> {
    void update(T list);
}
