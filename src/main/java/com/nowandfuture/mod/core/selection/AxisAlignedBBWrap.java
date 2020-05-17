package com.nowandfuture.mod.core.selection;

import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class AxisAlignedBBWrap extends AxisAlignedBB {

    private OBBox bounding;
    private float impactTime;
    private Vector3f v;
    private int count =0;

    private List<Vector3f> impactAxises;

    private Entity impactEntity;
    private boolean setVFinished = false;
    private AxisAlignedBB org;

    //make up the minecraft's function--Entity#move()[V;AABB;FFF] drop of x,y,z offset when they are zero
    //to see more, go to Entity#move()[V;AABB;FFF]
    private boolean checkZeros = false;
    private boolean setX,setY,setZ;

    private Vector3f newV;
    private World world;
    private static float DELTA = 1E-8f;

    public AxisAlignedBBWrap(Entity e, float impactTime,  @Nullable Vector3f v) {
        super(0,0,0,0,0,0);
        this.impactTime = impactTime;
        this.v = v;
        this.impactEntity = e;
        this.world = impactEntity.world;
        setX = setY = setZ = false;
        org = impactEntity.getEntityBoundingBox().offset(0,0,0);
        this.impactAxises = new ArrayList<>();
    }

    public float getImpactTime() {
        return impactTime;
    }

    public void setImpactTime(float impactTime) {
        if(impactTime < this.impactTime)
            this.impactTime = impactTime;
    }

    public void pushAxis(Vector3f impactAxis){
        for (Vector3f axis :
                impactAxises) {
            if (impactAxis.equals(axis)) {
                return;
            }
        }
        impactAxises.add(impactAxis);
    }

    public OBBox getBounding() {
        return bounding;
    }

    @Override
    public double calculateXOffset(AxisAlignedBB other, double offsetX) {

        if(v == null)
            return calculateXYZOffset(other,offsetX);
        else {
            checkAABBLimits();
            setX = false;
            setV(v);
            //disable moving by offsetX
            return checkSmall(newV.x);
        }
    }

    @Override
    public double calculateYOffset(AxisAlignedBB other, double offsetY) {

        if(v == null)
            return calculateXYZOffset(other,offsetY);
        else{
            checkAABBLimits();
            setY = false;
            setV(v);
            //disable moving by offsetY
            return checkSmall(newV.y);
        }
    }

    @Override
    public double calculateZOffset(AxisAlignedBB other, double offsetZ) {

        if(v == null)
            return calculateXYZOffset(other, offsetZ);
        else{
            checkAABBLimits();
            setZ = false;
            setV(v);
            //disable moving by offsetZ
            return checkSmall(newV.z);
        }
    }

    private float checkSmall(float value){
        if(Math.abs(value) < DELTA){
            return 0;
        }
        return value;
    }

    private void checkAABBLimits(){
        if(checkZeros) return;
        if(this.v.x == 0) setX = true;
        if(this.v.y == 0) setY = true;
        if(this.v.z == 0) setZ = true;
        checkZeros = true;
    }

    private void setV(Vector3f v){
        if(impactTime < 0 || impactTime > 1){
            newV = new Vector3f(v.x ,v.y,v.z);
            return;
        }

        if(v.lengthSquared() == 0) {
            newV = new Vector3f(0,0,0);
            return;
        }

        if(!setVFinished){

            Vector3f v1 = new Vector3f(v);

            int impactCount = impactAxises.size();

            if(impactCount > 2){
                v1 = new Vector3f(0,0,0);
            }else if(impactCount == 1) {
                Vector3f impactAxis = impactAxises.get(0);
                if(impactAxis.lengthSquared() != 0) {
                    subVOnAxis(v1, impactAxis, 1);
                }
            } else if(impactCount == 2){
                Vector3f axis2 = Vector3f.cross(impactAxises.get(0),
                        impactAxises.get(1), new Vector3f());
                if (axis2.lengthSquared() != 0) {
                    axis2.normalise();
                    float f = Vector3f.dot(v, axis2);
                    v1 = new Vector3f(axis2.x * f, axis2.y * f, axis2.z * f);
                }else{
                    v1 = new Vector3f(0,0,0);
                }
            }else{
//                v1 = new Vector3f(0,0,0);
//                v1 = new Vector3f(x,y,z);
            }

            float x = v1.x,y = v1.y,z = v1.z;
            AxisAlignedBB other = org.offset(0,0,0);

            List<AxisAlignedBB> list = world.getCollisionBoxes(impactEntity, impactEntity.getEntityBoundingBox().expand(x, y, z));

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

            Vector3f limitX,limitY,limitZ;

            if(x != v1.x){
                limitX = new Vector3f(1,0,0);
                pushAxis(limitX);
            }
            if(y != v1.y){
                limitY = new Vector3f(0,1,0);
                pushAxis(limitY);
            }
            if(z != v1.z){
                limitZ = new Vector3f(0,0,1);
                pushAxis(limitZ);
            }

            impactCount = impactAxises.size();
            System.out.println(impactCount);
            v1 = new Vector3f(v);

            if(impactCount > 2){
                v1 = new Vector3f(0,0,0);
            }else if(impactCount == 1) {
                Vector3f impactAxis = impactAxises.get(0);
                if(impactAxis.lengthSquared() != 0) {
                    subVOnAxis(v1, impactAxis, 1);
                }
            } else if(impactCount == 2){
                Vector3f axis2 = Vector3f.cross(impactAxises.get(0),
                        impactAxises.get(1), new Vector3f());
                if (axis2.lengthSquared() != 0) {
                    axis2.normalise();
                    float f = Vector3f.dot(v1, axis2);
                    v1 = new Vector3f(axis2.x * f, axis2.y * f, axis2.z * f);
                }else{
                    v1 = new Vector3f(0,0,0);
                }
            }else{
                v1 = new Vector3f(0,0,0);
//                v1 = new Vector3f(x,y,z);
            }

            newV = new Vector3f(v1.x,v1.y,v1.z);
//            System.out.println(newV.toString());

//            impactEntity.setEntityBoundingBox(org);

//            AxisAlignedBB axisAlignedBB = impactEntity.getEntityBoundingBox().expand(newV.x,newV.y,newV.z);
//            List<AxisAlignedBB> testList = world.getCollisionBoxes(impactEntity,
//                   axisAlignedBB);
//            other = org.offset(0,0,0);
//            x = newV.x;y = newV.y;z = newV.z;
//            if(testList.isEmpty()){
//                if (y != 0.0D)
//                {
//                    int k = 0;
//
//                    for (int l = testList.size(); k < l; ++k)
//                    {
//                        AxisAlignedBB axisAlignedBB1 = testList.get(k);
//                        if(axisAlignedBB1 instanceof AxisAlignedBBWrap) {
//                            float y1 = newV.y * ((AxisAlignedBBWrap) axisAlignedBB1).getImpactTime();
//                            if((Math.abs(y1) < Math.abs(y))) y = y1;
//                        }else {
//                            y = (float) axisAlignedBB1.calculateYOffset(other, y);
//                        }
//                    }
//
//                    other = other.offset(0,y,0);
//                }
//
//                if (x != 0.0D)
//                {
//                    int j5 = 0;
//
//                    for (int l5 = testList.size(); j5 < l5; ++j5)
//                    {
//                        AxisAlignedBB axisAlignedBB1 = testList.get(j5);
//                        if(axisAlignedBB1 instanceof AxisAlignedBBWrap) {
//                            float x1 = newV.x * ((AxisAlignedBBWrap) axisAlignedBB1).getImpactTime();
//                            if((Math.abs(x1) < Math.abs(x))) x = x1;
//                        }else {
//                            x = (float) axisAlignedBB1.calculateXOffset(other, x);
//                        }
//                    }
//
//                    if (x != 0.0D)
//                    {
//                        other = other.offset(x,0,0);
//                    }
//                }
//
//                if (z != 0.0D)
//                {
//                    int k5 = 0;
//
//                    for (int i6 = testList.size(); k5 < i6; ++k5)
//                    {
//                        AxisAlignedBB axisAlignedBB1 = testList.get(k5);
//                        if(axisAlignedBB1 instanceof AxisAlignedBBWrap) {
//                            float z1 = newV.z * ((AxisAlignedBBWrap) axisAlignedBB1).getImpactTime();
//                            if(Math.abs(z1) < Math.abs(z)) z = z1;
//                        }else {
//                            z = (float) axisAlignedBB1.calculateZOffset(other, z);
//                        }
//                    }
//
//                    if (z != 0.0D)
//                    {
//                        other = other.offset(0,0,z);
//                    }
//                }
//
//                newV.set(x,y,z);
//            }else{
//                newV.set(0,0,0);
//            }

            if(newV.y >= v.y){
                impactEntity.onGround = false;
            }
//            if(impactEntity.onGround && newV.y != v.y && v.y < 0) {
//
//                if(impactEntity.stepHeight > 0) {
//                    List cl = world.getCollisionBoxes(impactEntity,
//                            impactEntity.getEntityBoundingBox().expand(newV.x,impactEntity.stepHeight,newV.z));
//                    if(!cl.isEmpty())
//                        impactEntity.stepHeight = 0;
//                }
//            }
//            impactEntity.stepHeight = 0;
            impactAxises.clear();

            if(newV.x != v.x){
                impactEntity.motionX = 0;
                impactEntity.velocityChanged = true;
            }
            if(newV.y != v.y){
                impactEntity.motionY = 0;
                impactEntity.velocityChanged = true;
            }
            if(newV.z != v.z){
                impactEntity.motionZ = 0;
                impactEntity.velocityChanged = true;
            }



            if(setX)
                impactEntity.setEntityBoundingBox(org.offset(newV.x,0,0));
            if(setY)
                impactEntity.setEntityBoundingBox(org.offset(0,newV.y,0));
            if(setZ)
                impactEntity.setEntityBoundingBox(org.offset(0,0,newV.z));

            setVFinished = true;
        }
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
