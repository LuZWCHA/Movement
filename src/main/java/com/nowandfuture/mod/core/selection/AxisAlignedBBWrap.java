package com.nowandfuture.mod.core.selection;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;

public class AxisAlignedBBWrap extends AxisAlignedBB {

    private OBBox bounding;
    private float impactTime;
    private Vector3f v;

    public AxisAlignedBBWrap(OBBox bounding, float impactTime,@Nullable Vector3f v) {
        super(0,0,0,0,0,0);
        this.bounding = bounding;
        this.impactTime = impactTime;
        this.v = v;
        this.v = null;
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
        if(v == null)
            return calculateXYZOffset(other,offsetX);
        else{
            return v.getX();
        }
    }

    @Override
    public double calculateYOffset(AxisAlignedBB other, double offsetY) {
        if(v == null)
            return calculateXYZOffset(other,offsetY);
        else{
            return v.getY();
        }
    }

    @Override
    public double calculateZOffset(AxisAlignedBB other, double offsetZ) {
        if(v == null)
            return calculateXYZOffset(other, offsetZ);
        else{
            return v.getZ();
        }
    }

    private double calculateXYZOffset(AxisAlignedBB other, double offset){
        return offset * impactTime;
    }
}
