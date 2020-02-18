package com.nowandfuture.mod.network;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.network.message.DivBytesMessage;
import com.nowandfuture.mod.network.message.LMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum NetworkHandler {
    INSTANCE;
    private SimpleNetworkWrapper channel;

    NetworkHandler(){

    }

    public void init(){
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(Movement.MODID);
        int baseId = 0;
        channel.registerMessage(LMessage.RenamePrefabMessage.class, LMessage.RenamePrefabMessage.class, baseId++,Side.SERVER);
        channel.registerMessage(DivBytesMessage.class,DivBytesMessage.class,baseId++,Side.SERVER);
        channel.registerMessage(LMessage.FloatDataSyncMessage.class, LMessage.FloatDataSyncMessage.class,baseId++,Side.SERVER);
        channel.registerMessage(LMessage.StringDataSyncMessage.class, LMessage.StringDataSyncMessage.class,baseId++,Side.SERVER);
        channel.registerMessage(LMessage.IntDataSyncMessage.class, LMessage.IntDataSyncMessage.class,baseId++,Side.SERVER);
        channel.registerMessage(LMessage.VoidMessage.class, LMessage.VoidMessage.class,baseId++,Side.SERVER);
        channel.registerMessage(LMessage.NBTMessage.class, LMessage.NBTMessage.class,baseId++,Side.SERVER);
        channel.registerMessage(LMessage.LongDataMessage.class, LMessage.LongDataMessage.class,baseId++,Side.SERVER);
    }

    @SideOnly(Side.CLIENT)
    public void sendClientCommandMessage(String message){
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(message));
    }

    @SideOnly(Side.CLIENT)
    public void sendClientChatMessage(String message){
        Minecraft.getMinecraft().player.sendChatMessage(message);
    }

    public void sendMessageToDim(IMessage msg, int dim) {
        channel.sendToDimension(msg, dim);
    }

    public void sendMessageAroundPos(IMessage msg, int dim, BlockPos pos) {

        channel.sendToAllAround(msg, new NetworkRegistry.TargetPoint(dim, pos.getX(), pos.getY(), pos.getZ(), 2.0D));
    }

    public void sendMessageToPlayer(IMessage msg, EntityPlayerMP player) {
        channel.sendTo(msg, player);
    }

    public void sendMessageToAll(IMessage msg) {
        channel.sendToAll(msg);
    }

    public void sendMessageToServer(IMessage msg) {
        channel.sendToServer(msg);
    }

    public void sendMessageToAllTracking(IMessage msg,Entity entity) {
        channel.sendToAllTracking(msg, entity);
    }

    public void sendMessageToAllTracking(IMessage msg,NetworkRegistry.TargetPoint point) {
        channel.sendToAllTracking(msg,point);
    }

    public static World getServerWorld(MessageContext context) {
         return context.getServerHandler().player.world;
    }

    public static EntityPlayerMP getServerPlayer(MessageContext context) {
        return context.getServerHandler().player;
    }


    public static void syncToTrackingClients(MessageContext context,TileEntity tileEntity) {
        syncToTrackingClients(context, tileEntity,tileEntity.getUpdatePacket());
    }

    public static void syncToTrackingClients(MessageContext context,TileEntity tileEntity,SPacketUpdateTileEntity customPacket) {
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
