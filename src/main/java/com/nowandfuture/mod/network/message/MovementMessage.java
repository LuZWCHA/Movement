package com.nowandfuture.mod.network.message;

import com.nowandfuture.mod.core.common.entities.TileEntityConstructor;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.common.entities.TileEntityShowModule;
import com.nowandfuture.mod.core.common.entities.TileEntityTimelineEditor;
import com.nowandfuture.mod.core.transformers.TimeInterpolation;
import com.nowandfuture.mod.core.transformers.animation.TimeLine;
import com.nowandfuture.mod.handler.RegisterHandler;
import com.nowandfuture.mod.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import joptsimple.internal.Strings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.*;
import java.nio.charset.Charset;

import static com.nowandfuture.mod.utils.MathHelper.*;

public abstract class MovementMessage implements IMessage {
    private int x,y,z;
    private int tag;

    public MovementMessage(){
        tag = -1;
    }

    public MovementMessage(int x, int y, int z){
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

    public static class NBTMessage extends MovementMessage implements IMessageHandler<NBTMessage,IMessage>{
        public static final int TAG = 6;

        public static final short GUI_APPLY_TIMELINE_FLAG = 0x0000;

        private short flag;
        private NBTTagCompound nbt;

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

                        }
                    }
                }
            });

            return null;
        }
    }

    public static class VoidMessage extends MovementMessage implements IMessageHandler<VoidMessage,IMessage>{
        public static final int TAG = 5;

        public static final short GUI_RESTART_FLAG = 0x0000;
        public static final short GUI_EXPORT_TIMELINE_FLAG = 0x0001;
        public static final short GUI_START_FLAG = 0x0002;
        public static final short GUI_SHOW_OR_HIDE_BLOCK_FLAG = 0x0003;
        public static final short GUI_ENABLE_COLLISION_FLAG = 0x0004;

        private short flag;

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
                                    NBTTagCompound compound = ((TileEntityTimelineEditor)tileEntity).getLine().serializeNBT(new NBTTagCompound());
                                    if(compound != null && empty){
                                        ItemStack output = new ItemStack(RegisterHandler.timelineItem);
                                        output.setCount(1);
                                        output.setTagCompound(compound);
                                        ((TileEntityTimelineEditor) tileEntity).setInventorySlotContents(1,output);
                                        tileEntity.markDirty();
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
                                if(tileEntity instanceof TileEntityShowModule){

                                    if(((TileEntityShowModule) tileEntity).isShowBlock()) {
                                        ((TileEntityShowModule) tileEntity).setShowBlock(false);
                                    }else{
                                        ((TileEntityShowModule) tileEntity).setShowBlock(true);
                                    }
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,
                                            ((TileEntityShowModule) tileEntity).getShowBlockPacket()
                                    );
                                }
                                break;
                            case GUI_ENABLE_COLLISION_FLAG:
                                if(tileEntity instanceof TileEntityShowModule){

                                    if(((TileEntityShowModule) tileEntity).isEnableCollision()) {
                                        ((TileEntityShowModule) tileEntity).setEnableCollision(false);
                                    }else{
                                        ((TileEntityShowModule) tileEntity).setEnableCollision(true);
                                    }
                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,
                                            ((TileEntityShowModule) tileEntity).getCollisionEnablePacket()
                                    );
                                }
                        }
                    }
                }
            });

            return null;
        }
    }


    public static class RenamePrefabMessage extends MovementMessage implements IMessageHandler<RenamePrefabMessage,IMessage>{
        public static final int TAG = 4;
        private String name;

        public RenamePrefabMessage(){
            setTag(TAG);
        }

        public RenamePrefabMessage(BlockPos pos, String name) {
            this(pos.getX(), pos.getY(), pos.getZ(),name);
        }

        public RenamePrefabMessage(int x, int y, int z) {
            super(x, y, z);
            setTag(TAG);
        }

        public RenamePrefabMessage(int x, int y, int z,String name) {
            this(x, y, z);
            this.name = name;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            super.fromBytes(buf);
            name = Strings.EMPTY;
            int length = buf.readInt();
            if(length > 0)
                name = buf.readCharSequence(length,Charset.forName("UTF8")).toString();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
            if(name == null) name = Strings.EMPTY;
            buf.writeInt(name.length());
            if(name.length() > 0)
                buf.writeCharSequence(name,Charset.forName("UTF8"));
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public IMessage onMessage(RenamePrefabMessage message, MessageContext ctx) {
            BlockPos blockPos = new BlockPos(message.getX(),message.getY(),message.getZ());
            final EntityPlayerMP player = NetworkHandler.getServerPlayer(ctx);
            TileEntity tileEntity = NetworkHandler.getServerWorld(ctx).getTileEntity(blockPos);;

            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if(tileEntity != null){
                        if(tileEntity instanceof TileEntityConstructor){
                            ((TileEntityConstructor) tileEntity).setPrefabName(message.getName());
                            NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                        }
                    }
                }
            });

            return null;
        }
    }


    public static class FloatDataSyncMessage extends MovementMessage implements IMessageHandler<FloatDataSyncMessage,IMessage>{
        public static final int TAG = 3;

        public static final short PROGRESS_FLAG = 0x0000;

        private short flag;
        private float data;

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

    public static class IntDataSyncMessage extends MovementMessage implements IMessageHandler<IntDataSyncMessage,IMessage>{
        public static final int TAG = 3;

        public static final short RESIZE_FLAG = 0x0000;

        private short flag;
        private int data;

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
                                        ((TileEntityConstructor) tileEntity).getAABBSelectArea()
                                                    .setMaxX(offset);
                                            break;
                                        case 1:
                                            ((TileEntityConstructor) tileEntity).getAABBSelectArea()
                                                    .setMaxY(offset);
                                            break;
                                        case 2:
                                            ((TileEntityConstructor) tileEntity).getAABBSelectArea()
                                                    .setMaxZ(offset);
                                            break;
                                        case 4:
                                            ((TileEntityConstructor) tileEntity).getAABBSelectArea()
                                                    .setMaxX(offset);
                                            break;
                                        case 5:
                                            ((TileEntityConstructor) tileEntity).getAABBSelectArea()
                                                    .setMaxY(offset);
                                            break;
                                        case 6:
                                            ((TileEntityConstructor) tileEntity).getAABBSelectArea()
                                                    .setMaxZ(offset);
                                            break;
                                    }

                                    NetworkHandler.syncToTrackingClients(ctx,tileEntity,((TileEntityConstructor) tileEntity).getResizeUpdatePacket());
                                }

                        }


                    }
                }
            });


            return null;
        }
    }

    public static class StringDataSyncMessage extends MovementMessage implements IMessageHandler<StringDataSyncMessage,IMessage>{
        public static final int TAG = 3;

        public static final short CONSTRUCT_LOCK_FLAG = 0x0000;

        private short flag;
        private String data;

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
            super.fromBytes(buf);
            this.flag = buf.readShort();
            int length = buf.readInt();
            if(length > 0)
                this.data = buf.readCharSequence(length,Charset.forName("UTF-8")).toString();
            else
                this.data = Strings.EMPTY;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
            buf.writeShort(flag);
            if(data == null) data = "";
            buf.writeInt(data.length());
            buf.writeCharSequence(data,Charset.forName("UTF-8"));
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


                        }


                    }
                }
            });


            return null;
        }
    }

    //client
    public static class LongDataMessage extends MovementMessage implements IMessageHandler<LongDataMessage,IMessage>{
        public static final int TAG = 0;

        public static final short GUI_TICK_SLIDE = 0x0000;

        private short flag;
        private long data;

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
                        if(tileEntity instanceof TileEntityShowModule) {
                            ((TileEntityShowModule) tileEntity).getLine().setTick(message.data);
                            NetworkHandler.syncToTrackingClients(ctx,tileEntity,((TileEntityShowModule) tileEntity).getTimelineUpdatePacket(message.data,((TileEntityShowModule) tileEntity).getLine().isEnable()));
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
        private TimeLine.Mode mode = TimeLine.Mode.STOP;
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

        public TimeLine.Mode getMode() {
            return mode;
        }

        public void setMode(TimeLine.Mode mode) {
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

    public static class ReplaceTransformersSyncMessage extends MovementMessage {
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
