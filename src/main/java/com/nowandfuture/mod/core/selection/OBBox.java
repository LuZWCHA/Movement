package com.nowandfuture.mod.core.selection;

import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;
import com.nowandfuture.mod.utils.math.Vector4f;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

// TODO: 2020/2/2 Optimize
public class OBBox {
    private Vector3f xyz000;
    private Vector3f xyz001;
    private Vector3f xyz010;
    private Vector3f xyz011;
    private Vector3f xyz100;
    private Vector3f xyz101;
    private Vector3f xyz110;
    private Vector3f xyz111;

    public static class Facing{
        private Vector3f v0;
        private Vector3f v1;
        private Vector3f v2;
        private Vector3f v3;

        private Vector3f directionVector;


        /**
         * @param facing The facing wants to be created
         * @param aabbounding The OBBOX must be a AABBOX-Create so the every point will map to minX minY minZ ...
         * @return The Facing contain the OBB vex
         */
        public static Facing createFromAABBounding(EnumFacing facing,OBBox aabbounding){
            switch (facing){
                case DOWN:
                    return new Facing(aabbounding.xyz000,aabbounding.xyz001,aabbounding.xyz101,aabbounding.xyz100,facing.getDirectionVec());
                case UP:
                    return new Facing(aabbounding.xyz010,aabbounding.xyz011,aabbounding.xyz111,aabbounding.xyz110,facing.getDirectionVec());
                case EAST:
                    return new Facing(aabbounding.xyz100,aabbounding.xyz110,aabbounding.xyz111,aabbounding.xyz101,facing.getDirectionVec());
                case WEST:
                    return new Facing(aabbounding.xyz000,aabbounding.xyz010,aabbounding.xyz011,aabbounding.xyz001,facing.getDirectionVec());
                case NORTH:
                    return new Facing(aabbounding.xyz000,aabbounding.xyz100,aabbounding.xyz110,aabbounding.xyz010,facing.getDirectionVec());
                case SOUTH:
                    return new Facing(aabbounding.xyz001,aabbounding.xyz101,aabbounding.xyz111,aabbounding.xyz011,facing.getDirectionVec());
            }
            return null;
        }

        private Facing(Vector3f v0,Vector3f v1,Vector3f v2,Vector3f v3){
            this(v0,v1,v2,v3,null);
        }

        private Facing(Vector3f v0,Vector3f v1,Vector3f v2,Vector3f v3,Vec3i facing){
            this.v0 = new Vector3f(v0);
            this.v1 = new Vector3f(v1);
            this.v2 = new Vector3f(v2);
            this.v3 = new Vector3f(v3);

            if(facing != null)
                this.directionVector = new Vector3f(facing.getX(),facing.getY(),facing.getZ());
            else{
                final float a = (v2.y-v1.y)*(v3.z-v1.z)-(v2.z-v1.z)*(v3.y-v1.y);
                final float b = (v2.z-v1.z)*(v3.x-v1.x)-(v2.x-v1.x)*(v3.z-v1.z);
                final float c = (v2.x-v1.x)*(v3.y-v1.y)-(v2.y-v1.y)*(v3.x-v1.x);

                this.directionVector = new Vector3f(a,b,c);
            }
        }


        /**
         * @param output the out put of the Face's geometry parameters,the form:ax+by+cz+d=0
         */
        public void getPlane(float[] output){
            final float a = (v2.y - v1.y) * (v3.z - v1.z) - (v2.z -v1.z) * (v3.y - v1.y);
            final float b = (v2.z - v1.z) * (v3.x - v1.x) - (v2.x - v1.x) * (v3.z - v1.z);
            final float c = (v2.x - v1.x) * (v3.y - v1.y) - (v2.y - v1.y) * (v3.x - v1.x);
            final float d = - (a * v1.x + b * v1.y + c * v1.z);

            output = new float[]{a,b,c,d};
        }

        //not finished
        public void mulMatrix(final Matrix4f matrix4f){
            transform1(v0,matrix4f);
            transform1(v1,matrix4f);
            transform1(v2,matrix4f);
            transform1(v3,matrix4f);
            transformVector(matrix4f,directionVector,directionVector);
        }

        public boolean isFrontBy(Vector3f lookVec){
            return dotDirectionVec(lookVec) < 0;
        }

        public float dotDirectionVec(Vector3f lookVec){
            return Vector3f.dot(lookVec,directionVector);
        }

        public Vector3f getV0() {
            return v0;
        }

        public Vector3f getV1() {
            return v1;
        }

        public Vector3f getV2() {
            return v2;
        }

        public Vector3f getV3() {
            return v3;
        }

        public Vector3f getDirectionVector() {
            return directionVector;
        }

        public Vector3f getCenter(){
            Vector3f res = Vector3f.add(v0,v2,new Vector3f());
            return (Vector3f) res.scale(0.5f);
        }
    }

    //unused
    @Deprecated
    public enum ROOM{
        INSIDE(0x3f),

        UP(0x3d),
        DOWN(0x3e),
        NORTH(0x3b),
        SOUTH(0x3c),
        WEST(0x2f),
        EAST(0x1f),

        UP_NORTH(0x3a),
        UP_SOUTH(0x36),
        UP_WEST(0x2e),
        UP_EAST(0x1e),

        DOWN_NORTH(0x39),
        DOWN_SOUTH(0x35),
        DOWN_WEST(0x2d),
        DOWN_EAST(0x1d),

        EAST_SOUTH(0x16),
        SOUTH_WEST(0x26),
        WEST_NORTH(0x2b),
        NORTH_EAST(0x1b),

        UP_EAST_SOUTH(0x15),
        UP_SOUTH_WEST(0x25),
        UP_WEST_NORTH(0x2a),
        UP_NORTH_EAST(0x1a),
        DOWN_EAST_SOUTH(0x14),
        DOWN_SOUTH_WEST(0x24),
        DOWN_WEST_NORTH(0x29),
        DOWN_NORTH_EAST(0x19);

        public final int mark;

        ROOM(int mark) {
            this.mark = mark;
        }
    }

    public OBBox(AxisAlignedBB axisAlignedBB){
        this(new Vector3f((float) axisAlignedBB.minX,(float) axisAlignedBB.minY,(float) axisAlignedBB.minZ),
                new Vector3f((float) axisAlignedBB.minX,(float) axisAlignedBB.minY,(float) axisAlignedBB.maxZ),
                new Vector3f((float) axisAlignedBB.minX,(float) axisAlignedBB.maxY,(float) axisAlignedBB.minZ),
                new Vector3f((float) axisAlignedBB.minX,(float) axisAlignedBB.maxY,(float) axisAlignedBB.maxZ),
                new Vector3f((float) axisAlignedBB.maxX,(float) axisAlignedBB.minY,(float) axisAlignedBB.minZ),
                new Vector3f((float) axisAlignedBB.maxX,(float) axisAlignedBB.minY,(float) axisAlignedBB.maxZ),
                new Vector3f((float) axisAlignedBB.maxX,(float) axisAlignedBB.maxY,(float) axisAlignedBB.minZ),
                new Vector3f((float) axisAlignedBB.maxX,(float) axisAlignedBB.maxY,(float) axisAlignedBB.maxZ)
        );
    }

    public OBBox(Vector3f xyz000, Vector3f xyz001, Vector3f xyz010, Vector3f xyz011, Vector3f xyz100, Vector3f xyz101, Vector3f xyz110, Vector3f xyz111) {
        this.xyz000 = new Vector3f(xyz000);
        this.xyz001 = new Vector3f(xyz001);
        this.xyz010 = new Vector3f(xyz010);
        this.xyz011 = new Vector3f(xyz011);
        this.xyz100 = new Vector3f(xyz100);
        this.xyz101 = new Vector3f(xyz101);
        this.xyz110 = new Vector3f(xyz110);
        this.xyz111 = new Vector3f(xyz111);
    }

    public OBBox(OBBox other) {
        this(other.xyz000,other.xyz001,other.xyz010,other.xyz011,other.xyz100,other.xyz101,other.xyz110,other.xyz111);
    }

    public Vector3f getCenter(){
        return new Vector3f((xyz000.x + xyz111.x) * .5f,(xyz000.y + xyz111.y) * .5f,(xyz000.z + xyz111.z) * .5f);
    }

    public Vector3f[] asArray(){
        return new Vector3f[]{xyz000,xyz001,xyz010,xyz011,xyz100,xyz101,xyz110,xyz111};
    }

    public void mulMatrix(final Matrix4f matrix4f){
        transform1(xyz000,matrix4f);
        transform1(xyz001,matrix4f);
        transform1(xyz010,matrix4f);
        transform1(xyz011,matrix4f);
        transform1(xyz100,matrix4f);
        transform1(xyz101,matrix4f);
        transform1(xyz110,matrix4f);
        transform1(xyz111,matrix4f);
    }

    public OBBox transform(final Matrix4f matrix4f){
        return new OBBox(
                transform(xyz000,matrix4f),
                transform(xyz001,matrix4f),
                transform(xyz010,matrix4f),
                transform(xyz011,matrix4f),
                transform(xyz100,matrix4f),
                transform(xyz101,matrix4f),
                transform(xyz110,matrix4f),
                transform(xyz111,matrix4f)
        );
    }

    public OBBox translate(Vec3i pos){
        translate(pos.getX(),pos.getY(),pos.getZ());
        return this;
    }

    public OBBox translate(double x, double y, double z){
        return translate((float) x, (float) y,(float)z);
    }

    public OBBox translate(float x, float y, float z){
        this.xyz000.translate(x,y,z);
        this.xyz001.translate(x,y,z);
        this.xyz010.translate(x,y,z);
        this.xyz011.translate(x,y,z);
        this.xyz100.translate(x,y,z);
        this.xyz101.translate(x,y,z);
        this.xyz110.translate(x,y,z);
        this.xyz111.translate(x,y,z);
        return this;
    }

    public OBBox translateTo(float x, float y, float z){
        OBBox nob = new OBBox(xyz000,xyz001,xyz010,xyz011,xyz100,xyz101,xyz110,xyz111);
        nob.xyz000.translate(x,y,z);
        nob.xyz001.translate(x,y,z);
        nob.xyz010.translate(x,y,z);
        nob.xyz011.translate(x,y,z);
        nob.xyz100.translate(x,y,z);
        nob.xyz101.translate(x,y,z);
        nob.xyz110.translate(x,y,z);
        nob.xyz111.translate(x,y,z);
        return nob;
    }

    public Vector3f getXyz000() {
        return xyz000;
    }

    public void setXyz000(Vector3f xyz000) {
        this.xyz000 = xyz000;
    }

    public Vector3f getXyz001() {
        return xyz001;
    }

    public void setXyz001(Vector3f xyz001) {
        this.xyz001 = xyz001;
    }

    public Vector3f getXyz010() {
        return xyz010;
    }

    public void setXyz010(Vector3f xyz010) {
        this.xyz010 = xyz010;
    }

    public Vector3f getXyz011() {
        return xyz011;
    }

    public void setXyz011(Vector3f xyz011) {
        this.xyz011 = xyz011;
    }

    public Vector3f getXyz100() {
        return xyz100;
    }

    public void setXyz100(Vector3f xyz100) {
        this.xyz100 = xyz100;
    }

    public Vector3f getXyz101() {
        return xyz101;
    }

    public void setXyz101(Vector3f xyz101) {
        this.xyz101 = xyz101;
    }

    public Vector3f getXyz110() {
        return xyz110;
    }

    public void setXyz110(Vector3f xyz110) {
        this.xyz110 = xyz110;
    }

    public Vector3f getXyz111() {
        return xyz111;
    }

    public void setXyz111(Vector3f xyz111) {
        this.xyz111 = xyz111;
    }

    public static Vector4f transform(Vector4f vec4f,Matrix4f matrix4f){
        Vector4f vector4f = new Vector4f();
        Matrix4f.transform(matrix4f,
                vec4f,
                vector4f);
        return vector4f;
    }

    public static Vector3f transform(Vector3f coordinate,Matrix4f matrix4f){
        //optimized for zero vector
        if(coordinate.getX() == 0 && coordinate.getY() == 0 && coordinate.getZ() == 0) {
            return new Vector3f( matrix4f.m30,matrix4f.m31,matrix4f.m32);
        }
        return transformCoordinate(matrix4f, coordinate, new Vector3f());
    }

    public static void transform1(Vector3f coordinate,Matrix4f matrix4f){
        transformCoordinate(matrix4f, coordinate, coordinate);
    }

    public static Vector3f transformCoordinate(Matrix4f left, Vector3f right, Vector3f dest) {
        if (dest == null)
            dest = new Vector3f();

        float x = left.m00 * right.x + left.m10 * right.y + left.m20 * right.z + left.m30;
        float y = left.m01 * right.x + left.m11 * right.y + left.m21 * right.z + left.m31;
        float z = left.m02 * right.x + left.m12 * right.y + left.m22 * right.z + left.m32;

        dest.x = x;
        dest.y = y;
        dest.z = z;

        return dest;
    }


    public static Vec3d transformCoordinate(Matrix4f left, Vec3d right) {

        double x = left.m00 * right.x + left.m10 * right.y + left.m20 * right.z + left.m30;
        double y = left.m01 * right.x + left.m11 * right.y + left.m21 * right.z + left.m31;
        double z = left.m02 * right.x + left.m12 * right.y + left.m22 * right.z + left.m32;

        return new Vec3d(x,y,z);
    }

    public static Vector3f transformVector(Matrix4f left, Vector3f right, Vector3f dest) {
        if (dest == null)
            dest = new Vector3f();

        float x = left.m00 * right.x + left.m10 * right.y + left.m20 * right.z;
        float y = left.m01 * right.x + left.m11 * right.y + left.m21 * right.z;
        float z = left.m02 * right.x + left.m12 * right.y + left.m22 * right.z;

        dest.x = x;
        dest.y = y;
        dest.z = z;

        return dest;
    }

    public boolean intersect(AxisAlignedBB other){
        return Collision.intersect(this,new OBBox(other));
    }

    public float collisionDetermination(AxisAlignedBB other, Vector3f v, Vector3f a){
        return collisionDetermination(new OBBox(other),v,a);
    }

    public float collisionDetermination(OBBox other, Vector3f v, Vector3f a){
        return Collision.satTest(other,this,v,a);
    }

    public boolean intersect(OBBox other){
        return Collision.intersect(this,other);
    }

    public RayTraceResult intersect(Vec3d start,Vec3d end){

        return null;
    }

    public String toString(){

        return "box[" +
                xyz000.toString() + "," +
                xyz001.toString() + "," +
                xyz010.toString() + "," +
                xyz011.toString() + "," +
                xyz100.toString() + "," +
                xyz101.toString() + "," +
                xyz110.toString() + "," +
                xyz111.toString() + "]";
    }

    public static class Collision{

        public static float satTest(OBBox moveOBB, OBBox staticOBB, Vector3f v, Vector3f ar){
            float maxTime = Float.MIN_VALUE;
            Vector3f axis = null;
            for (int i = 0; i <3; i++)
            {
                Vector3f axis0 = getFaceDirection(moveOBB,i).normalise();
                float length = Math.abs(Vector3f.dot(axis0,v));

                if(length > 0) {
                    float[] res1 = getInterval(moveOBB, axis0);
                    float[] res2 = getInterval(staticOBB, axis0);
                    if (res1[1] <= res2[0]) {
                        float a = (res2[0] - res1[1]) / length;
                        if (a > maxTime && a <= 1) {
                            maxTime = a;
                            axis = axis0;
                        }
                    } else if (res2[1] <= res1[0]) {
                        float a = (-res2[1] + res1[0]) / length;
                        if (a > maxTime && a <= 1) {
                            maxTime = a;
                            axis = axis0;
                        }
                    }
                }

                Vector3f axis1 = getFaceDirection(staticOBB,i).normalise();
                length = Math.abs(Vector3f.dot(axis1,v));

                if(length > 0) {
                    float[] res3 = getInterval(moveOBB, axis1);
                    float[] res4 = getInterval(staticOBB, axis1);
                    if (res3[1] <= res4[0]) {
                        float a = (res4[0] - res3[1]) / length;
                        if (a > maxTime && a <= 1) {
                            maxTime = a;
                            axis = axis1;
                        }
                    } else if (res3[0] >= res4[1]) {
                        float a = (res3[0] - res4[1]) / length;
                        if (a > maxTime && a <= 1) {
                            maxTime = a;
                            axis = axis1;
                        }
                    }
                }
            }

            for (int i = 0; i <3; i++)
            {
                for (int j = 0; j <3; j++)
                {
                    Vector3f axis2 = new Vector3f();
                    Vector3f.cross(getEdgeDirection(moveOBB,i), getEdgeDirection(staticOBB,j),axis2);
                    if(axis2.lengthSquared() == 0) continue;
                    axis2.normalise();
                    float length = Math.abs(Vector3f.dot(axis2,v));
                    float[] res1 = getInterval(moveOBB, axis2);
                    float[] res2 = getInterval(staticOBB, axis2);
                    if (res1[1] <= res2[0]){
                        float a = (res2[0] - res1[1])/length;
                        if(a > maxTime && a <= 1) {
                            maxTime = a;
                            axis = axis2;
                        }
                    }else if(res2[1] <= res1[0]){
                        float a = (-res2[1] + res1[0])/length;
                        if(a > maxTime && a <= 1) {
                            maxTime = a;
                            axis = axis2;
                        }
                    }
                }
            }

            if(axis != null) {
                ar.set(axis.x, axis.y, axis.z);
            }else{
            }

            return maxTime;
        }

        public static boolean intersect(OBBox a,OBBox b){
            for (int i = 0; i <3; i++)
            {
                Vector3f fd = getFaceDirection(a,i);
                float[] res1 = getInterval(a, fd);
                float[] res2 = getInterval(b, fd);
                if (res1[1] <= res2[0] || res2[1] <= res1[0]) return false;

                fd = getFaceDirection(b,i);
                float[] res3 = getInterval(a, fd);
                float[] res4 = getInterval(b, fd);
                if (res3[1] <= res4[0] || res4[1] <= res3[0]) return false;
            }

            for (int i = 0; i <3; i++)
            {
                for (int j = 0; j <3; j++)
                {
                    Vector3f axis = new Vector3f();
                    Vector3f.cross(getEdgeDirection(a,i), getEdgeDirection(b,j),axis);
                    if(axis.lengthSquared() != 0) {
                        float[] res1 = getInterval(a, axis);
                        float[] res2 = getInterval(b, axis);
                        if (res1[1] <= res2[0] || res2[1] <= res1[0]) return false;
                    }
                }
            }
            return true;
        }

        private static float[] getInterval(OBBox obBox,Vector3f axis){
            Vector3f[] corners = obBox.asArray();

            float value,min,max;

            min = max = projectPoint(axis, corners[0]);
            for(int i = 1; i <8; i++)
            {
                value = projectPoint(axis, corners[i]);
                min = Math.min(min, value);
                max = Math.max(max, value);
            }

            return new float[]{min,max};
        }

        private static float projectPoint( Vector3f axis,Vector3f point){
            return Vector3f.dot(point,axis) / axis.length();
        }

        private static Vector3f getEdgeDirection(OBBox obBox,int index){
            switch (index){
                case 0: return new Vector3f(obBox.xyz100.getX() - obBox.xyz000.getX(),obBox.xyz100.getY() - obBox.xyz000.getY(),obBox.xyz100.getZ() - obBox.xyz000.getZ());
                case 1: return new Vector3f(obBox.xyz010.getX() - obBox.xyz000.getX(),obBox.xyz010.getY() - obBox.xyz000.getY(),obBox.xyz010.getZ() - obBox.xyz000.getZ());
                default:
                    return new Vector3f(obBox.xyz001.getX() - obBox.xyz000.getX(),obBox.xyz001.getY() - obBox.xyz000.getY(),obBox.xyz001.getZ() - obBox.xyz000.getZ());
            }
        }

        private static Vector3f getFaceDirection(OBBox obBox,int index){
            final Vector3f x = getEdgeDirection(obBox,0);
            final Vector3f y = getEdgeDirection(obBox,1);
            final Vector3f z = getEdgeDirection(obBox,2);

            switch (index){
                case 0:
                    return Vector3f.cross(x,y,new Vector3f());
                case 1:
                    return Vector3f.cross(x,z,new Vector3f());
                default:
                    return Vector3f.cross(y,z,new Vector3f());
            }
        }
    }



}
