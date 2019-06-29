package com.nowandfuture.mod.core.prefab;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;

public class LocalChunk extends Chunk {

    public LocalChunk(World worldIn, int x, int z) {
        super(worldIn, x, z);
    }

    public LocalChunk(World worldIn, ChunkPrimer primer, int x, int z) {
        super(worldIn, primer, x, z);
    }
}
