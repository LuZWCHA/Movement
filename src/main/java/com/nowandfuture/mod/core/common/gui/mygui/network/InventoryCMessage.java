package com.nowandfuture.mod.core.common.gui.mygui.network;

import com.nowandfuture.mod.core.common.gui.mygui.api.SerializeWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class InventoryCMessage<T extends SerializeWrapper> implements IMessage {

    private NBTTagCompound nbt;
    private T object;
    private short flag;

    public NBTTagCompound getNbt() {
        return nbt;
    }

    public Object getObject() {
        return object.getObject();
    }

    public short getFlag() {
        return flag;
    }

    public InventoryCMessage(){

    }

    public InventoryCMessage(short flag , BlockPos blockPos, NBTTagCompound nbt){
        this.flag = flag;
        this.nbt = nbt;
        this.object = (T) new SerializeWrapper.BlockPosWrap(blockPos);
    }

    public InventoryCMessage(short flag , T object, NBTTagCompound nbt){
        this.flag = flag;
        this.nbt = nbt;
        this.object = object;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.flag = buf.readShort();
        DataInputStream inputStream = new DataInputStream(new ByteBufInputStream(buf));
        NBTTagCompound combineNBT;
        if(flag == 0){
            this.object = (T) new SerializeWrapper.BlockPosWrap(BlockPos.ORIGIN);
        }
        try {
            combineNBT = CompressedStreamTools.read(inputStream);
            this.nbt = combineNBT.getCompoundTag("inventory");
            this.object.deserializeNBT(combineNBT.getCompoundTag("identify"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(flag);
        NBTTagCompound combineNBT = new NBTTagCompound();
        combineNBT.setTag("inventory",nbt);
        combineNBT.setTag("identify",object.serializeNBT());

        DataOutputStream outputStream = new DataOutputStream(new ByteBufOutputStream(buf));
        try {
            CompressedStreamTools.write(combineNBT,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
