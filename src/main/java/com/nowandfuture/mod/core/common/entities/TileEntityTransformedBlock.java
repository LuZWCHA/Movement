package com.nowandfuture.mod.core.common.entities;

import com.nowandfuture.mod.core.common.blocks.TransformedBlock;
import com.nowandfuture.mod.core.prefab.LocalWorld;
import net.minecraft.tileentity.TileEntity;

public class TileEntityTransformedBlock extends TileEntity {
    private TransformedBlock.BlockWrapper localBlock;

    public void setLocalBlock(TransformedBlock.BlockWrapper localBlock) {
        this.localBlock = localBlock;
    }

    public TransformedBlock.BlockWrapper getLocalBlock() {
        return localBlock;
    }
}
