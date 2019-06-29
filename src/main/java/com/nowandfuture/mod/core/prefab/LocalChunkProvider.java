package com.nowandfuture.mod.core.prefab;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import javax.annotation.Nullable;

public class LocalChunkProvider implements IChunkProvider {
    @Nullable
    @Override
    public Chunk getLoadedChunk(int x, int z) {
        return null;
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        return null;
    }

    @Override
    public boolean tick() {
        return false;
    }

    @Override
    public String makeString() {
        return null;
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return false;
    }
}
