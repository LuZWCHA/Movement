package com.nowandfuture.mod.core.transformers;

import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.utils.MathHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.util.vector.Vector3f;

public class ScaleTransformNode extends AbstractTransformNode<ScaleTransformNode.ScaleKeyFrame> {

    public ScaleTransformNode(){
        super();
        setTypeId(3);
    }

    @Override
    protected void transform(AbstractPrefab recipe, float p, ScaleKeyFrame preKey, ScaleKeyFrame key) {
        GlStateManager.pushMatrix();

        final float s = MathHelper.Lerp(p,preKey.scale,key.scale);
        GlStateManager.scale(s,s,s);

        recipe.getModelMatrix().scale(new Vector3f(s,s,s));
    }

    @Override
    protected void transformPost(AbstractPrefab recipe, float p, ScaleKeyFrame pre, ScaleKeyFrame key) {
        GlStateManager.popMatrix();
    }

    @Override
    protected boolean isAcceptKeyFarm(KeyFrame keyFrame) {
        return keyFrame instanceof ScaleKeyFrame;
    }

    @Override
    public void update(ScaleKeyFrame list) {

    }

    public static class ScaleKeyFrame extends KeyFrame<ScaleTransformNode> {

        public final String NBT_ATTRIBUTE_SCALE = "Scale";

        public float scale;

        public ScaleKeyFrame(){
            type = 2;
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
