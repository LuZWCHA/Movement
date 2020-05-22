package com.nowandfuture.mod.core.common.entities;

import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public interface IClickableTile {
    boolean onRightClick(Vec3d hit);
    boolean onLeftClick(Vec3d hit);
    //set (0,0,0) disable this option
    Vec3d getClickableFaceNormal();
    //the distance the entities can reach this tileEntity
    float getReachedDistance();
    //AABB
    AxisAlignedBB getClickBox(Vec3d start,Vec3d end,AxisAlignedBB area);
    //other box may used
    IBox getExtentBox();


    interface IBox{
        boolean intersect();
        Vector3f intersectAt();
        long intersectAtTime();
    }
}
