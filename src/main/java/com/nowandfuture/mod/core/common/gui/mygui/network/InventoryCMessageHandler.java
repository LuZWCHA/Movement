package com.nowandfuture.mod.core.common.gui.mygui.network;

import com.nowandfuture.mod.core.common.gui.mygui.api.IDynInventoryHolder;
import com.nowandfuture.mod.core.common.gui.mygui.api.IDynamicInventory;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class InventoryCMessageHandler implements IMessageHandler<InventoryCMessage, IMessage> {

    public InventoryCMessageHandler(){

    }

    @Override
    public IMessage onMessage(InventoryCMessage message, MessageContext ctx) {
        if(message.getFlag() == 0) {
            BlockPos blockPos = (BlockPos) message.getObject();
            TileEntity tileEntity = NetworkHandler.getServerWorld(ctx).getTileEntity(blockPos);
            final EntityPlayerMP player = NetworkHandler.getServerPlayer(ctx);
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (tileEntity instanceof IDynInventoryHolder) {
                        IDynamicInventory dynamicInventory = ((IDynInventoryHolder) tileEntity).getDynInventory();
                        //to drop items if a slot (not empty) is removed
                        dynamicInventory.readFromNBT(message.getNbt(), true);

                        NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                    }
                }
            });
        }

        return null;
    }
}
