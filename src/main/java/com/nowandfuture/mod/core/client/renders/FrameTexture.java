package com.nowandfuture.mod.core.client.renders;

import net.minecraft.client.renderer.texture.DynamicTexture;

import java.awt.image.BufferedImage;

public class FrameTexture extends DynamicTexture {
    public FrameTexture(BufferedImage p_i1270_1_) {
        super(p_i1270_1_);
    }

    public FrameTexture(int p_i1271_1_, int p_i1271_2_) {
        super(p_i1271_1_, p_i1271_2_);
    }

    public void updateBufferedImage(BufferedImage p_i1270_1_){
        p_i1270_1_.getRGB(0, 0, p_i1270_1_.getWidth(), p_i1270_1_.getHeight(), getTextureData(), 0, p_i1270_1_.getWidth());
        this.updateDynamicTexture();
    }
}
