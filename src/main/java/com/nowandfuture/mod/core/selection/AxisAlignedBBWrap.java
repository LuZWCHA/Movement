package com.nowandfuture.mod.core.selection;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector3f;

public class AxisAlignedBBWrap extends AxisAlignedBB {

    private OBBox bounding;

    public AxisAlignedBBWrap( OBBox bounding) {
        super(0,0,0,0,0,0);
        this.bounding = bounding;
    }

    @Override
    public Vec3d getCenter() {
        Vector3f vector3f = bounding.getCenter();
        return new Vec3d(vector3f.x,vector3f.y,vector3f.z);
    }

    public OBBox getBounding() {
        return bounding;
    }

    @Override
    public double calculateXOffset(AxisAlignedBB other, double offsetX) {
        return calculateXYZOffset(other,offsetX);
    }

    @Override
    public double calculateYOffset(AxisAlignedBB other, double offsetY) {
        return calculateXYZOffset(other,offsetY);
    }

    @Override
    public double calculateZOffset(AxisAlignedBB other, double offsetZ) {
        return calculateXYZOffset(other, offsetZ);
    }

    private double calculateXYZOffset(AxisAlignedBB other, double offsetY){
        return 0;
    }
}
