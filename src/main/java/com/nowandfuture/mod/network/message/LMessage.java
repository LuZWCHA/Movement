package com.nowandfuture.mod.network.message;

import com.nowandfuture.mod.core.common.entities.*;
import com.nowandfuture.mod.core.movementbase.ModuleNode;
import com.nowandfuture.mod.core.prefab.ModuleNodeMap;
import com.nowandfuture.mod.core.transformers.TimeInterpolation;
import com.nowandfuture.mod.core.transformers.animation.Timeline;
import com.nowandfuture.mod.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import joptsimple.internal.Strings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Stack;

import static com.nowandfuture.mod.utils.math.MathHelper.getIntLowBits;


/**
 * For any entities can be located（unique） at minecraft by BlockPos
 */
public abstract class LMessage implements IMessage {
    private int x,y,z;
    private int tag;
    public short flag;

    public LMessage(){
        tag = -1;
    }

    public LMessage(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        tag = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if(buf == null) return;
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(tag);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setPos(BlockPos pos){
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();
    }

    public static class NBTMessage extends LMessage implements IMessageHandler<NBTMessage,IMessage>{
        public static final int TAG = 6;

        public static final short GUI_APPLY_TIMELINE_FLAG = 0x00;
        public static final short TRANSFORMED_BLOCK_FLAG = 0x01;
        public static final short GUI_CHANGE_INVENTORY = 0x02;
        public static final short GUI_REMOVE_NODE = 0x03;
        public static final short GUI_INTERPOLATION_FLAG = 0x04;

        public NBTTagCompound nbt;

        public NBTMessage(){
            setTag(TAG);
        }

        public NBTMessage(short flag ,NBTTagCompound nbt){
            this.flag = flag;
            this.nbt = nbt;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            super.fromBytes(buf);
            this.flag = buf.readShort();
            DataInputStream inputStream = new DataInputStream(new ByteBufInputStream(buf));
            this.nbt = null;
            try {
                this.nbt = CompressedStreamTools.read(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
            buf.writeShort(flag);
            DataOutputStream outputStream = new DataOutputStream(new ByteBufOutputStream(buf));
            if(nbt == null) nbt = new NBTTagCompound();
            try {
                CompressedStreamTools.write(nbt,outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public IMessage onMessage(NBTMessage message, MessageContext ctx) {
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
                                        NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                                    }
                                }
                                break;
                            case GUI_CHANGE_INVENTORY:
                                if(tileEntity instanceof TileEntityCoreModule){
                                    NBTTagCompound nbtTagCompound = message.nbt;
                                    if(nbtTagCompound != null) {
                                        ((TileEntityCoreModule) tileEntity).handleInventoryTag(nbtTagCompound);
                                        NetworkHandler.syncToTrackingClients(ctx, tileEntity,
                                                ((TileEntityCoreModule) tileEntity).getInventoryPacket()
                                        );
                                    }
                                }
                                break;
                            case GUI_REMOVE_NODE:
                                if(tileEntity instanceof TileEntityCoreModule){
                                    NBTTagCompound nbtTagCompound = message.nbt;
                                    if(nbtTagCompound != null) {
                                        ((TileEntityCoreModule) tileEntity).handleRemoveNodeTag(nbtTagCompound);
                                        NetworkHandler.syncToTrackingClients(ctx, tileEntity,
                                                tileEntity.getUpdatePacket()
                                        );
                                    }
                                }
                                break;
                            case GUI_INTERPOLATION_FLAG:
                                if(tileEntity instanceof TileEntityTimelineEditor){
                                    NBTTagCompound nbtTagCompound = message.nbt;
                                    if(nbtTagCompound != null) {
                                        int ti = nbtTagCompound.getInteger("TimeInterpolation");
                                        int li = nbtTagCompound.getInteger("LocationInterpolation");
                                        ((TileEntityTimelineEditor) tileEntity).setTransformerNodeAg(1,li);
                                        ((TileEntityTimelineEditor) tileEntity).setTimeInterpolation(TimeInterpolation.Type.values()[ti]);
                                        NetworkHandler.syncToTrackingClients(ctx, tileEntity);
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

    public static class VoidMessage extends LMessage implements IMessageHandler<VoidMessage,IMessage>{
        public static final int TAG = 5;

        public static final short GUI_RESTART_FLAG = 0x00;
        public static final short GUI_EXPORT_TIMELINE_FLAG = 0x01;
        public static final short START_FLAG = 0x02;
        public static final short GUI_SHOW_OR_HIDE_BLOCK_FLAG = 0x03;
        public static final short GUI_ENABLE_COLLISION_FLAG = 0x04;
        public static final short GUI_VIDEO_PLAYER_STATE_FLAG = 0x05;

        public static final short GUI_MODULE_REMOVE = 0x07;
        public static final short GUI_LIST_BACK = 0x08;

        public VoidMessage(){
            setTag(TAG);
        }

        public VoidMessage(short flag){
            this.flag = flag;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            super.fromBytes(buf);
            this.flag = buf.readShort();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
            buf.writeShort(flag);
        }

        @Override
        public IMessage onMessage(VoidMessage message, MessageContext ctx) {
            BlockPos blockPos = new BlockPos(message.getX(),message.getY(),message.getZ());
            TileEntity tileEntity = NetworkHandler.getServerWorld(ctx).getTileEntity(blockPos);
            final EntityPlayerMP player = NetworkHandler.getServerPlayer(ctx);

            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if(tileEntity != null) {
                        switch (message.flag) {
                            case GUI_RESTART_FLAG:
                                if (tileEntity instanceof TileEntityModule) {

                                    if (!((TileEntityModule) tileEntity).getLine().isEnable()) {
                                        ((TileEntityModule) tileEntity).getLine().restart();
                                    } else {
                                        ((TileEntityModule) tileEntity).getLine().stop();
                                    }
                                    ((TileEntityModule) tileEntity).enable();

                                    NetworkHandler.syncToTrackingClients(ctx, tileEntity,
                                            ((TileEntityModule) tileEntity).getTimelineUpdatePacket(
                                                    ((TileEntityModule) tileEntity).getLine().getTick(),
                                                    ((TileEntityModule) tileEntity).getLine().isEnable()
                                            )
                                    );
                                }
                                break;
                            case GUI_EXPORT_TIMELINE_FLAG:
                                if (tileEntity instanceof TileEntityTimelineEditor) {
                                    boolean empty = ((TileEntityTimelineEditor) tileEntity).getStackInSlot(1).isEmpty();
                                    NBTTagCompound compound = ((TileEntityTimelineEditor) tileEntity).getLine()
                                            .serializeNBT(new NBTTagCompound());
                                    if (compound != null && !empty) {

                                        ((TileEntityTimelineEditor) tileEntity)
                                                .getStackInSlot(1)
                                                .setTagCompound(compound);

                                        NetworkHandler.syncToTrackingClients(ctx, tileEntity);

                                        NBTTagCompound nbtTagCompound = new NBTTagCompound();
                                        nbtTagCompound.setString("content","导出成功");
                                        NetworkHandler.INSTANCE.sendMessageToPlayer(new GuiResponseMessage(nbtTagCompound),player);
                                    }
                                }
                                break;
                            case START_FLAG:
                                if (tileEntity instanceof TileEntityModule) {

                                    if (!((TileEntityModule) tileEntity).getLine().isEnable()) {
                                        ((TileEntityModule) tileEntity).getLine().start();
                                    } else {
                                        ((TileEntityModule) tileEntity).getLine().stop();
                                    }
                                    ((TileEntityModule) tileEntity).enable();

                                    NetworkHandler.syncToTrackingClients(ctx, tileEntity,
                                            ((TileEntityModule) tileEntity).getTimelineUpdatePacket(
                                                    ((TileEntityModule) tileEntity).getLine().getTick(),
                                                    ((TileEntityModule) tileEntity).getLine().isEnable()
                                            )
                                    );
                                }
                                break;
                            case GUI_SHOW_OR_HIDE_BLOCK_FLAG:
                                if (tileEntity instanceof TileEntityCoreModule) {

                                    if (((TileEntityCoreModule) tileEntity).isShowBlock()) {
                                        ((TileEntityCoreModule) tileEntity).setShowBlock(false);
                                    } else {
                                        ((TileEntityCoreModule) tileEntity).setShowBlock(true);
                                    }
                                    NetworkHandler.syncToTrackingClients(ctx, tileEntity,
                                            ((TileEntityCoreModule) tileEntity).getShowBlockPacket()
                                    );
                                }
                                break;
                            case GUI_ENABLE_COLLISION_FLAG:
                                if (tileEntity instanceof TileEntityCoreModule) {

                                    if (((TileEntityCoreModule) tileEntity).isEnableCollision()) {
                                        ((TileEntityCoreModule) tileEntity).setEnableCollision(false);
                                    } else {
                                        ((TileEntityCoreModule) tileEntity).setEnableCollision(true);
                                    }
                                    NetworkHandler.syncToTrackingClients(ctx, tileEntity,
                                            ((TileEntityCoreModule) tileEntity).getCollisionEnablePacket()
                                    );
                                }
                                break;
                            case GUI_VIDEO_PLAYER_STATE_FLAG:
                                if (tileEntity instanceof TileEntitySimplePlayer) {
                                    NetworkHandler.syncToTrackingClients(ctx, tileEntity);
                                }
                                break;
                            case GUI_MODULE_REMOVE:
                                if (tileEntity instanceof TileEntityCoreModule) {
                                    ModuleNodeMap map = ((TileEntityCoreModule) tileEntity).getCurModuleNode().getModuleMap();

                                    ((TileEntityCoreModule) tileEntity).removeModuleNode(map.size() - 1);

                                    NetworkHandler.syncToTrackingClients(ctx, tileEntity);
                                }
                                break;
                            case GUI_LIST_BACK:
                                if (tileEntity instanceof TileEntityCoreModule) {
                                    ((TileEntityCoreModule) tileEntity).pop();

                                    NetworkHandler.syncToTrackingClients(ctx, tileEntity);
                                }
                                break;

                        }
                    }
                }
            });

            return null;
        }
    }

    public static class FloatDataSyncMessage extends LMessage implements IMessageHandler<FloatDataSyncMessage,IMessage>{
        public static final int TAG = 3;

        public static final short PROGRESS_FLAG = 0x0000;

        public float data;

        public FloatDataSyncMessage(){
            super();
            setTag(TAG);
        }

        public FloatDataSyncMessage(short flag,float data){
            this();
            this.flag = flag;
            this.data = data;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            super.fromBytes(buf);
            this.flag = buf.readShort();
            this.data = buf.readFloat();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
            buf.writeShort(flag);
            buf.writeFloat(data);
        }

        public float getData() {
            return data;
        }

        public void setData(float data) {
            this.data = data;
        }

        public short getFlag() {
            return flag;
        }

        public void setFlag(short flag) {
            this.flag = flag;
        }

        @Override
        public IMessage onMessage(FloatDataSyncMessage message, MessageContext ctx) {
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

    public static class IntDataSyncMessage extends LMessage implements IMessageHandler<IntDataSyncMessage,IMessage>{
        public static final int TAG = 3;

        public static final short RESIZE_FLAG = 0x0000;
        public static final short GUI_PLAYER_FACING_ROTATE = 0x0001;
        public static final short GUI_PLAYER_SIZE_X = 0x0002;
        public static final short GUI_PLAYER_SIZE_Y = 0x0003;
//        public static final short GUI_NODE_TRANSFORM_AG = 0x0004;

        public int data;

        public IntDataSyncMessage(){
            super();
            setTag(TAG);
        }

        public IntDataSyncMessage(short flag,int data){
            this();
            this.flag = flag;
            this.data = data;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            super.fromBytes(buf);
            this.flag = buf.readShort();
            this.data = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
            buf.writeShort(flag);
            buf.writeInt(data);
        }

        public int getData() {
            return data;
        }

        public void setData(int data) {
            this.data = data;
        }

        public short getFlag() {
            return flag;
        }

        public void setFlag(short flag) {
            this.flag = flag;
        }

        @Override
        public IMessage onMessage(IntDataSyncMessage message, MessageContext ctx) {
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
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                                }
                                break;
                            case GUI_PLAYER_SIZE_X:
                                if(tileEntity instanceof TileEntitySimplePlayer){
                                    ((TileEntitySimplePlayer) tileEntity).setWidth(message.data);
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                                }
                                break;
                            case GUI_PLAYER_SIZE_Y:
                                if(tileEntity instanceof TileEntitySimplePlayer){
                                    ((TileEntitySimplePlayer) tileEntity).setHeight(message.data);
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                                }
                                break;
//                            case GUI_NODE_TRANSFORM_AG:
//                                if(tileEntity instanceof TileEntityTimelineEditor){
//                                    ((TileEntityTimelineEditor) tileEntity).setTransformerNodeAg(1,message.data);
//                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity);
//                                }
//                                break;

                        }


                    }
                }
            });


            return null;
        }
    }

    public static class StringDataSyncMessage extends LMessage implements IMessageHandler<StringDataSyncMessage,IMessage>{
        public static final int TAG = 3;

        public static final short CONSTRUCT_LOCK_FLAG = 0x00;
        public static final short GUI_CONSTRUCT_RENAME = 0x01;
        public static final short GUI_PLAYER_URL = 0x02;
        public static final short GUI_CLICK_NODE = 0x03;

        public String data;

        public StringDataSyncMessage(){
            super();
            setTag(TAG);
        }

        public StringDataSyncMessage(short flag,String data){
            this();
            this.flag = flag;
            this.data = data;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            PacketBuffer packetBuffer = new PacketBuffer(buf);
            super.fromBytes(buf);
            this.flag = packetBuffer.readShort();
            this.data = packetBuffer.readString(32767);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
            buf.writeShort(flag);
            if(data == null) data = Strings.EMPTY;

            PacketBuffer buffer = new PacketBuffer(buf);
            buffer.writeString(data);
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public short getFlag() {
            return flag;
        }

        public void setFlag(short flag) {
            this.flag = flag;
        }

        @Override
        public IMessage onMessage(StringDataSyncMessage message, MessageContext ctx) {

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
                            case GUI_CONSTRUCT_RENAME:
                                if(tileEntity instanceof TileEntityConstructor){
                                    ((TileEntityConstructor) tileEntity).setPrefabName(message.getData());
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                                }
                                break;
                            case GUI_PLAYER_URL:
                                if (tileEntity instanceof TileEntitySimplePlayer) {
                                    String url = message.data;
                                    ((TileEntitySimplePlayer) tileEntity).setUrl(url);
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                                }
                                break;
                            case GUI_CLICK_NODE:
                                if(tileEntity instanceof TileEntityCoreModule){
                                    String path = message.data;
                                    Stack<ModuleNode> stack = new Stack<>();
                                    boolean flag = ((TileEntityCoreModule) tileEntity).updateStackByPathString(path,stack);
                                    if(flag){
                                        ((TileEntityCoreModule) tileEntity).setNodeStack(stack);
                                    }
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                                }
                                break;

                        }


                    }
                }
            });


            return null;
        }
    }

    //client
    public static class LongDataMessage extends LMessage implements IMessageHandler<LongDataMessage,IMessage>{
        public static final int TAG = 0;

        public static final short GUI_TICK_SLIDE = 0x00;
        public static final short GUI_MODULE_ADD = 0x01;


        public long data;

        public LongDataMessage(){
            super();
            setTag(TAG);
        }

        public LongDataMessage(short flag,long data) {
            this();
            this.flag = flag;
            this.data = data;
        }

        public LongDataMessage(int x, int y, int z) {
            super(x, y, z);
            setTag(TAG);
        }

        public long getData() {
            return data;
        }

        public void setData(long data) {
            this.data = data;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            super.fromBytes(buf);
            flag = buf.readShort();
            data = buf.readLong();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
            buf.writeShort(flag);
            buf.writeLong(data);
        }

        @Override
        public IMessage onMessage(LongDataMessage message, MessageContext ctx) {
            final BlockPos blockPos = new BlockPos(message.getX(),message.getY(),message.getZ());
            final EntityPlayerMP player = NetworkHandler.getServerPlayer(ctx);
            final TileEntity tileEntity = NetworkHandler.getServerWorld(ctx).getTileEntity(blockPos);

            if(tileEntity != null){
                switch (message.flag){
                    case GUI_TICK_SLIDE:
                        if(tileEntity instanceof TileEntityCoreModule) {
                            ((TileEntityCoreModule) tileEntity).driveLine(message.data);
                            NetworkHandler.syncToTrackingClients(ctx,tileEntity,((TileEntityCoreModule) tileEntity).getTimelineUpdatePacket(message.data,((TileEntityCoreModule) tileEntity).getLine().isEnable()));
                        }
                        break;
                    case GUI_MODULE_ADD:
                        if (tileEntity instanceof TileEntityCoreModule) {
                            long posLong = message.data;
                            ((TileEntityCoreModule) tileEntity).createModuleNode(BlockPos.fromLong(posLong));
                            NetworkHandler.syncToTrackingClients(ctx, tileEntity);
                        }
                        break;
                }
            }

            return null;
        }

        public short getFlag() {
            return flag;
        }

        public void setFlag(short flag) {
            this.flag = flag;
        }
    }

    public static class TimeLineSyncMessage extends LongDataMessage {
        public static final int TAG = 1;

        //time line
        private Timeline.Mode mode = Timeline.Mode.STOP;
        private long totalTick;
        private TimeInterpolation.Type type;

        public TimeLineSyncMessage(){
            super();
            setTag(TAG);//"TimeLineSync"
        }

        public TimeLineSyncMessage(int x, int y, int z){
            super(x, y, z);
            setTag(TAG);//"TimeLineSync"
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
            buf.writeInt(mode.modeValue);
            buf.writeLong(totalTick);
            buf.writeInt(type.ordinal());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            super.fromBytes(buf);
            mode = mode.getMode(buf.readInt());
            totalTick = buf.readLong();
            try {
                type = TimeInterpolation.Type.values()[buf.readInt()];
            }catch (Exception e){
                type = TimeInterpolation.Type.LINEAR;
            }

        }

        public Timeline.Mode getMode() {
            return mode;
        }

        public void setMode(Timeline.Mode mode) {
            this.mode = mode;
        }

        public long getTotalTick() {
            return totalTick;
        }

        public void setTotalTick(long totalTick) {
            this.totalTick = totalTick;
        }

        public TimeInterpolation.Type getType() {
            return type;
        }

        public void setType(TimeInterpolation.Type type) {
            this.type = type;
        }
    }

    public static class ReplaceTransformersSyncMessage extends LMessage {
        public static final int TAG = 2;

        //time line
        private long partTypeId = 0;

        private int byteLength;
        private NBTBase compound;

        public ReplaceTransformersSyncMessage() {
            super();
            setTag(TAG);//"ReplacePart"
        }

        public ReplaceTransformersSyncMessage(int x, int y, int z) {
            super(x, y, z);
            setTag(TAG);//"ReplacePart"
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            super.fromBytes(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
        }

        public long getPartTypeId() {
            return partTypeId;
        }

        public void setPartTypeId(long partTypeId) {
            this.partTypeId = partTypeId;
        }

        public NBTBase getCompound() {
            return compound;
        }

        public void setCompound(NBTTagCompound compound) {
            this.compound = compound;
        }
    }
}
