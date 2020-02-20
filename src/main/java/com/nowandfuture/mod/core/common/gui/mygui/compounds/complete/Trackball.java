package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;


import com.nowandfuture.mod.utils.math.Quaternion;
import com.nowandfuture.mod.utils.math.Vector3f;
import com.nowandfuture.mod.utils.math.Vector4f;


import java.nio.FloatBuffer;

public class Trackball{
    /*
     * This size should really be based on the distance from the center of
     * rotation to the point on the object underneath the mouse. That point
     * would then track the mouse as closely as possible. This is a simple
     * example, though, so that is left as an Exercise for the Programmer.
     */
    public float trackballSize = 5f;

    public Quaternion curquat = new Quaternion();
    Quaternion lastquat = new Quaternion();
    int beginx, beginy;

    int tb_width;
    int tb_height;
    int tb_button;

    boolean tb_tracking = false;

    public void tbInit(int button) {
        tb_button = button;
        trackball(curquat, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    public Quaternion tbMatrix(FloatBuffer matrix) {
        return curquat.store(matrix);
    }

    public void tbReshape(int width, int height) {
        tb_width = width;
        tb_height = height;
    }

    /**
     * {@inheritDoc}
     */
    public void mouseMoved(int x, int y, int dx, int dy) {
        if (tb_tracking) {
            trackball(lastquat, (2.0f * beginx - tb_width) / tb_width,
                    (tb_height - 2.0f * beginy) / tb_height,
                    (2.0f * x - tb_width) / tb_width, (tb_height - 2.0f * y)
                            / tb_height);
            beginx = x;
            beginy = y;

            Quaternion.mul(lastquat,curquat,curquat);
        }
    }

    void startMotion(int x, int y) {
        tb_tracking = true;
        beginx = x;
        beginy = y;
    }

    void stopMotion() {
        tb_tracking = false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: replacement for tbMouse(int button...)
     */
    public void mousePressed(int button, int x, int y) {
        if (button == tb_button)
            startMotion(x, y);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: replacement for tbMouse(int button...)
     */
    public void mouseReleased(int button, int x, int y) {
        // else if (state == GLUT_UP && button == tb_button)
        if (button == tb_button)
            stopMotion();
    }

    /**
     * Ok, simulate a track-ball. Project the points onto the virtual trackball,
     * then figure out the axis of rotation, which is the cross product of P1 P2
     * and O P1 (O is the center of the ball, 0,0,0) Note: This is a deformed
     * trackball-- is a trackball in the center, but is deformed into a
     * hyperbolic sheet of rotation away from the center. This particular
     * function was chosen after trying out several variations.
     *
     * It is assumed that the arguments to this routine are in the range (-1.0
     * ... 1.0) Pass the x and y coordinates of the last and current positions
     * of the mouse, scaled so they are from (-1.0 ... 1.0).
     *
     * The resulting rotation is stored in the quaternion rotation (first
     * paramater).
     *
     * @param q
     *            quaternion to be set
     * @param p1x
     *            start point of rotation
     * @param p1y
     *            start point of rotation
     * @param p2x
     *            end point of rotation
     * @param p2y
     *            end point of rotation
     */
    void trackball(Quaternion q, float p1x, float p1y, float p2x, float p2y) {
        Vector3f a = new Vector3f(); /* Axis of rotation */
        float phi; /* how much to rotate about axis */
        Vector3f p1, p2, d = new Vector3f();
        float t;

        if (p1x == p2x && p1y == p2y) {
            /* Zero rotation */
            q.set(0,0,0);
            q.w = 1.0f;
            return;
        }

        /*
         * First, figure out z-coordinates for projection of P1 and P2 to
         * deformed sphere
         */
        p1 = new Vector3f(p1x, p1y, projectToSphere(trackballSize, p1x,
                p1y));
        p2 = new Vector3f(p2x, p2y, projectToSphere(trackballSize, p2x,
                p2y));


        /*
         * Now, we want the cross product of P1 and P2
         */
        Vector3f.cross(p1,p2,a);

        /*
         * Figure out how much to rotate around that axis.
         */
        Vector3f.sub(p1,p2,d);
        t = d.length() / (2.0f * trackballSize);

        /*
         * Avoid problems with out-of-control values...
         */
        if (t > 1.0)
            t = 1.0f;
        if (t < -1.0)
            t = -1.0f;
        phi = 2.0f * (float) Math.asin(t) * trackballSize;

        q.setFromAxisAngle(new Vector4f(a.getX(),a.getY(),a.getZ(),phi));
    }

    /**
     * Project an x,y pair onto a sphere of radius r OR a hyperbolic sheet if we
     * are away from the center of the sphere.
     *
     * @param r
     *            radius of the sphere
     * @param x
     *            point on the sphere or inside it?
     * @param y
     *            point on the sphere or inside it?
     * @return transformed Z
     */
    public float projectToSphere(float r, float x, float y) {
        float d, z;

        d = (float) Math.sqrt(x * x + y * y);
        if (d < r * 0.70710678118654752440)
            z = (float) Math.sqrt(r * r - d * d);
        else { /* On hyperbola */
            z = r * r / (2 * d);
        }
        return z;
    }
}
