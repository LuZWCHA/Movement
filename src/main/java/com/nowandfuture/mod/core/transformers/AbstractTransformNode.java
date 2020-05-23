package com.nowandfuture.mod.core.transformers;

import com.nowandfuture.mod.core.transformers.animation.IKeyFarmVisitor;
import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.core.transformers.arithmetics.*;
import com.nowandfuture.mod.utils.math.Matrix4f;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTransformNode<T extends KeyFrame> implements IKeyFarmVisitor<T> {
    public static final String NBT_NEXT_TYPE = "NextType";
    public static final String NBT_NEXT_TAG = "NextTag";

    public static final String NBT_TYPE = "Type";

    public static final String NBT_INTERPOLATION_TYPE = "Interpolation";
    public static final String NBT_Arithmetic_ID = "ArithmeticID";

    private long typeId;
    private int arithmeticId;
    private String name;

    //time line
    private TimeInterpolation.Type type =
            TimeInterpolation.Type.LINEAR;
    private TimeInterpolation interpolation =
            TimeInterpolation.Factory.build(type);

    private AbstractTransformNode next;

    protected AbstractTransformNode(){

    }

    //transform start from the root to this, because of GL Matrix use the stack,so the final order is form the this to next
    public final void transformStart(final Matrix4f renderer, float p, KeyFrame pre, KeyFrame now){
        if(pre == null) {
            pre = now.clone();
            pre.setBeginTick(0);
        }

        if(getNext() != null)
            getNext().transformStart(renderer,p,pre,now);

        if(isAcceptKeyFarm(pre) && isAcceptKeyFarm(now)) {
            if(interpolation!=null)
                p = (float) interpolation.interpolate(p);
            transform(renderer, p, (T) pre, (T) now);
        }
    };

    public final void transformEnd(final Matrix4f renderer, float p, KeyFrame pre, KeyFrame now){
        if(pre == null) pre = now;
        if(now == null) now = pre;

        if(isAcceptKeyFarm(pre) && isAcceptKeyFarm(now)) {
            if(interpolation!=null)
                p = (float) interpolation.interpolate(p);
            transformPost(renderer, p, (T) pre, (T) now);
        }

        if(getNext() != null)
            getNext().transformEnd(renderer, p,(T)pre,(T)now);
    }

    public final void initLine(KeyFrameLine frameLine){
        prepare(frameLine);
        if(getNext() != null)
            getNext().initLine(frameLine);
    }

    public void prepare(KeyFrameLine frameLine){

    }

    protected abstract boolean isAcceptKeyFarm(KeyFrame keyFrame);
    protected abstract void transform(final Matrix4f renderer, float p,T preKey,T key);
    protected abstract void transformPost(final Matrix4f renderer, float p,T preKey,T key);

    public void readFromNBT(NBTTagCompound compound){
        readParametersFromNBT(compound);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        return writeParametersToNBT(compound);
    }

    public void readParametersFromNBT(NBTTagCompound compound){
        if(compound.isEmpty()) return;
        typeId = compound.getLong(NBT_TYPE);
        arithmeticId = compound.getInteger(NBT_Arithmetic_ID);

        if (compound.hasKey(NBT_INTERPOLATION_TYPE)) {
            type = TimeInterpolation.Type.values()[compound.getInteger(NBT_INTERPOLATION_TYPE)];
            interpolation = TimeInterpolation.Factory.build(type);
        }

        if(compound.hasKey(NBT_Arithmetic_ID)){
            arithmeticId = compound.getInteger(NBT_Arithmetic_ID);
        }

        //set parents params
        long type = compound.getLong(NBT_NEXT_TYPE);
        //set no next
        if(type < 0) {
            next = null;
            return;
        }

        next = TransformNodeManager.INSTANCE.getTransformNode(type).orElse(RootTransformNode.create());

        if(compound.hasKey(NBT_NEXT_TAG))
            next.readParametersFromNBT(compound.getCompoundTag(NBT_NEXT_TAG));
    }

    public NBTTagCompound writeParametersToNBT(NBTTagCompound compound){
        compound.setLong(NBT_TYPE, typeId);

        compound.setInteger(NBT_INTERPOLATION_TYPE,type.ordinal());
        compound.setInteger(NBT_Arithmetic_ID,arithmeticId);

        if(next != null){
            compound.setLong(NBT_NEXT_TYPE, next.getTypeId());

            NBTTagCompound nextTag = new NBTTagCompound();
            next.writeParametersToNBT(nextTag);
            compound.setTag(NBT_NEXT_TAG,nextTag);

        }else{
            compound.setLong(NBT_NEXT_TYPE,-1);//set no next
        }

        return compound;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AbstractTransformNode getNext() {
        return next;
    }

    private void setNext(AbstractTransformNode next) {
        this.next = next;
    }

    public long getTypeId() {
        return typeId;
    }

    void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public void setArithmeticId(int arithmeticId) {
        this.arithmeticId = arithmeticId;
    }

    public int getArithmeticId() {
        return arithmeticId;
    }

    public void setInterpolation(TimeInterpolation.Type type) {
        this.type = type;
        this.interpolation = TimeInterpolation.Factory.build(type);
    }

    public TimeInterpolation getInterpolation() {
        return interpolation;
    }

    public static class Builder{
        private AbstractTransformNode now;
        private AbstractTransformNode head;
        private Builder(){

        }

        public static Builder newBuilder(){
            return new Builder();
        }

        public AbstractTransformNode buildFromNBTTag(NBTTagCompound compound){
            long typeId = compound.getLong(NBT_TYPE);

            AbstractTransformNode node = TransformNodeManager.INSTANCE.getTransformNode(typeId).get();
            node.readParametersFromNBT(compound);

            //set parents params
            long type = compound.getLong(NBT_NEXT_TYPE);

            if(type < 0) {//set no next transformer
                node.next = null;
                return node;
            }

            node.next = TransformNodeManager.INSTANCE.getTransformNode(type).orElse(RootTransformNode.create());

            if(node.next instanceof RootTransformNode) return node;//nothing to set

            if(compound.hasKey(NBT_NEXT_TAG))
                node.next.readParametersFromNBT(compound.getCompoundTag(NBT_NEXT_TAG));

            return node;
        }

        public Builder create(AbstractTransformNode node){
            this.now = node;
            this.head = node;
            return this;
        }

        public Builder parent(AbstractTransformNode node){
            this.head.setNext(node);
            this.head = node;
            return this;
        }

        public void build(){
            head.setNext(TransformNodeManager.INSTANCE.getDefaultAttributeNode());
        }
    }

    private final static Map<Integer,IInterpolationAlgorithm> algorithmList;
    static {
        algorithmList = new HashMap<>();

        IInterpolationAlgorithm[] algorithms = new IInterpolationAlgorithm[]{
                new LinearInterpolation(),
                new CubicBezierInterpolation(),
                new QuaternionSlerpInterpolation(),
                new ScaleLinearInterpolation()
        };

        for (IInterpolationAlgorithm a :
                algorithms) {
            algorithmList.put(a.getID(), a);
        }
    }

    protected <T extends KeyFrame> IInterpolationAlgorithm<T> getAlgorithm(int aid){
        return algorithmList.get(aid);
    }
}
