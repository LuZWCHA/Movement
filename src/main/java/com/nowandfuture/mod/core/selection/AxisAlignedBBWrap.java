package com.nowandfuture.mod.core.selection;

import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class AxisAlignedBBWrap extends AxisAlignedBB {

    private OBBox bounding;
    private float impactTime;
    private Vector3f v;

    private Vector3f impactAxis;

    private Entity impactEntity;
    private boolean setVFinished = false;
    private AxisAlignedBB org;

    //make up the minecraft's function--Entity#move()[V;AABB;FFF] drop of x,y,z offset when they are zero
    //to see more go to Entity#move()[V;AABB;FFF]
    private boolean checkZeros = false;
    private boolean setX,setY,setZ;

    private Vector3f newV;
    private World world;
    private static float DELTA = 0.001f;

    public AxisAlignedBBWrap(Entity e, OBBox bounding, float impactTime, Vector3f vector3f, @Nullable Vector3f v) {
        super(0,0,0,0,0,0);
        this.bounding = bounding;
        this.impactTime = impactTime;
        this.v = v;
        this.impactAxis = vector3f;
        this.impactEntity = e;
        this.world = impactEntity.world;
        setX = setY = setZ = false;
        org = impactEntity.getEntityBoundingBox().offset(0,0,0);
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
        else {
            checkAABBLimits(other, v);
            setV(v);
            //disable moving by offsetX
            return newV.x;
        }
    }

    @Override
    public double calculateYOffset(AxisAlignedBB other, double offsetY) {

        if(v == null)
            return calculateXYZOffset(other,offsetY);
        else{
            checkAABBLimits(other,v);
            setV(v);

            //disable moving by offsetY
            return newV.y;
        }
    }

    @Override
    public double calculateZOffset(AxisAlignedBB other, double offsetZ) {

        if(v == null)
            return calculateXYZOffset(other, offsetZ);
        else{
            checkAABBLimits(other,v);
            setV(v);
            //disable moving by offsetZ
            return newV.z;
        }
    }

    private float checkSmall(float value){
        if(Math.abs(value) < DELTA){
            return 0;
        }
        return value;
    }

    private void checkAABBLimits(AxisAlignedBB other,Vector3f v){
        if(checkZeros) return;
        if(this.v.x == 0) setX = true;
        if(this.v.y == 0) setY = true;
        if(this.v.z == 0) setZ = true;
        checkZeros = true;
    }

    private void setV(Vector3f v){
        if(v.lengthSquared() == 0) {
            newV = new Vector3f(0,0,0);
            return;
        }

        if(!setVFinished){
            Vector3f v1 = new Vector3f(v);

            float x = v1.x,y = v1.y,z = v1.z;
            AxisAlignedBB other = org.offset(0,0,0);

            List<AxisAlignedBB> list = world.getCollisionBoxes(impactEntity, org.expand(x, y, z));

            for (AxisAlignedBB a :
                    list) {
                if (a instanceof AxisAlignedBBWrap){

                }else{
                    x = (float) a.calculateXOffset(other,x);
                }
            }
            other = other.offset(x,0,0);
            for (AxisAlignedBB a :
                    list) {
                if (a instanceof AxisAlignedBBWrap){

                }else{
                    y = (float) a.calculateYOffset(other,y);
                }
            }
            other = other.offset(0,y,0);

            for (AxisAlignedBB a :
                    list) {
                if (a instanceof AxisAlignedBBWrap){

                }else{
                    z = (float) a.calculateZOffset(other,z);
                }
            }

            int impactCount = 0;
            Vector3f limitX = null,limitY = null,limitZ = null;
            if(x != v1.x){
                impactCount += 1;
                limitX = new Vector3f(0,0,1);
            }
            if(y != v1.y){
                impactCount += 1;
                limitY = new Vector3f(0,1,0);
            }
            if(z != v1.z){
                impactCount += 1;
                limitZ = new Vector3f(1,0,0);
            }

            if(impactCount > 2){
                v1 = new Vector3f(0,0,0);
            }

            if(impactCount == 0) {
                if(impactAxis.lengthSquared() != 0)
                    subVOnAxis(v1, impactAxis.normalise(), 1);
                else
                    v1 = new Vector3f(0,0,0);
            } else if(impactCount == 2){
                if(impactAxis.equals(new Vector3f(1,0,0)) && limitX != null){
                    if(limitY != null){
                        v1.x = 0;
                        v1.y = 0;
                    }else{
                        v1.x = 0;
                        v1.z = 0;
                    }
                }else if(impactAxis.equals(new Vector3f(0,1,0)) && limitY != null){
                    if(limitZ != null){
                        v1.z = 0;
                        v1.y = 0;
                    }else{
                        v1.x = 0;
                        v1.y = 0;
                    }
                }else if(impactAxis.equals(new Vector3f(0,0,1)) && limitZ != null){
                    if(limitX != null){
                        v1.x = 0;
                        v1.z = 0;
                    }else{
                        v1.z = 0;
                        v1.y = 0;
                    }
                }else{
                    v1 = new Vector3f(0,0,0);
                }
            }else if(!impactAxis.equals(limitZ) && limitZ != null){
                Vector3f axis2 = Vector3f.cross(limitZ, impactAxis,new Vector3f());
                if(axis2.lengthSquared() != 0){
                    axis2.normalise();
                    float f = Vector3f.dot(v,axis2);
                    v1 = new Vector3f(axis2.x * f,axis2.y * f,axis2.z *f);
                }else {
                    v1 = new Vector3f(0,0,0);
                }
            }else if(!impactAxis.equals(limitY) && limitY != null){
                Vector3f axis2 = Vector3f.cross(limitY, impactAxis,new Vector3f());
                if(axis2.lengthSquared() != 0){
                    axis2.normalise();
                    float f = Vector3f.dot(v,axis2);
                    v1 = new Vector3f(axis2.x * f,axis2.y * f,axis2.z *f);
                }else {
                    v1 = new Vector3f(0,0,0);
                }
            }else if(!impactAxis.equals(limitX) && limitX != null){
                Vector3f axis2 = Vector3f.cross(limitX, impactAxis,new Vector3f());
                if(axis2.lengthSquared() != 0){
                    axis2.normalise();
                    float f = Vector3f.dot(v,axis2);
                    v1 = new Vector3f(axis2.x * f,axis2.y * f,axis2.z *f);
                }else {
                    v1 = new Vector3f(0,0,0);
                }
            }else{
                v1 = new Vector3f(0,0,0);
            }

            if(setX)
                impactEntity.setEntityBoundingBox(impactEntity.getEntityBoundingBox().offset(checkSmall(v1.x),0,0));
            if(setY)
                impactEntity.setEntityBoundingBox(impactEntity.getEntityBoundingBox().offset(0,checkSmall(v1.y),0));
            if(setZ)
                impactEntity.setEntityBoundingBox(impactEntity.getEntityBoundingBox().offset(0,0,checkSmall(v1.z)));

            newV = new Vector3f(v1.x,v1.y,v1.z);

            setVFinished = true;
        }
    }

    private void subVOnAxisX(Vector3f v,float factor){
        subVOnAxis(v,new Vector3f(1,0,0),factor);
    }

    private void subVOnAxisY(Vector3f v,float factor){
        subVOnAxis(v,new Vector3f(0,1,0),factor);
    }

    private void subVOnAxisZ(Vector3f v,float factor){
        subVOnAxis(v,new Vector3f(0,0,1),factor);
    }

    private void subVOnAxis(Vector3f v,Vector3f axis,float factor){
        float f = Vector3f.dot(axis,v);
        Vector3f vn = new Vector3f(axis.x * f * factor, axis.y * f * factor, axis.z * f * factor);
        Vector3f v1 = Vector3f.sub(v,vn,new Vector3f());
        v.set(v1.x,v1.y,v1.z);
    }

    private double calculateXYZOffset(AxisAlignedBB other, double offset){
        return offset * impactTime;
    }
}
