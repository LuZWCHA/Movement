package com.nowandfuture.mod.api;

import com.nowandfuture.mod.utils.math.Matrix4f;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public interface IModule extends ITickable {
    void readModuleFromNBT(NBTTagCompound compound);
    NBTTagCompound writeModuleToNBT(NBTTagCompound compound);
    boolean isEnable();
    BlockPos getModulePos();
    void doTransform(double p, Matrix4f matrix4f);
}
