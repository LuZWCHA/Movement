package com.nowandfuture.mod.network.message;

import com.nowandfuture.mod.core.transformers.animation.TimeLine;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class MoveStateMessage implements IMessage {
    private int x,y,z;
    private int tag;

    public MoveStateMessage(int x,int y,int z){
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

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public static class ForceSyncTickMessage extends MoveStateMessage {
        private long tick;

        public ForceSyncTickMessage(int x, int y, int z) {
            super(x, y, z);
            setTag(0);//"ForceSyncTick"
        }

        public long getTick() {
            return tick;
        }

        public void setTick(long tick) {
            this.tick = tick;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            super.fromBytes(buf);
            tick = buf.readLong();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
            buf.writeLong(tick);
        }
    }

    public static class ModifyAttributeMessage extends MoveStateMessage{
        //time line
        private TimeLine.Mode mode = TimeLine.Mode.STOP;
        private long totalTick;

        public ModifyAttributeMessage(int x,int y,int z){
            super(x, y, z);
            setTag(1);//"ModifyAttribute"
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
            buf.writeInt(mode.modeValue);
            buf.writeLong(totalTick);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            super.fromBytes(buf);
            mode = mode.getMode(buf.readInt());
            totalTick = buf.readLong();
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
    }

    public static class ReplacePartMessage extends ModifyAttributeMessage{
        //time line
        private long partTypeId = 0;

        private int byteLength;
        private NBTBase compound;

        public ReplacePartMessage(int x, int y, int z) {
            super(x, y, z);
            setTag(2);//"ReplacePart"
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            super.fromBytes(buf);
            partTypeId = buf.readLong();
            if(partTypeId < 0) return;

            byteLength = buf.readInt();
            if(byteLength <= 0) return;

            ByteBuf data = buf.readBytes(byteLength);

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
