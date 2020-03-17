package com.nowandfuture.mod.core.common.gui.mygui.network;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class InventorySMessageHandler implements IMessageHandler<InventorySMessage, IMessage> {
    @Override
    public IMessage onMessage(InventorySMessage message, MessageContext ctx) {
        final EntityPlayerSP player = Minecraft.getMinecraft().player;
        Minecraft.getMinecraft().addScheduledTask(
                new Runnable() {
                    @Override
                    public void run() {
                        Container container = player.openContainer;

                        if(container instanceof AbstractContainer &&
                                container.windowId == message.getWindowId()){
                            ((AbstractContainer) container).setAllForDymInventory(message.getNbt());
                        }
                    }
                }
        );

        return null;
    }
}
