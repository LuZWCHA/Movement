package com.nowandfuture.mod.core.transformers.arithmetics;

import com.nowandfuture.mod.core.transformers.LocationTransformNode;
import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class CubicBezierInterpolation implements IInterpolationAlgorithm<LocationTransformNode.LocationKeyFrame>{

    private KeyFrameLine line;

    @Override
    public int getID() {
        return 1;
    }

    @Override
    public void calculate(Matrix4f renderer, @Nonnull KeyFrameLine line, float p, LocationTransformNode.LocationKeyFrame preKey, LocationTransformNode.LocationKeyFrame key) {
        this.line = line;
        Vec3d[] controlPoints = getControlPoints(preKey,key);
        Vec3d temp = calculateCubicBezier(preKey.curPos,controlPoints[0],controlPoints[1],key.curPos,p);
        renderer.translate(new Vector3f((float) temp.x,(float)temp.y,(float)temp.z));
    }

    //P = (1-t)^3 * P0 + 3 * t * (1-t) ^ 2 * P1 + 3 * t^2 * (1-t) * P2 + t^3 * P3
    private Vec3d calculateCubicBezier(Vec3d p0,Vec3d p1,Vec3d p2,Vec3d p3,float t){
        double x = pow3(1-t) * p0.x + 3 * t * pow2(1-t) * p1.x + 3 * pow2(t) * (1-t) * p2.x + pow3(t) * p3.x;
        double y = pow3(1-t) * p0.y + 3 * t * pow2(1-t) * p1.y + 3 * pow2(t) * (1-t) * p2.y + pow3(t) * p3.y;
        double z = pow3(1-t) * p0.z + 3 * t * pow2(1-t) * p1.z + 3 * pow2(t) * (1-t) * p2.z + pow3(t) * p3.z;
        return new Vec3d(x,y,z);
    }

    private Vec3d[] getControlPoints(LocationTransformNode.LocationKeyFrame p0Key, LocationTransformNode.LocationKeyFrame p3Key){
        Vec3d[] centers = {p0Key.curPos,getCenterPoint(p0Key.curPos,p3Key.curPos),p3Key.curPos};
        line.getPreFrame(p0Key).ifPresent(new Consumer<KeyFrame>() {
            @Override
            public void accept(KeyFrame frame) {
                LocationTransformNode.LocationKeyFrame bezierKeyFrame = (LocationTransformNode.LocationKeyFrame) frame;
                centers[0] = getCenterPoint(bezierKeyFrame.curPos,p0Key.curPos);
            }
        });

        line.getNextFrame(p3Key).ifPresent(new Consumer<KeyFrame>() {
            @Override
            public void accept(KeyFrame frame) {
                LocationTransformNode.LocationKeyFrame bezierKeyFrame = (LocationTransformNode.LocationKeyFrame) frame;
                centers[2] = getCenterPoint(bezierKeyFrame.curPos,p3Key.curPos);
            }
        });

        Vec3d c0 = getCenterPoint(centers[0],centers[1]);
        Vec3d c1 = getCenterPoint(centers[1],centers[2]);

        Vec3d d0 = p0Key.curPos.subtract(c0);
        Vec3d d1 = p3Key.curPos.subtract(c1);

        return new Vec3d[]{centers[1].add(d0),centers[1].add(d1)};
    }

    private Vec3d getCenterPoint(Vec3d start,Vec3d end){
        return start.add(end).scale(.5f);
    }

    private double pow3(double t){
        return t * t * t;
    }

    private double pow2(double t){
        return t * t;
    }
}
