package com.nowandfuture.mod.core.common.gui.mygui.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class InventorySMessage implements IMessage {
    private NBTTagCompound nbt;
    private int windowId;

    public NBTTagCompound getNbt() {
        return nbt;
    }

    public int getWindowId() {
        return windowId;
    }

    public InventorySMessage(){

    }

    public InventorySMessage(int windowId,NBTTagCompound nbt){
        this.windowId = windowId;
        this.nbt = nbt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        DataInputStream inputStream = new DataInputStream(new ByteBufInputStream(buf));
        try {
            this.windowId = buf.readInt();
            this.nbt = CompressedStreamTools.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        DataOutputStream outputStream = new DataOutputStream(new ByteBufOutputStream(buf));
        try {
            buf.writeInt(this.windowId);
            CompressedStreamTools.write(nbt,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
