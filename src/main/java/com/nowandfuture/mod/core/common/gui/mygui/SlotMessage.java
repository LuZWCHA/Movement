package com.nowandfuture.mod.core.common.gui.mygui;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SlotMessage implements IMessage, IMessageHandler<SlotMessage,IMessage> {

    private int windowId;
    private long slotId;
    private int slotType;
    //0:add 1:remove
    private int option;

    private SlotMessage(){

    }

    public SlotMessage(int windowId, long slotId, int slotType, int option) {
        this.windowId = windowId;
        this.slotId = slotId;
        this.slotType = slotType;
        this.option = option;
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    @Override
    public IMessage onMessage(SlotMessage message, MessageContext ctx) {

        return null;
    }
}
