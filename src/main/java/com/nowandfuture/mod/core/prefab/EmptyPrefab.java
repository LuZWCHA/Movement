package com.nowandfuture.mod.core.prefab;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class EmptyPrefab extends AbstractPrefab {

    public EmptyPrefab(){
        super();
    }

    public EmptyPrefab(World world, BlockPos baseLocation, Vec3i size) {
        super(world, baseLocation, size);
    }
}
