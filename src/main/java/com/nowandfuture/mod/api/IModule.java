package com.nowandfuture.mod.api;

import com.nowandfuture.asm.IRender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;

public interface IModule {

    void readModuleFromNBT(NBTTagCompound compound);
    NBTTagCompound writeModuleToNBT(NBTTagCompound compound);
    boolean isEnable();
    BlockPos getModulePos();
}
