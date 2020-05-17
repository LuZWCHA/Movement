package com.nowandfuture.mod.utils.collision;

import com.nowandfuture.mod.api.Unstable;
import com.nowandfuture.mod.utils.math.Vector3f;

import java.util.Objects;

@Unstable
public class CollisionInfo {
    private Vector3f impactAxis;
    private double impactTime;

    public CollisionInfo(Vector3f impactAxis, double impactTime) {
        this.impactAxis = impactAxis.normalise();
        if(impactTime < 1E-5) impactTime = 0;
        this.impactTime = impactTime;
    }

    public double getImpactTime() {
        return impactTime;
    }

    public Vector3f getImpactAxis() {
        return impactAxis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollisionInfo that = (CollisionInfo) o;
        return impactAxis.equals(that.impactAxis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impactAxis);
    }
}
