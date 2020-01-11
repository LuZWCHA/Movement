package com.nowandfuture.mod.core.selection;

import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class AxisAlignedBBWrap extends AxisAlignedBB {

    private OBBox bounding;
    private float impactTime;
    private Vector3f v;
    private Vector3f axis;
    private Entity impactEntity;
    private boolean setX,setY,setZ;

    public AxisAlignedBBWrap(Entity e,AxisAlignedBB axisAlignedBB, OBBox bounding, float impactTime, Vector3f vector3f, @Nullable Vector3f v) {
        super(0,0,0,0,0,0);
        this.bounding = bounding;
        this.impactTime = impactTime;
        this.v = v;
        this.axis = vector3f;
        this.impactEntity = e;
        setX = setY = setZ = false;
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
            if(v.z == 0){
                setZ = true;
            }
            if(v.y == 0){
                setY = true;
            }

            if(Math.abs(offsetX - v.x) > 0.0001){
                Vector3f axis2 = Vector3f.cross(new Vector3f(1,0,0),axis,new Vector3f());
                if(axis2.lengthSquared() != 0){
                    axis2.normalise();
                    float f = Vector3f.dot(v,axis2);
                    Vector3f v1 = new Vector3f(axis2.x * f,axis2.y * f,axis2.z *f);
                    setXYZ(v1);

                    return checkSmall(v1.x);
                }else{
                    setXYZ(new Vector3f(0,0,0));

                    return 0;
                }
            }else{
                float f = Vector3f.dot(axis,v);
                Vector3f vn = new Vector3f(axis.x * f,axis.y * f,axis.z *f);
                Vector3f v1 = Vector3f.sub(v,vn,new Vector3f());
                setXYZ(v1);

                return checkSmall(v1.x);
            }
        }
    }

    @Override
    public double calculateYOffset(AxisAlignedBB other, double offsetY) {

        if(v == null)
            return calculateXYZOffset(other,offsetY);
        else{
            if(v.x == 0){
                setX = true;
            }
            if(v.z == 0){
                setZ = true;
            }

            if(Math.abs(offsetY - v.y) > 0.0001){
                Vector3f axis2 = Vector3f.cross(new Vector3f(0,1,0),axis,new Vector3f());
                if(axis2.lengthSquared() != 0){
                    axis2.normalise();
                    float f = Vector3f.dot(v,axis2);
                    Vector3f v1 = new Vector3f(axis2.x * f,axis2.y * f,axis2.z *f);

                    setXYZ(v1);

                    return checkSmall(v1.y);
                }else{
                    setXYZ(new Vector3f(0,0,0));
                    return 0;
                }
            }else{
                float f = Vector3f.dot(axis,v);
                Vector3f vn = new Vector3f(axis.x * f,axis.y * f,axis.z *f);

                Vector3f v1 = Vector3f.sub(v,vn,new Vector3f());
                setXYZ(v1);
                return checkSmall(v1.y);
            }

        }
    }

    @Override
    public double calculateZOffset(AxisAlignedBB other, double offsetZ) {
        if(v == null)
            return calculateXYZOffset(other, offsetZ);
        else{
            if(v.x == 0){
                setX = true;
            }
            if(v.y == 0){
                setY = true;
            }

            if(Math.abs(offsetZ - v.z) > 0.0001){
                Vector3f axis2 = Vector3f.cross(new Vector3f(0,0,1),axis,new Vector3f());
                if(axis2.lengthSquared() != 0){
                    axis2.normalise();
                    float f = Vector3f.dot(v,axis2);
                    Vector3f v1 = new Vector3f(axis2.x * f,axis2.y * f,axis2.z *f);
                    setXYZ(v1);

                    return checkSmall(v1.z);
                }else{
                    setXYZ(new Vector3f(0,0,0));
                    return 0;
                }
            }else{
                float f = Vector3f.dot(axis,v);
                Vector3f vn = new Vector3f(axis.x * f,axis.y * f,axis.z *f);
                Vector3f v1 = Vector3f.sub(v,vn,new Vector3f());
                setXYZ(v1);

                return checkSmall(v1.z);
            }
        }
    }

    private float checkSmall(float value){
        if(Math.abs(value) < 0.0001){
            return 0;
        }
        return value;
    }

    private void setXYZ(Vector3f v){
        if(setX){
            this.v.x = v.x;
            impactEntity.setEntityBoundingBox(
                    impactEntity.getEntityBoundingBox().offset(v.x,0,0)
            );
        }
        if(setY){
            this.v.y = v.y;
            impactEntity.setEntityBoundingBox(
                    impactEntity.getEntityBoundingBox().offset(0,v.y,0)
            );
        }
        if(setZ){
            this.v.z = v.z;
            impactEntity.setEntityBoundingBox(
                    impactEntity.getEntityBoundingBox().offset(0,0,v.z)
            );
        }
    }

    private double calculateXYZOffset(AxisAlignedBB other, double offset){
        return offset * impactTime;
    }
}
