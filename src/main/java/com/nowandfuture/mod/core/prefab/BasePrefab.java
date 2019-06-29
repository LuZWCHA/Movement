package com.nowandfuture.mod.core.prefab;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class BasePrefab extends AbstractPrefab {

    public BasePrefab(){
        super();
    }

    public BasePrefab(World world, BlockPos baseLocation, Vec3i size) {
        super(world, baseLocation, size);
    }
}
