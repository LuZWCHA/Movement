package com.nowandfuture.asm;

import net.minecraft.util.BlockRenderLayer;

public interface IRender {
    void renderBlockLayer(int pass, double p, BlockRenderLayer blockRenderLayer);
    void prepare(float p);
    @Deprecated
    void buildTranslucentBlocks(int pass, float p);
    boolean isRenderValid();
}
