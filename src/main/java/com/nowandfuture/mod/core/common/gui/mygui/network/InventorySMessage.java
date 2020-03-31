package com.nowandfuture.mod.core.common.gui.mygui.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;

public class InventorySMessage implements IMessage {
    private String inventoryId;
    private NBTTagCompound nbt;
    private int windowId;

    public NBTTagCompound getNbt() {
        return nbt;
    }

    public int getWindowId() {
        return windowId;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public InventorySMessage(){

    }

    public InventorySMessage(String inventoryId ,int windowId,NBTTagCompound nbt){
        this.inventoryId = inventoryId;
        this.windowId = windowId;
        this.nbt = nbt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            PacketBuffer packetBuffer = new PacketBuffer(buf);
            this.windowId = packetBuffer.readInt();
            this.inventoryId = packetBuffer.readString(256);
            this.nbt = packetBuffer.readCompoundTag();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeInt(this.windowId);
        packetBuffer.writeString(this.inventoryId);
        packetBuffer.writeCompoundTag(this.nbt);
    }
}
