package com.nowandfuture.mod.utils;

import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Quaternion;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;


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

    public static Quaternion interpolate(Quaternion var1, Quaternion var2, float var3) {
        double var4 = (double)(var2.x * var1.x + var2.y * var1.y + var2.z * var1.z + var2.w * var1.w);
        if (var4 < 0.0D) {
            var1.x = -var1.x;
            var1.y = -var1.y;
            var1.z = -var1.z;
            var1.w = -var1.w;
            var4 = -var4;
        }

        double var6;
        double var8;
        if (1.0D - var4 > 1.0E-6D) {
            double var10 = Math.acos(var4);
            double var12 = Math.sin(var10);
            var6 = Math.sin((1.0D - (double)var3) * var10) / var12;
            var8 = Math.sin((double)var3 * var10) / var12;
        } else {
            var6 = 1.0D - (double)var3;
            var8 = (double)var3;
        }

        float w = (float)(var6 * (double)var1.w + var8 * (double)var2.w);
        float x = (float)(var6 * (double)var1.x + var8 * (double)var2.x);
        float y = (float)(var6 * (double)var1.y + var8 * (double)var2.y);
        float z = (float)(var6 * (double)var1.z + var8 * (double)var2.z);

        return new Quaternion(x,y,z,w);
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

    public static Quaternion inverse(Quaternion quaternion) {
        float length = quaternion.lengthSquared();
        if (length != 1f && length != 0f) {
            length = (float) (1.0 / Math.sqrt(length));
            return new Quaternion(-quaternion.x * length, -quaternion.y * length, -quaternion.z * length, quaternion.w * length);
        }
        return new Quaternion(-quaternion.x, -quaternion.y, -quaternion.z, quaternion.w);
    }

    public static Vector3f mult(Vector3f v, Quaternion quaternion) {
        if (v.x == 0 && v.y == 0 && v.z == 0) {
            return new Vector3f(0, 0, 0);
        } else {
            float w = quaternion.w,x = quaternion.x,y = quaternion.y ,z = quaternion.z;
            float vx = v.x, vy = v.y, vz = v.z;
            float rx = w * w * vx + 2 * y * w * vz - 2 * z * w * vy + x * x * vx + 2 * y * x * vy + 2 * z * x * vz
                    - z * z * vx - y * y * vx;
            float ry = 2 * x * y * vx + y * y * vy + 2 * z * y * vz + 2 * w * z * vx - z * z * vy + w * w * vy
                    - 2 * x * w * vz - x * x * vy;
            float rz = 2 * x * z * vx + 2 * y * z * vy + z * z * vz - 2 * w * y * vx - y * y * vz + 2 * w * x * vy
                    - x * x * vz + w * w * vz;
            return new Vector3f(rx, ry, rz);
        }
    }

    public static Matrix4f mult(Matrix4f org, Quaternion quaternion) {
        float x = quaternion.x;
        float y = quaternion.y;
        float z = quaternion.z;
        float w = quaternion.w;

        Matrix4f right = new Matrix4f();
//                [1.0f - 2.0f*y*y - 2.0f*z*z, 2.0f*x*y - 2.0f*z*w, 2.0f*x*z + 2.0f*y*w, 0.0f,
//                2.0f*x*y + 2.0f*z*w, 1.0f - 2.0f*x*x - 2.0f*z*z, 2.0f*y*z - 2.0f*x*w, 0.0f,
//                2.0f*x*z - 2.0f*y*w, 2.0f*y*z + 2.0f*x*w, 1.0f - 2.0f*x*x - 2.0f*y*y, 0.0f,
//                0.0f, 0.0f, 0.0f, 1.0f] = M^T
        right.m00 = 1.0f - 2.0f*y*y - 2.0f*z*z;
        right.m01 = 2.0f*x*y + 2.0f*z*w;
        right.m02 = 2.0f*x*z - 2.0f*y*w;
        right.m03 = 0;
        right.m10 = 2.0f*x*y - 2.0f*z*w;
        right.m11 = 1.0f - 2.0f*x*x - 2.0f*z*z;
        right.m12 = 2.0f*y*z + 2.0f*x*w;
        right.m13 = 0;
        right.m20 = 2.0f*x*z + 2.0f*y*w;
        right.m21 = 2.0f*y*z - 2.0f*x*w;
        right.m22 = 1.0f - 2.0f*x*x - 2.0f*y*y;
        right.m23 = 0;
        right.m30 = 0;
        right.m31 = 0;
        right.m32 = 0;
        right.m33 = 1;

        return Matrix4f.mul(org,right,org);
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

    //x-y-z
    public static Quaternion eulerAnglesToQuaternion(float roll,float pitch,float hdg)
    {
        final double factor = Math.PI / 180;
        roll *= factor;
        pitch *= factor;
        hdg *= factor;

        float cosRoll = net.minecraft.util.math.MathHelper.cos(roll * 0.5f);
        float sinRoll = net.minecraft.util.math.MathHelper.sin(roll * 0.5f);

        float cosPitch = net.minecraft.util.math.MathHelper.cos(pitch * 0.5f);
        float sinPitch = net.minecraft.util.math.MathHelper.sin(pitch * 0.5f);

        float cosHeading = net.minecraft.util.math.MathHelper.cos(hdg * 0.5f);
        float sinHeading = net.minecraft.util.math.MathHelper.sin(hdg * 0.5f);

        float q0 = cosRoll * cosPitch * cosHeading + sinRoll * sinPitch * sinHeading;
        float q1 = sinRoll * cosPitch * cosHeading - cosRoll * sinPitch * sinHeading;
        float q2 = cosRoll * sinPitch * cosHeading + sinRoll * cosPitch * sinHeading;
        float q3 = cosRoll * cosPitch * sinHeading - sinRoll * sinPitch * cosHeading;

        return new Quaternion(q1,q2,q3,q0);
    }

    //x-y-z
    public static Vector3f quaternionToEulerAngles(Quaternion quaternion)
    {
        float q0 = quaternion.w;
        float q1 = quaternion.x;
        float q2 = quaternion.y;
        float q3 = quaternion.z;

        float roll = (float) net.minecraft.util.math.MathHelper.atan2(2.f * (q2 * q3 + q0 * q1), q0 * q0 - q1 * q1 - q2 * q2 + q3 * q3);
        float pitch = (float) Math.asin(2.f * (q0 * q2 - q1 * q3));
        float yaw = (float) net.minecraft.util.math.MathHelper.atan2(2.f * (q1 * q2 + q0 * q3), q0 * q0 + q1 * q1 - q2 * q2 - q3 * q3);

        final double factor =  180 / Math.PI;
        roll *= factor;
        pitch *= factor;
        yaw *= factor;

        return new Vector3f(roll,pitch,yaw);
    }

    //.       .
    //  . 2 .
    // 3  x  1
    //  . 0 .
    //.       .
    //|---------->x+
    //|
    //|
    //↓
    //z+
    public static int getRelativePos2D(BlockPos fixedPos,BlockPos observerPos,Quaternion q){
        BlockPos pos = observerPos.subtract(fixedPos);
        Vector3f v = MathHelper.mult(new Vector3f(pos),q);
        int x = (int) v.getX();
        int z = (int) v.getZ();
        int y = (int) v.getY();
        if(z <= x && z > -x){
            return y > 0 ? 1 : 3;
        }else if(z >= x && z < -x){
            return y <= 0 ? 1 : 3;
        }else if(x >= -z && x < z){
            return y > 0 ? 0 : 2;
        }else {
            return y <= 0 ? 0 : 2;
        }
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
