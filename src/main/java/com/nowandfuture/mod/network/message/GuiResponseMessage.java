package com.nowandfuture.mod.network.message;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GuiResponseMessage  implements IMessage,IMessageHandler<GuiResponseMessage,IMessage> {

    public NBTTagCompound nbt;

    public GuiResponseMessage(){
    }

    public GuiResponseMessage(NBTTagCompound nbtTagCompound){
        this.nbt = nbtTagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {

        this.nbt = null;
        try (DataInputStream inputStream = new DataInputStream(new ByteBufInputStream(buf))){
            this.nbt = CompressedStreamTools.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {

        if(nbt == null) nbt = new NBTTagCompound();
        try (DataOutputStream outputStream = new DataOutputStream(new ByteBufOutputStream(buf))){
            CompressedStreamTools.write(nbt,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IMessage onMessage(GuiResponseMessage message, MessageContext ctx) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if(screen!=null){
            if(screen instanceof AbstractGuiContainer){
                ((AbstractGuiContainer) screen).post(new RootView.TextTipEvent(message.nbt.getString("content"),1000,2));
            }
        }
        return null;
    }
}
