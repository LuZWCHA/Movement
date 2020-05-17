package com.nowandfuture.mod.core.transformers;

import net.minecraft.util.math.MathHelper;

import java.io.Serializable;

//0-1 interpolation
//never used
public abstract class TimeInterpolation {
    public abstract Type getType();
    public abstract double interpolate(double progress);

    private TimeInterpolation(){}

    static class LinearInterpolation extends TimeInterpolation {

        @Override
        public Type getType() {
            return Type.LINEAR;
        }

        @Override
        public double interpolate(double x) {
            return x;
        }
    }

    static class SmoothStepInterpolation extends TimeInterpolation {

        @Override
        public Type getType() {
            return Type.SMOOTH_STEP;
        }

        @Override
        public double interpolate(double x) {
            return x * x * (3 - 2 * x);
        }
    }

    static class HigherPowerInterpolationDown extends TimeInterpolation {

        @Override
        public Type getType() {
            return Type.HIGHER_POWER_DOWN;
        }

        @Override
        public double interpolate(double v) {
            return 1 - (1 - v) * (1 - v);
        }
    }

    static class HigherPowerInterpolationUp extends TimeInterpolation {

        @Override
        public Type getType() {
            return Type.HIGHER_POWER_UP;
        }

        @Override
        public double interpolate(double v) {
            return v * v;
        }
    }

    static class SinInterpolation extends TimeInterpolation {


        @Override
        public Type getType() {
            return Type.SIN;
        }

        @Override
        public double interpolate(double v) {
            return MathHelper.sin((float) (v * Math.PI / 2));
        }
    }


    public enum Type implements Serializable {
        LINEAR,
        SMOOTH_STEP,
        HIGHER_POWER_DOWN,
        HIGHER_POWER_UP,
        SIN
    }

    public static class Factory{
        public static TimeInterpolation build(Type type){
            switch (type){
                case SIN:return new SinInterpolation();
                case SMOOTH_STEP:return new SmoothStepInterpolation();
                case HIGHER_POWER_UP:return new HigherPowerInterpolationUp();
                case HIGHER_POWER_DOWN:return new HigherPowerInterpolationDown();
                default:
                    return new LinearInterpolation();
            }
        }
    }
}
