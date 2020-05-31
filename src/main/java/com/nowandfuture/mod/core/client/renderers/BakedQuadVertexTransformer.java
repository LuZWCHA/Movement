package com.nowandfuture.mod.core.client.renderers;

import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.VertexTransformer;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class BakedQuadVertexTransformer extends VertexTransformer {

    private ColorTransformation colorTransformation;
    private TRSRTransformation trsrTransformation;

    public BakedQuadVertexTransformer(IVertexConsumer parent,TRSRTransformation trsrTransformation) {
        super(parent);
        this.trsrTransformation =trsrTransformation;
    }

    public BakedQuadVertexTransformer(IVertexConsumer parent,ColorTransformation colorTransformation) {
        super(parent);
        this.colorTransformation = colorTransformation;
    }

    public BakedQuadVertexTransformer(IVertexConsumer parent,ColorTransformation colorTransformation,TRSRTransformation trsrTransformation) {
        this(parent,colorTransformation);
        this.trsrTransformation = trsrTransformation;
    }

    @Override
    public void put(int element, float... data) {
        VertexFormatElement formatElement = getVertexFormat().getElement(element);
        switch(formatElement.getUsage()) {
            case COLOR: {
                /*
                 * 0 is r
                 * 1 is g
                 * 2 is b
                 * 3 is a
                 */
                if(colorTransformation != null){
                    colorTransformation.transform(data);
                }
                break;
            }
            case POSITION: {
                /*
                 * 0 is x (positive to east)
                 * 1 is y (positive to up)
                 * 2 is z (positive to south)
                 * 3 is idk
                 */
                if(trsrTransformation != null) {
                    Vector4f normal = new Vector4f(data);
                    trsrTransformation.transformPosition(normal);
                    normal.get(data);
                    Vector3f v = trsrTransformation.getTranslation();
                    data[2] += v.z;
                    data[1] += v.y;
                    data[0] += v.x;
                }
                break;
            }
            case NORMAL:
                if(trsrTransformation != null) {
                    Vector3f normal = new Vector3f(data);
                    trsrTransformation.transformNormal(normal);
                    normal.get(data);
                }
                break;
        }
        super.put(element, data);
    }

    public static class ColorTransformation{
        public void transform(float[] colors){

        }
    }
}
