package com.nowandfuture.mod.network.message;

import com.nowandfuture.mod.core.common.entities.*;
import com.nowandfuture.mod.network.NetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Map;

import static com.nowandfuture.mod.utils.math.MathHelper.getIntLowBits;

public class LMessageHandler<T extends LMessage,P extends IMessage> implements IMessageHandler<T,P> {
    private Map<Class<? extends LMessage>,LMessageHandler> handlers;

    @Override
    public P onMessage(T message, MessageContext ctx) {
        IMessage rep = null;
        if(!handlers.isEmpty()){
           rep = handlers.get(message.getClass()).onMessage(message,ctx);
        }
        return (P) rep;
    }

    public <HANDLER extends LMessageHandler> void register(Class<? extends LMessage> clazz,HANDLER handler){
        handlers.put(clazz,handler);
    }


    public static class NBTMessage extends LMessageHandler<LMessage.NBTMessage,IMessage>{
        public static final short GUI_APPLY_TIMELINE_FLAG = 0x0000;
        public static final short TRANSFORMED_BLOCK_FLAG = 0x0001;

        @Override
        public IMessage onMessage(LMessage.NBTMessage message, MessageContext ctx) {
            BlockPos blockPos = new BlockPos(message.getX(),message.getY(),message.getZ());
            TileEntity tileEntity = NetworkHandler.getServerWorld(ctx).getTileEntity(blockPos);
            final EntityPlayerMP player = NetworkHandler.getServerPlayer(ctx);

            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if(tileEntity != null) {
                        switch (message.flag){
                            case GUI_APPLY_TIMELINE_FLAG:
                                if(tileEntity instanceof TileEntityModule){
                                    NBTTagCompound nbtTagCompound = message.nbt;
                                    if(nbtTagCompound != null){
                                        ((TileEntityModule) tileEntity).getLine().deserializeNBT(nbtTagCompound);
                                        NetworkHandler.syncToTrackingClients(ctx,tileEntity,((TileEntityModule) tileEntity).getTimelineModifyPacket());
                                    }

                                }
                                break;
                            case TRANSFORMED_BLOCK_FLAG:
                                if(tileEntity instanceof TileEntityTransformedBlock){
                                    NBTTagCompound nbtTagCompound = message.nbt;
                                    if(nbtTagCompound != null){
                                        tileEntity.readFromNBT(nbtTagCompound);
                                        NetworkHandler.syncToTrackingClients(ctx,tileEntity,tileEntity.getUpdatePacket());
                                    }
                                }
                                break;

                        }
                    }
                }
            });

            return null;
        }
    }

    public static class VoidMessage extends LMessageHandler<LMessage.VoidMessage,IMessage>{
        public static final short GUI_RESTART_FLAG = 0x0000;
        public static final short GUI_EXPORT_TIMELINE_FLAG = 0x0001;
        public static final short GUI_START_FLAG = 0x0002;
        public static final short GUI_SHOW_OR_HIDE_BLOCK_FLAG = 0x0003;
        public static final short GUI_ENABLE_COLLISION_FLAG = 0x0004;
        public static final short GUI_VIDEO_PLAYER_STATE_FLAG = 0x0005;

        @Override
        public IMessage onMessage(LMessage.VoidMessage message, MessageContext ctx) {

            BlockPos blockPos = new BlockPos(message.getX(),message.getY(),message.getZ());
            TileEntity tileEntity = NetworkHandler.getServerWorld(ctx).getTileEntity(blockPos);
            final EntityPlayerMP player = NetworkHandler.getServerPlayer(ctx);

            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if(tileEntity != null){
                        switch (message.flag){
                            case GUI_RESTART_FLAG:
                                if(tileEntity instanceof TileEntityModule){

                                    if(!((TileEntityModule) tileEntity).getLine().isEnable()) {
                                        ((TileEntityModule) tileEntity).getLine().restart();
                                    }else{
                                        ((TileEntityModule) tileEntity).getLine().stop();
                                    }
                                    ((TileEntityModule) tileEntity).enable();

                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,
                                            ((TileEntityModule) tileEntity).getTimelineUpdatePacket(
                                                    ((TileEntityModule) tileEntity).getLine().getTick(),
                                                    ((TileEntityModule) tileEntity).getLine().isEnable()
                                            )
                                    );
                                }
                                break;
                            case GUI_EXPORT_TIMELINE_FLAG:
                                if(tileEntity instanceof TileEntityTimelineEditor){
                                    boolean empty =((TileEntityTimelineEditor) tileEntity).getStackInSlot(1).isEmpty();
                                    NBTTagCompound compound = ((TileEntityTimelineEditor)tileEntity).getLine()
                                            .serializeNBT(new NBTTagCompound());
                                    if(compound != null && !empty){

                                        ((TileEntityTimelineEditor) tileEntity)
                                                .getStackInSlot(1)
                                                .setTagCompound(compound);

                                        NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                                    }
                                }
                                break;
                            case GUI_START_FLAG:
                                if(tileEntity instanceof TileEntityModule){

                                    if(!((TileEntityModule) tileEntity).getLine().isEnable()) {
                                        ((TileEntityModule) tileEntity).getLine().start();
                                    }else{
                                        ((TileEntityModule) tileEntity).getLine().stop();
                                    }
                                    ((TileEntityModule) tileEntity).enable();

                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,
                                            ((TileEntityModule) tileEntity).getTimelineUpdatePacket(
                                                    ((TileEntityModule) tileEntity).getLine().getTick(),
                                                    ((TileEntityModule) tileEntity).getLine().isEnable()
                                            )
                                    );
                                }
                                break;
                            case GUI_SHOW_OR_HIDE_BLOCK_FLAG:
                                if(tileEntity instanceof TileEntityCoreModule){

                                    if(((TileEntityCoreModule) tileEntity).isShowBlock()) {
                                        ((TileEntityCoreModule) tileEntity).setShowBlock(false);
                                    }else{
                                        ((TileEntityCoreModule) tileEntity).setShowBlock(true);
                                    }
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,
                                            ((TileEntityCoreModule) tileEntity).getShowBlockPacket()
                                    );
                                }
                                break;
                            case GUI_ENABLE_COLLISION_FLAG:
                                if(tileEntity instanceof TileEntityCoreModule){

                                    if(((TileEntityCoreModule) tileEntity).isEnableCollision()) {
                                        ((TileEntityCoreModule) tileEntity).setEnableCollision(false);
                                    }else{
                                        ((TileEntityCoreModule) tileEntity).setEnableCollision(true);
                                    }
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,
                                            ((TileEntityCoreModule) tileEntity).getCollisionEnablePacket()
                                    );
                                }
                                break;
                            case GUI_VIDEO_PLAYER_STATE_FLAG:
                                if(tileEntity instanceof TileEntitySimplePlayer){
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,
                                            ((TileEntitySimplePlayer) tileEntity).getUpdatePacket()
                                    );
                                }
                                break;

                        }
                    }
                }
            });

            return null;
        }
    }

    public static class FloatDataSyncMessage extends LMessageHandler<LMessage.FloatDataSyncMessage,IMessage>{
        public static final int TAG = 3;

        public static final short PROGRESS_FLAG = 0x0000;
        @Override
        public IMessage onMessage(LMessage.FloatDataSyncMessage message, MessageContext ctx) {
            final BlockPos blockPos = new BlockPos(message.getX(),message.getY(),message.getZ());
            final EntityPlayerMP player = NetworkHandler.getServerPlayer(ctx);
            final TileEntity tileEntity = NetworkHandler.getServerWorld(ctx).getTileEntity(blockPos);

            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if(tileEntity != null){

                        switch (message.flag) {
                            case PROGRESS_FLAG:
                                if (tileEntity instanceof TileEntityConstructor) {
                                    ((TileEntityConstructor) tileEntity).setConstructProgress(message.data);
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,
                                            ((TileEntityConstructor) tileEntity).getProgressUpdatePacket());
                                }
                                break;
                        }

                    }
                }
            });


            return null;
        }
    }

    public static class IntDataSyncMessage extends LMessageHandler<LMessage.IntDataSyncMessage,IMessage>{
        public static final short RESIZE_FLAG = 0x0000;
        public static final short GUI_PLAYER_FACING_ROTATE = 0x0001;
        public static final short GUI_PLAYER_SIZE_X = 0x0002;
        public static final short GUI_PLAYER_SIZE_Y = 0x0003;

        @Override
        public IMessage onMessage(LMessage.IntDataSyncMessage message, MessageContext ctx) {
            final BlockPos blockPos = new BlockPos(message.getX(),message.getY(),message.getZ());
            final EntityPlayerMP player = NetworkHandler.getServerPlayer(ctx);
            final TileEntity tileEntity = NetworkHandler.getServerWorld(ctx).getTileEntity(blockPos);
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {

                    if(tileEntity != null){
                        switch (message.flag) {
                            case RESIZE_FLAG:
                                if (tileEntity instanceof TileEntityConstructor) {
                                    int offset = getIntLowBits(message.data,16);
                                    if(offset <= 0)return;

                                    int p = (message.data >>> 16) & 0x00000003;
                                    switch (p) {
                                        case 0:
                                        case 4:
                                            ((TileEntityConstructor) tileEntity).getAABBSelectArea()
                                                    .setMaxX(offset);
                                            break;
                                        case 1:
                                        case 5:
                                            ((TileEntityConstructor) tileEntity).getAABBSelectArea()
                                                    .setMaxY(offset);
                                            break;
                                        case 2:
                                        case 6:
                                            ((TileEntityConstructor) tileEntity).getAABBSelectArea()
                                                    .setMaxZ(offset);
                                            break;
                                    }

                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,((TileEntityConstructor) tileEntity).getResizeUpdatePacket());
                                }
                                break;
                            case GUI_PLAYER_FACING_ROTATE:
                                if(tileEntity instanceof TileEntitySimplePlayer){
                                    ((TileEntitySimplePlayer) tileEntity).setFacing(EnumFacing.values()[message.data]);
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,tileEntity.getUpdatePacket());
                                }
                                break;
                            case GUI_PLAYER_SIZE_X:
                                if(tileEntity instanceof TileEntitySimplePlayer){
                                    ((TileEntitySimplePlayer) tileEntity).setWidth(message.data);
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,tileEntity.getUpdatePacket());
                                }
                                break;
                            case GUI_PLAYER_SIZE_Y:
                                if(tileEntity instanceof TileEntitySimplePlayer){
                                    ((TileEntitySimplePlayer) tileEntity).setHeight(message.data);
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,tileEntity.getUpdatePacket());
                                }
                                break;

                        }


                    }
                }
            });


            return null;
        }
    }

    public static class StringDataSyncMessage extends LMessageHandler<LMessage.StringDataSyncMessage,IMessage>{
        public static final short CONSTRUCT_LOCK_FLAG = 0x0000;
        public static final short GUI_PLAYER_URL = 0x0001;

        @Override
        public IMessage onMessage(LMessage.StringDataSyncMessage message, MessageContext ctx) {

            final BlockPos blockPos = new BlockPos(message.getX(),message.getY(),message.getZ());
            final EntityPlayerMP player = NetworkHandler.getServerPlayer(ctx);
            final TileEntity tileEntity = NetworkHandler.getServerWorld(ctx).getTileEntity(blockPos);
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {

                    if(tileEntity != null){

                        switch (message.flag) {
                            case CONSTRUCT_LOCK_FLAG:
                                if (tileEntity instanceof TileEntityConstructor) {
                                    if(!((TileEntityConstructor) tileEntity).isLock()) {
                                        ((TileEntityConstructor) tileEntity).setLock(true);
                                        ((TileEntityConstructor) tileEntity).setLockUserName(message.data);
                                        player.sendMessage(new TextComponentString("You has lock the constructor"));
                                        NetworkHandler.syncToTrackingClients(ctx,tileEntity);

                                    } else {
                                        if(!player.getName().equals(message.data))
                                            player.sendMessage(new TextComponentString("This constructor has been locked by "+
                                                    ((TileEntityConstructor) tileEntity).getLockUserName()));
                                        else{
                                            //update for player again
                                            ((TileEntityConstructor) tileEntity).setLock(false);
                                            NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                                        }
                                    }
                                }
                                break;
                            case GUI_PLAYER_URL:
                                if (tileEntity instanceof TileEntitySimplePlayer) {
                                    String url = message.data;
                                    ((TileEntitySimplePlayer) tileEntity).setUrl(url);
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                                }

                        }


                    }
                }
            });


            return null;
        }
    }

    //client
    public static class LongDataMessage extends LMessageHandler<LMessage.LongDataMessage,IMessage>{
        public static final short GUI_TICK_SLIDE = 0x0000;
        @Override
        public IMessage onMessage(LMessage.LongDataMessage message, MessageContext ctx) {
            final BlockPos blockPos = new BlockPos(message.getX(),message.getY(),message.getZ());
            final EntityPlayerMP player = NetworkHandler.getServerPlayer(ctx);
            final TileEntity tileEntity = NetworkHandler.getServerWorld(ctx).getTileEntity(blockPos);

            if(tileEntity != null){
                switch (message.flag){
                    case GUI_TICK_SLIDE:
                        if(tileEntity instanceof TileEntityCoreModule) {
                            ((TileEntityCoreModule) tileEntity).getLine().setTick(message.data);
                            System.out.println(message.data);
                            NetworkHandler.syncToTrackingClients(ctx,tileEntity,((TileEntityCoreModule) tileEntity).getTimelineUpdatePacket(message.data,((TileEntityCoreModule) tileEntity).getLine().isEnable()));
                        }
                        break;
                }
            }

            return null;
        }

    }

}
