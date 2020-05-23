package com.nowandfuture.mod.core.transformers;

import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.core.transformers.arithmetics.IInterpolationAlgorithm;
import com.nowandfuture.mod.utils.math.Matrix4f;
import net.minecraft.nbt.NBTTagCompound;


public class ScaleTransformNode extends AbstractTransformNode<ScaleTransformNode.ScaleKeyFrame> {

    private KeyFrameLine line;

    public ScaleTransformNode(){
        super();
        setTypeId(3);
        setArithmeticId(3);
    }

    @Override
    protected void transform(Matrix4f renderer, float p, ScaleKeyFrame preKey, ScaleKeyFrame key) {
        IInterpolationAlgorithm<ScaleKeyFrame> algorithm = getAlgorithm(getArithmeticId());
        algorithm.calculate(renderer,line,p,preKey,key);
    }

    @Override
    protected void transformPost(Matrix4f renderer, float p, ScaleKeyFrame pre, ScaleKeyFrame key) {
    }

    @Override
    protected boolean isAcceptKeyFarm(KeyFrame keyFrame) {
        return keyFrame instanceof ScaleKeyFrame;
    }

    @Override
    public void prepare(KeyFrameLine frameLine) {
        this.line = frameLine;
    }

    @Override
    public void update(ScaleKeyFrame list) {

    }

    public static class ScaleKeyFrame extends KeyFrame<ScaleTransformNode> {

        public final String NBT_ATTRIBUTE_SCALE = "Scale";

        public float scale;

        @Override
        public KeyFrame<ScaleTransformNode> clone() {
            ScaleKeyFrame scaleKeyFrame = new ScaleKeyFrame();
            scaleKeyFrame.setBeginTick(getBeginTick());
            scaleKeyFrame.scale = scale;
            return scaleKeyFrame;
        }

        public ScaleKeyFrame(){
            type = 2;
            scale = 1;
        }

        public ScaleKeyFrame(float scale){
            this();
            this.scale = scale;
        }

        public void readParametersFromNBT(NBTTagCompound compound) {
            super.readParametersFromNBT(compound);
            scale = compound.getFloat(NBT_ATTRIBUTE_SCALE);
        }

        public NBTTagCompound writeParametersToNBT(NBTTagCompound compound) {
            compound.setFloat(NBT_ATTRIBUTE_SCALE,scale);
            super.writeParametersToNBT(compound);

            return compound;
        }

       @Override
       public void visit(ScaleTransformNode visitor) {
           visitor.update(this);
       }

        public static ScaleKeyFrame create(NBTTagCompound compound){
            ScaleKeyFrame scaleKeyFrame = new ScaleKeyFrame();
            scaleKeyFrame.readParametersFromNBT(compound);
            return scaleKeyFrame;
        }
   }
}
