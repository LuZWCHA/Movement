package com.nowandfuture.asm;

import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IRender {
    void renderBlockLayer(int pass, double p, BlockRenderLayer blockRenderLayer);
    void prepare(float p);
    boolean isRenderValid();
}
