package com.nowandfuture.mod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public enum NetworkHandler {
    INSTANCE;

    NetworkHandler(){

    }

    @SubscribeEvent
    public void handlerMessage(IMessage message){

    }

    public void sendMessage(String message){
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(message));
    }

}
