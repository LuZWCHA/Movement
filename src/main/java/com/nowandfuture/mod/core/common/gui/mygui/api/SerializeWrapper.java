package com.nowandfuture.mod.core.common.gui.mygui.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public abstract class SerializeWrapper<T> {
    protected T object;

    public SerializeWrapper(T object){
        this.object = object;
    }

    public abstract NBTTagCompound serializeNBT();

    public abstract void deserializeNBT(NBTTagCompound nbt);

    public T getObject() {
        return object;
    }

    public static class BlockPosWrap extends SerializeWrapper<BlockPos>{

        public BlockPosWrap(BlockPos object) {
            super(object);
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setLong("pos",object.toLong());
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            object = BlockPos.fromLong(nbt.getLong("pos"));
        }
    }
}
