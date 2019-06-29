package com.nowandfuture.mod.core.transformers.animation;

import com.nowandfuture.mod.core.transformers.*;
import net.minecraft.nbt.NBTTagCompound;

import java.io.Serializable;
import java.util.Objects;

public abstract class KeyFrame<T extends IKeyFarmVisitor> implements Serializable,Comparable<KeyFrame> {
    private static final long serialVersionUID = -1765955595746454563L;
    public final static String NBT_ATTRIBUTE_TICK = "BeginTick";

    public enum KeyFrameType{
        LINEAR(0),
        ROTATION(1),
        SCALE(2);

        int typeId;

        KeyFrameType(int i){
            typeId = i;
        }
    }

    //shoudong baozheng type xiangtong
    protected int type;
    private long beginTick;

    protected KeyFrame(){
        type = -1;
    }

    public abstract void visit(T visitor);

    public void readParametersFromNBT(NBTTagCompound compound) {
        beginTick = compound.getLong(NBT_ATTRIBUTE_TICK);
    }

    public NBTTagCompound writeParametersToNBT(NBTTagCompound compound) {
        compound.setLong(NBT_ATTRIBUTE_TICK,beginTick);
        return compound;
    }

    public long getBeginTick() {
        return beginTick;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyFrame)) return false;
        KeyFrame<?> keyFrame = (KeyFrame<?>) o;
        return beginTick == keyFrame.beginTick;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginTick);
    }

    @Override
    public int compareTo(KeyFrame o) {
        return (int) (this.beginTick - o.beginTick);
    }

    public void setBeginTick(long beginTick) {
        this.beginTick = beginTick;
    }

    public static class Factory{
        public static KeyFrame create(KeyFrameType keyFrameType,NBTTagCompound compound){
            switch (keyFrameType){
                case LINEAR:return LinearTransformNode.LinearKeyFrame.create(compound);
                case ROTATION:return RotationTransformNode.RotationKeyFrame.create(compound);
                case SCALE:return ScaleTransformNode.ScaleKeyFrame.create(compound);
            }
            return null;
        }
    }
}
