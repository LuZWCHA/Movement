package com.nowandfuture.mod.core.prefab;

import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class NormalPrefab extends AbstractPrefab {

    public NormalPrefab(){
        super();
    }

    public NormalPrefab(World world, BlockPos baseLocation, Vec3i size) {
        super(world, baseLocation, size);
    }
}
