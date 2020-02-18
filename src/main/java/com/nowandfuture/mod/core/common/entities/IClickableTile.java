package com.nowandfuture.mod.core.common.entities;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public interface IClickableTile {
    boolean onRightClick(Vec3d hit);
    boolean onLeftClick(Vec3d hit);
    //set (0,0,0) disable this option
    Vec3d getClickableFaceNormal();
    float getReachedDistance();
    AxisAlignedBB getClickBox();
}
