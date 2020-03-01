package com.nowandfuture.mod.core.prefab;

import com.nowandfuture.mod.utils.math.Vector3f;

public class Anchor {
    private long id;
    private boolean fixed;
    private Vector3f p0,p1;
    private AnchorObj obj0;
    private AnchorObj obj1;

    public AnchorObj getObj0() {
        return obj0;
    }

    public AnchorObj getObj1() {
        return obj1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setObj0(AnchorObj obj0) {
        this.obj0 = obj0;
    }

    public void setObj1(AnchorObj obj1) {
        this.obj1 = obj1;
    }

    public Vector3f getP0() {
        return p0;
    }

    public void setP0(Vector3f p0) {
        this.p0 = p0;
    }

    public Vector3f getP1() {
        return p1;
    }

    public void setP1(Vector3f p1) {
        this.p1 = p1;
    }
}
