package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.core.common.gui.mygui.SlotMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public enum NetworkHandler {
    INSTANCE;
    private SimpleNetworkWrapper wrapper;
    private static String channelName;
    
    static {
        channelName = "_slot_channel";
    }
    
    NetworkHandler(){
        
    }
    
    public void init(String modId){
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(modId + channelName);
        wrapper.registerMessage(SlotMessage.class,SlotMessage.class,0, Side.SERVER);
    }

    public void sendMessageToDim(IMessage msg, int dim) {
        wrapper.sendToDimension(msg, dim);
    }

    public void sendMessageAroundPos(IMessage msg, int dim, BlockPos pos) {

        wrapper.sendToAllAround(msg, new NetworkRegistry.TargetPoint(dim, pos.getX(), pos.getY(), pos.getZ(), 2.0D));
    }

    public void sendMessageToPlayer(IMessage msg, EntityPlayerMP player) {
        wrapper.sendTo(msg, player);
    }

    public void sendMessageToAll(IMessage msg) {
        wrapper.sendToAll(msg);
    }

    public void sendMessageToServer(IMessage msg) {
        wrapper.sendToServer(msg);
    }

    public void sendMessageToAllTracking(IMessage msg, Entity entity) {
        wrapper.sendToAllTracking(msg, entity);
    }

    public void sendMessageToAllTracking(IMessage msg,NetworkRegistry.TargetPoint point) {
        wrapper.sendToAllTracking(msg,point);
    }

    public static World getServerWorld(MessageContext context) {
        return context.getServerHandler().player.world;
    }

    public static EntityPlayerMP getServerPlayer(MessageContext context) {
        return context.getServerHandler().player;
    }


    public static void syncToTrackingClients(MessageContext context, TileEntity tileEntity) {
        syncToTrackingClients(context, tileEntity,tileEntity.getUpdatePacket());
    }

    public static void syncToTrackingClients(MessageContext context, TileEntity tileEntity, SPacketUpdateTileEntity customPacket) {
        World world = getServerWorld(context);
        syncToTrackingClients(world,tileEntity,customPacket);
    }

    public static void syncToTrackingClients(World world,TileEntity tileEntity,SPacketUpdateTileEntity customPacket) {
        if (!world.isRemote) {
            PlayerChunkMapEntry trackingEntry = ((WorldServer)world).getPlayerChunkMap()
                    .getEntry(tileEntity.getPos().getX() >> 4, tileEntity.getPos().getZ() >> 4);
            if (trackingEntry != null) {
                for (EntityPlayerMP player : trackingEntry.getWatchingPlayers()) {
                    if(customPacket != null)
                        player.connection.sendPacket(customPacket);
                }
            }
        }
    }

    public static void syncToOnePlayer(EntityPlayerMP player,TileEntity tileEntity) {
        if (!player.world.isRemote) {
            SPacketUpdateTileEntity packet = tileEntity.getUpdatePacket();

            if(packet != null)
                player.connection.sendPacket(packet);
        }
    }
}
