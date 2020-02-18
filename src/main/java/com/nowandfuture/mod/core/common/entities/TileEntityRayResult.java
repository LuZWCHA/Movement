package com.nowandfuture.mod.core.common.entities;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class TileEntityRayResult extends RayTraceResult {
    public IClickableTile tileEntity;

    public TileEntityRayResult(Vec3d hitVecIn, EnumFacing sideHitIn, BlockPos blockPosIn) {
        super(hitVecIn, sideHitIn, blockPosIn);
    }

    public TileEntityRayResult(Vec3d hitVecIn, EnumFacing sideHitIn) {
        super(hitVecIn, sideHitIn);
    }

    public TileEntityRayResult(Entity entityIn) {
        super(entityIn);
    }

    public TileEntityRayResult(Type typeIn, Vec3d hitVecIn, EnumFacing sideHitIn, BlockPos blockPosIn) {
        super(typeIn, hitVecIn, sideHitIn, blockPosIn);
    }

    public TileEntityRayResult(Entity entityHitIn, Vec3d hitVecIn) {
        super(entityHitIn, hitVecIn);
    }

    public TileEntityRayResult(IClickableTile entityHitIn, Vec3d hitVecIn) {
        this(hitVecIn,EnumFacing.NORTH);
        tileEntity = entityHitIn;
    }
}
