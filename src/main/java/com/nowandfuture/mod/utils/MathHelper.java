package com.nowandfuture.mod.utils;

import com.sun.javafx.geom.Vec3f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.util.vector.Quaternion;

import javax.vecmath.Quat4f;

public class MathHelper {

    public static class Vec4{
        float x; float y; float z; float w;
        public Vec4(){x=0;y=0;z=0;w=0;}
        public Vec4(float x,float y,float z,float w){this.x = x; this.y = y; this.z = z; this.w = w;}
        public Vec4(double x,double y,double z,double w){this.x = (float) x; this.y = (float) y; this.z = (float) z; this.w = (float) w;}
    }


    public static Quat4f to(final Quaternion from){
        return new Quat4f(from.x,from.y,from.z,from.w);
    }

    public static Quaternion to(final Quat4f from){
        return new Quaternion(from.x,from.y,from.z,from.w);
    }

    public static void to(Quaternion from,Quat4f to){
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;
        to.w = from.w;
    }
    public static void to(Quat4f from,Quaternion to){
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;
        to.w = from.w;
    }

    public static boolean isInCuboid(Vec3i vecIn,Vec3i min,Vec3i max){
        return vecIn.getX() >= min.getX() && vecIn.getX() < max.getX() &&
                vecIn.getY() >= min.getY() && vecIn.getY() < max.getY() &&
                vecIn.getZ() >= min.getZ() && vecIn.getZ() < max.getZ();
    }

    public static Vec3d Lerp(float t, Vec3d base, Vec3d next)
    {
        return new Vec3d(
                base.x*(1-t)+next.x*t,
                base.y*(1-t)+next.y*t,
                base.z*(1-t)+next.z*t
        );
    }

    public static double angleTwoVec3(Vec3d a, Vec3d b)
    {
        return Math.acos( clamp(a.normalize().dotProduct(b.normalize())) );
    }

    public static double clamp(double a)
    {
        return a>1d?1d:(a<-1d?-1d:a);
    }

    public static float wrap(float a)
    {
        if(a >  Math.PI)a -= Math.PI*2;
        if(a < -Math.PI)a += Math.PI*2;
        return a;
    }

    public static float Lerp(float t, float a1, float a2)
    {
        return a1*(1-t) + a2*t;
    }

    public static Vec3d rotateAroundVector(double x, double y, double z,
                                           double axisx, double axisy, double axisz, double radian)
    {
        radian *= 0.5;
        Vec4 Qsrc = new Vec4(0,x,y,z);
        Vec4 Q1 = new Vec4(Math.cos(radian), axisx*Math.sin(radian), axisy*Math.sin(radian), axisz*Math.sin(radian));
        Vec4 Q2 = new Vec4(Math.cos(radian),-axisx*Math.sin(radian),-axisy*Math.sin(radian),-axisz*Math.sin(radian));

        Vec4 ans = MulQuaternion(MulQuaternion(Q2, Qsrc), Q1);
        return new Vec3d(ans.y, ans.z, ans.w);
    }

    public static Vec3d rotateAroundVector(Vec3d vecIn, double x, double y, double z, double radian)
    {
        radian *= 0.5;
        Vec4 Qsrc = new Vec4(0d,vecIn.x,vecIn.y,vecIn.z);
        Vec4 Q1 = new Vec4(Math.cos(radian), x*Math.sin(radian), y*Math.sin(radian), z*Math.sin(radian));
        Vec4 Q2 = new Vec4(Math.cos(radian),-x*Math.sin(radian),-y*Math.sin(radian),-z*Math.sin(radian));

        Vec4 ans = MulQuaternion(MulQuaternion(Q2, Qsrc), Q1);

        return new Vec3d(ans.y,ans.z,ans.w);

    }

    public static Vec3d rotateAroundVector(Vec3d rotPos, Vec3d axis, double radian)
    {
        radian *= 0.5;
        Vec4 Qsrc = new Vec4(0,rotPos.x,rotPos.y,rotPos.z);
        Vec4 Q1 = new Vec4(Math.cos(radian), axis.x*Math.sin(radian), axis.y*Math.sin(radian), axis.z*Math.sin(radian));
        Vec4 Q2 = new Vec4(Math.cos(radian),-axis.x*Math.sin(radian),-axis.y*Math.sin(radian),-axis.z*Math.sin(radian));

        Vec4 ans = MulQuaternion(MulQuaternion(Q2, Qsrc), Q1);
        return new Vec3d(ans.y, ans.z, ans.w);
    }
    private static Vec4 MulQuaternion(Vec4 q1, Vec4 q2)
    {
        return new Vec4(
                q1.x*q2.x - (q1.y*q2.y+q1.z*q2.z+q1.w*q2.w),
                q1.x*q2.y + q2.x*q1.y + (q1.z*q2.w - q1.w*q2.z),
                q1.x*q2.z + q2.x*q1.z + (q1.w*q2.y - q1.y*q2.w),
                q1.x*q2.w + q2.x*q1.w + (q1.y*q2.z - q1.z*q2.y)
        );
    }

    public static Vec3d Slerp(float t, Vec3d base, Vec3d goal)
    {
        double theta = Math.acos(clamp(base.dotProduct(goal)));
        if(theta == 0 || theta == 1d)return base;
        double sinTh = Math.sin(theta);
        double Pb = Math.sin(theta*(1-t));
        double Pg = Math.sin(theta*t);
        return new Vec3d(
                (base.x*Pb + goal.x*Pg)/sinTh,
                (base.y*Pb + goal.y*Pg)/sinTh,
                (base.z*Pb + goal.z*Pg)/sinTh
        );
    }

    public static boolean approximate(double a,double b,double accuracy){
        return Math.abs(a - b) < accuracy;
    }

    public static int getIntHighBits(int data,int bit){
        return data >>> (32 - bit);
    }

    public static int getIntLowBits(int data,int bit){
        int mark = 0xFFFFFFFF;
        mark >>>= (32 - bit);
        return data & mark;
    }

    public static int getIntMiddleBits(int data,int bitStart ,int length){
        return getIntHighBits(data << bitStart,length);
    }
    
}
