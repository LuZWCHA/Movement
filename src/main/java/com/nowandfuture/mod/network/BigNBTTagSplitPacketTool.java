package com.nowandfuture.mod.network;

import com.nowandfuture.mod.network.message.DivBytesMessage;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BigNBTTagSplitPacketTool {

    private byte[][] bytes = null;
    private NBTTagCompound nbt = null;

    public boolean putBytes(int index,int divNum, byte[] divBytes) throws IOException {
        nbt = null;

        if(bytes == null || bytes.length != divNum)
            bytes = new byte[divNum][];

        bytes[index] = divBytes;

        if(index < divNum - 1) return false;

        return combineNBT(divNum);
    }

    private boolean combineNBT(int divNum) throws IOException {

        try (ByteArrayOutputStream allByteArray = new ByteArrayOutputStream();
                ByteArrayInputStream ips = new ByteArrayInputStream(allByteArray.toByteArray())){

            for(int i=0; i<divNum; ++i)
            {
                if(bytes[i] != null)
                    allByteArray.write(bytes[i], 0, bytes[i].length);
            }

            nbt = CompressedStreamTools.readCompressed(ips);

        } catch (IOException e) {
            e.printStackTrace();
            return false;

        }finally {
            if(nbt == null)
            {
                bytes = null;
            }
        }
        return true;
    }

    public NBTTagCompound getNBT() {
        return nbt;
    }

    public void reset(){
        bytes = null;
        nbt = null;
    }

    public static byte[] NBTToByteArray(NBTTagCompound nbt){
        byte[] temp = null;

        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            CompressedStreamTools.writeCompressed(nbt,outputStream);
            temp = outputStream.toByteArray();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    //divByteSize should below 2^15
    public static void forEachByteArray(NBTTagCompound compound,final int divByteSize,@Nonnull DivBytesMessage.DivBytesGetter getter)throws Exception{

        byte[] bytes = NBTToByteArray(compound);
        if(bytes != null){
            final int size = bytes.length;
            int divNum = size / divByteSize + 1;
            ByteArrayInputStream ips = new ByteArrayInputStream(bytes);

            {
                byte[] divBytes;
                for (short i = 0; i < divNum; ++i) {
                    divBytes = new byte[divByteSize];
                    if(ips.read(divBytes, 0, divByteSize) != -1) {
                        getter.get(i,divNum,divBytes);
                    }else {
                        break;
                    }
                }
            }
        }else{
            throw new IOException();
        }
    }


}
