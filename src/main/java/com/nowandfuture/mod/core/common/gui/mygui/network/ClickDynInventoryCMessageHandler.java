package com.nowandfuture.mod.core.common.gui.mygui.network;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClickDynInventoryCMessageHandler implements IMessageHandler<ClickDynInventoryCMessage, IMessage> {

    @Override
    public IMessage onMessage(ClickDynInventoryCMessage message, MessageContext ctx) {
        final EntityPlayerMP player = NetworkHandler.getServerPlayer(ctx);
        IThreadListener scheduler =  player.getServerWorld();

        scheduler.addScheduledTask(new Runnable() {
            @Override
            public void run() {

                String inventoryId = message.getInventoryId();
                player.markPlayerActive();

                if(player.openContainer instanceof AbstractContainer){
                    AbstractContainer container = (AbstractContainer) player.openContainer;
                    if(container.getDynInventoryId().equals(inventoryId)){

                        if (player.openContainer.windowId == message.getWindowId() && player.openContainer.getCanCraft(player))
                        {
                            if (player.isSpectator())
                            {
                                player.sendContainerToPlayer(container);
                            }
                            else
                            {
                                ItemStack stack = container.slotClickInExtSlot(message.getSlotId(), message.getPackedClickData(), message.getMode(), player);
                                if(ItemStack.areItemStacksEqualUsingNBTShareTag(message.getClickedItem(), stack)) {
                                    player.connection.sendPacket(new SPacketConfirmTransaction(message.getWindowId(), message.getActionNumber(), true));
                                    player.isChangingQuantityOnly = true;
                                    player.openContainer.detectAndSendChanges();
                                    player.updateHeldItem();
                                    player.isChangingQuantityOnly = false;
                                }else{
                                    player.sendContainerToPlayer(container);
                                }
                            }
                        }
                    }
                }
            }
        });


        return null;
    }
}
