package com.nowandfuture.mod.network.message;

import com.nowandfuture.mod.core.common.entities.TileEntityConstructor;
import com.nowandfuture.mod.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DivBytesMessage implements IMessage,IMessageHandler<DivBytesMessage,IMessage> {

    public interface DivBytesGetter{
        void get(int index,int divNum,byte[] bytes) throws Exception;
    }

    public interface DivBytesSetter{
        void setter(int index,int divNum,byte[] bytes);
    }

    private int x,y,z;
    private byte[] byteArray;
    private int index;
    private int total;

    public DivBytesMessage(byte[] bytes, int index, int total){
        this();
        byteArray = bytes;
        this.index = index;
        this.total = total;
    }

    public DivBytesMessage(){
        index = 0;
        total = 0;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        index = buf.readInt();
        total = buf.readInt();
        int length = buf.readInt();
        if(length > 0) {
            byteArray = new byte[length];
            buf.readBytes(byteArray);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(index);
        buf.writeInt(total);
        if(byteArray != null) {
            buf.writeInt(byteArray.length);
            buf.writeBytes(byteArray);
        }else{
            buf.writeInt(0);
        }
    }

    public void setPos(BlockPos blockPos){
        x = blockPos.getX();
        y = blockPos.getY();
        z = blockPos.getZ();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public IMessage onMessage(DivBytesMessage message, MessageContext ctx) {

        final BlockPos blockPos = new BlockPos(message.getX(),message.getY(),message.getZ());
        final EntityPlayerMP player = NetworkHandler.getServerPlayer(ctx);
        final TileEntity tileEntity = NetworkHandler.getServerWorld(ctx).getTileEntity(blockPos);

        player.getServerWorld().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                if(tileEntity != null){
                    if(tileEntity instanceof TileEntityConstructor){

                        if(((TileEntityConstructor) tileEntity).isEmpty()) return;

                        if(!((TileEntityConstructor) tileEntity).isRevTag()) {
                            //check rev(abnormal rev packet which index is not 0 when rev not start)
                            if(message.index == 0) {
                                //start new rev
                                ((TileEntityConstructor) tileEntity).setRevTag(true);
                                ((TileEntityConstructor) tileEntity).setLastRevIndex(-1);
                            }else {
                                return;
                            }
                        }

                        if(((TileEntityConstructor) tileEntity).isRevTag()){
                            //check packet order
                            if(((TileEntityConstructor) tileEntity).getLastRevIndex() + 1 != message.index){
                                ((TileEntityConstructor) tileEntity).setRevTag(false);//end rev
                                return;
                            }
                        }

                        if(((TileEntityConstructor) tileEntity).revItemDivBytes(
                                message.index,message.total,message.byteArray)){

                            ((TileEntityConstructor) tileEntity).setRevTag(false);//end rev

                            //sync all clients'chunks that have loaded the tileEntity
                            NetworkHandler.syncToTrackingClients(ctx,tileEntity);
                        }

                        ((TileEntityConstructor) tileEntity).setLastRevIndex(message.index);
                    }
                }
            }
        });


        return null;
    }


}
