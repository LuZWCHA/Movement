package com.nowandfuture.mod.utils.math;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.vecmath.Quat4f;
import java.util.Collection;

public class MathHelper {
    private static final double FACTOR = Math.PI / 180;

    public static class Vec4f {
        float x; float y; float z; float w;
        public Vec4f(){x=0;y=0;z=0;w=0;}
        public Vec4f(float x, float y, float z, float w){this.x = x; this.y = y; this.z = z; this.w = w;}
        public Vec4f(double x, double y, double z, double w){this.x = (float) x; this.y = (float) y; this.z = (float) z; this.w = (float) w;}
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

    public static Quaternion interpolate(Quaternion q1, Quaternion q2, float t) {
        double var4 = q2.x * q1.x + q2.y * q1.y + q2.z * q1.z + q2.w * q1.w;
        if (var4 < 0.0D) {
            q1.x = -q1.x;
            q1.y = -q1.y;
            q1.z = -q1.z;
            q1.w = -q1.w;
            var4 = -var4;
        }

        double var6;
        double var8;
        if (1.0D - var4 > 1.0E-6D) {
            double var10 = Math.acos(var4);
            double var12 = net.minecraft.util.math.MathHelper.sin((float) var10);
            var6 = net.minecraft.util.math.MathHelper.sin((float) ((1.0f - t) * var10)) / var12;
            var8 = net.minecraft.util.math.MathHelper.sin((float) (t * var10)) / var12;
        } else {
            var6 = 1.0D - (double)t;
            var8 = t;
        }

        float w = (float)(var6 * (double)q1.w + var8 * (double)q2.w);
        float x = (float)(var6 * (double)q1.x + var8 * (double)q2.x);
        float y = (float)(var6 * (double)q1.y + var8 * (double)q2.y);
        float z = (float)(var6 * (double)q1.z + var8 * (double)q2.z);

        return new Quaternion(x,y,z,w);
    }

    public static boolean isInCuboid(Vec3i vecIn,Vec3i min,Vec3i max){
        return vecIn.getX() >= min.getX() && vecIn.getX() < max.getX() &&
                vecIn.getY() >= min.getY() && vecIn.getY() < max.getY() &&
                vecIn.getZ() >= min.getZ() && vecIn.getZ() < max.getZ();
    }

    public static Vec3d lerp(float t, Vec3d base, Vec3d next)
    {
        return new Vec3d(
                base.x*(1-t)+next.x*t,
                base.y*(1-t)+next.y*t,
                base.z*(1-t)+next.z*t
        );
    }

    public static double angleTwoVec3(Vec3d a, Vec3d b)
    {
        return Math.acos( clampN1ToP1(a.normalize().dotProduct(b.normalize())) );
    }

    private static double clampN1ToP1(double a)
    {
        return a>1d?1d:(Math.max(a, -1d));
    }

    public static float wrap(float a)
    {
        if(a >  Math.PI)a -= Math.PI*2;
        if(a < -Math.PI)a += Math.PI*2;
        return a;
    }

    public static float lerp(float t, float a1, float a2)
    {
        return a1*(1-t) + a2*t;
    }

    public static Vec3d rotateAroundVector(double x, double y, double z,
                                           double axisx, double axisy, double axisz, float radian)
    {
        radian *= 0.5;
        Vec4f Qsrc = new Vec4f(0,x,y,z);
        Vec4f Q1 = new Vec4f(net.minecraft.util.math.MathHelper.cos(radian), axisx* net.minecraft.util.math.MathHelper.sin(radian), axisy*Math.sin(radian), axisz* net.minecraft.util.math.MathHelper.sin(radian));
        Vec4f Q2 = new Vec4f(net.minecraft.util.math.MathHelper.cos(radian),-axisx*Math.sin(radian),-axisy* net.minecraft.util.math.MathHelper.sin(radian),-axisz* net.minecraft.util.math.MathHelper.sin(radian));

        Vec4f ans = mulQuaternion(mulQuaternion(Q2, Qsrc), Q1);
        return new Vec3d(ans.y, ans.z, ans.w);
    }

    public static Vec3d rotateAroundVector(Vec3d vecIn, double x, double y, double z, float radian)
    {
        radian *= 0.5;
        Vec4f srcQ = new Vec4f(0d,vecIn.x,vecIn.y,vecIn.z);
        Vec4f q1 = new Vec4f(net.minecraft.util.math.MathHelper.cos(radian), x*net.minecraft.util.math.MathHelper.sin(radian), y*net.minecraft.util.math.MathHelper.sin(radian), z*net.minecraft.util.math.MathHelper.sin(radian));
        Vec4f q2 = new Vec4f(net.minecraft.util.math.MathHelper.cos(radian),-x*net.minecraft.util.math.MathHelper.sin(radian),-y*net.minecraft.util.math.MathHelper.sin(radian),-z*net.minecraft.util.math.MathHelper.sin(radian));

        Vec4f ans = mulQuaternion(mulQuaternion(q2, srcQ), q1);

        return new Vec3d(ans.y,ans.z,ans.w);

    }

    public static Vec3d rotateAroundVector(Vec3d rotPos, Vec3d axis, float radian)
    {
        radian *= 0.5;
        Vec4f Qsrc = new Vec4f(0,rotPos.x,rotPos.y,rotPos.z);
        Vec4f Q1 = new Vec4f(net.minecraft.util.math.MathHelper.cos(radian), axis.x*net.minecraft.util.math.MathHelper.sin(radian), axis.y*net.minecraft.util.math.MathHelper.sin(radian), axis.z*net.minecraft.util.math.MathHelper.sin(radian));
        Vec4f Q2 = new Vec4f(net.minecraft.util.math.MathHelper.cos(radian),-axis.x*net.minecraft.util.math.MathHelper.sin(radian),-axis.y*net.minecraft.util.math.MathHelper.sin(radian),-axis.z*net.minecraft.util.math.MathHelper.sin(radian));

        Vec4f ans = mulQuaternion(mulQuaternion(Q2, Qsrc), Q1);
        return new Vec3d(ans.y, ans.z, ans.w);
    }

    private static Vec4f mulQuaternion(Vec4f q1, Vec4f q2)
    {
        return new Vec4f(
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

    public static Vector3f mulQuaternion(Vector3f point, Quaternion quaternion) {
        if (point.x == 0 && point.y == 0 && point.z == 0) {
            return new Vector3f(0, 0, 0);
        } else {
            float w = quaternion.w,x = quaternion.x,y = quaternion.y ,z = quaternion.z;
            float vx = point.x, vy = point.y, vz = point.z;
            float rx = w * w * vx + 2 * y * w * vz - 2 * z * w * vy + x * x * vx + 2 * y * x * vy + 2 * z * x * vz
                    - z * z * vx - y * y * vx;
            float ry = 2 * x * y * vx + y * y * vy + 2 * z * y * vz + 2 * w * z * vx - z * z * vy + w * w * vy
                    - 2 * x * w * vz - x * x * vy;
            float rz = 2 * x * z * vx + 2 * y * z * vy + z * z * vz - 2 * w * y * vx - y * y * vz + 2 * w * x * vy
                    - x * x * vz + w * w * vz;
            return new Vector3f(rx, ry, rz);
        }
    }

    /**
     * @param org the original matrix
     * @param quaternion the quaternion that will be applied in the original matrix
     * @return the matrix after rotation
     */
    public static Matrix4f mulQuaternion(Matrix4f org, Quaternion quaternion) {
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


    /**
     * @param t factor from 0-1
     * @param base the Vector base
     * @param goal the Vector goal
     * @return return the middle state of base and goal
     */
    public static Vec3d slerp(float t, Vec3d base, Vec3d goal)
    {
        float theta = (float) Math.acos(clampN1ToP1(base.dotProduct(goal)));
        if(theta == 0 || theta == 1d)return base;
        double sinTh = net.minecraft.util.math.MathHelper.sin(theta);
        double Pb = net.minecraft.util.math.MathHelper.sin(theta*(1-t));
        double Pg = net.minecraft.util.math.MathHelper.sin(theta*t);
        return new Vec3d(
                (base.x*Pb + goal.x*Pg)/sinTh,
                (base.y*Pb + goal.y*Pg)/sinTh,
                (base.z*Pb + goal.z*Pg)/sinTh
        );
    }

    //x-y-z
    public static Quaternion eulerAnglesToQuaternion(float roll,float pitch,float hdg)
    {
        roll *= FACTOR;
        pitch *= FACTOR;
        hdg *= FACTOR;

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

        roll *= FACTOR;
        pitch *= FACTOR;
        yaw *= FACTOR;

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
        Vector3f v = MathHelper.mulQuaternion(new Vector3f(pos),q);
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

    public static double invSqrt(float x)
    {
        return net.minecraft.util.math.MathHelper.fastInvSqrt(x);
    }

    public static double fastBigPow(double x, int n) {
        if (n < 0) {
            x = 1d / x;
        }
        n = Math.abs(n);
        return fastPow(x, n);
    }

    private static double fastPow(double x, int n) {
        if (n == 0) {
            return 1.0d;
        }
        double half = fastPow(x, n >> 1);
        if ((n & 1) == 0) {
            return half * half;
        } else {
            return half * half * x;
        }
    }

    public static double fastSigmoid(double x){
        return 1/(1+ fastExp(-x));
    }

    public static double fastSoftmax(double x, Collection<Double> collection){
        double sum = 0;
        while (collection.iterator().hasNext())
            sum += Math.exp(collection.iterator().next());
        return fastExp(x) / sum;
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

    private static double[] ExpAdjustment = new double[]{
            1.040389835,
            1.039159306,
            1.037945888,
            1.036749401,
            1.035569671,
            1.034406528,
            1.033259801,
            1.032129324,
            1.031014933,
            1.029916467,
            1.028833767,
            1.027766676,
            1.02671504,
            1.025678708,
            1.02465753,
            1.023651359,
            1.022660049,
            1.021683458,
            1.020721446,
            1.019773873,
            1.018840604,
            1.017921503,
            1.017016438,
            1.016125279,
            1.015247897,
            1.014384165,
            1.013533958,
            1.012697153,
            1.011873629,
            1.011063266,
            1.010265947,
            1.009481555,
            1.008709975,
            1.007951096,
            1.007204805,
            1.006470993,
            1.005749552,
            1.005040376,
            1.004343358,
            1.003658397,
            1.002985389,
            1.002324233,
            1.001674831,
            1.001037085,
            1.000410897,
            0.999796173,
            0.999192819,
            0.998600742,
            0.998019851,
            0.997450055,
            0.996891266,
            0.996343396,
            0.995806358,
            0.995280068,
            0.99476444,
            0.994259393,
            0.993764844,
            0.993280711,
            0.992806917,
            0.992343381,
            0.991890026,
            0.991446776,
            0.991013555,
            0.990590289,
            0.990176903,
            0.989773325,
            0.989379484,
            0.988995309,
            0.988620729,
            0.988255677,
            0.987900083,
            0.987553882,
            0.987217006,
            0.98688939,
            0.98657097,
            0.986261682,
            0.985961463,
            0.985670251,
            0.985387985,
            0.985114604,
            0.984850048,
            0.984594259,
            0.984347178,
            0.984108748,
            0.983878911,
            0.983657613,
            0.983444797,
            0.983240409,
            0.983044394,
            0.982856701,
            0.982677276,
            0.982506066,
            0.982343022,
            0.982188091,
            0.982041225,
            0.981902373,
            0.981771487,
            0.981648519,
            0.981533421,
            0.981426146,
            0.981326648,
            0.98123488,
            0.981150798,
            0.981074356,
            0.981005511,
            0.980944219,
            0.980890437,
            0.980844122,
            0.980805232,
            0.980773726,
            0.980749562,
            0.9807327,
            0.9807231,
            0.980720722,
            0.980725528,
            0.980737478,
            0.980756534,
            0.98078266,
            0.980815817,
            0.980855968,
            0.980903079,
            0.980955475,
            0.981017942,
            0.981085714,
            0.981160303,
            0.981241675,
            0.981329796,
            0.981424634,
            0.981526154,
            0.981634325,
            0.981749114,
            0.981870489,
            0.981998419,
            0.982132873,
            0.98227382,
            0.982421229,
            0.982575072,
            0.982735318,
            0.982901937,
            0.983074902,
            0.983254183,
            0.983439752,
            0.983631582,
            0.983829644,
            0.984033912,
            0.984244358,
            0.984460956,
            0.984683681,
            0.984912505,
            0.985147403,
            0.985388349,
            0.98563532,
            0.98588829,
            0.986147234,
            0.986412128,
            0.986682949,
            0.986959673,
            0.987242277,
            0.987530737,
            0.987825031,
            0.988125136,
            0.98843103,
            0.988742691,
            0.989060098,
            0.989383229,
            0.989712063,
            0.990046579,
            0.990386756,
            0.990732574,
            0.991084012,
            0.991441052,
            0.991803672,
            0.992171854,
            0.992545578,
            0.992924825,
            0.993309578,
            0.993699816,
            0.994095522,
            0.994496677,
            0.994903265,
            0.995315266,
            0.995732665,
            0.996155442,
            0.996583582,
            0.997017068,
            0.997455883,
            0.99790001,
            0.998349434,
            0.998804138,
            0.999264107,
            0.999729325,
            1.000199776,
            1.000675446,
            1.001156319,
            1.001642381,
            1.002133617,
            1.002630011,
            1.003131551,
            1.003638222,
            1.00415001,
            1.004666901,
            1.005188881,
            1.005715938,
            1.006248058,
            1.006785227,
            1.007327434,
            1.007874665,
            1.008426907,
            1.008984149,
            1.009546377,
            1.010113581,
            1.010685747,
            1.011262865,
            1.011844922,
            1.012431907,
            1.013023808,
            1.013620615,
            1.014222317,
            1.014828902,
            1.01544036,
            1.016056681,
            1.016677853,
            1.017303866,
            1.017934711,
            1.018570378,
            1.019210855,
            1.019856135,
            1.020506206,
            1.02116106,
            1.021820687,
            1.022485078,
            1.023154224,
            1.023828116,
            1.024506745,
            1.025190103,
            1.02587818,
            1.026570969,
            1.027268461,
            1.027970647,
            1.02867752,
            1.029389072,
            1.030114973,
            1.030826088,
            1.03155163,
            1.032281819,
            1.03301665,
            1.033756114,
            1.034500204,
            1.035248913,
            1.036002235,
            1.036760162,
            1.037522688,
            1.038289806,
            1.039061509,
            1.039837792,
            1.040618648
    };

    public static double fastExp(double x) {
        long tmp = (long)(1512775 * x + 1072632447);
        int index = (int)(tmp >> 12) & 0xFF;
        return Double.longBitsToDouble(tmp << 32) * ExpAdjustment[index];
    }

}
