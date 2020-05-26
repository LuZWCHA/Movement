package com.nowandfuture.mod.core.client.renders;

import net.minecraft.util.math.Vec3d;

public interface IParticularTarget {

    Vec3d getPos();
    boolean isDead();
    boolean isMovable();
}
