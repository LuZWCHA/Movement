package com.nowandfuture.mod.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class ModuleBlock extends Block {

    public ModuleBlock(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public ModuleBlock(Material materialIn) {
        super(materialIn);
    }
}
