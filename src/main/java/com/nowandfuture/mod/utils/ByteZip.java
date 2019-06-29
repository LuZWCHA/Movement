package com.nowandfuture.mod.utils;

import com.nowandfuture.mod.Movement;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ByteZip {

    private int index;
    private int outSize;
    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private ByteArrayOutputStream orgByteArray = new ByteArrayOutputStream();

    public ByteZip()
    {
        orgByteArray.reset();
        index = 0;
    }

    public void clear()
    {
        bos.reset();
        index = 0;
    }

    public void setByte(byte data)
    {
        orgByteArray.write(data);
    }

    public void setByteArray(byte[] ba)
    {
        orgByteArray.write(ba,0,ba.length);
    }

    public void setShort(short value)
    {
        int arraySize = Short.SIZE / Byte.SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(arraySize);
        setByteArray(buffer.putShort(value).array() );
    }

    public void setInt(int value)
    {
        int arraySize = Integer.SIZE / Byte.SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(arraySize);
        setByteArray( buffer.putInt(value).array() );
    }

    public void setFloat(float value)
    {
        int arraySize = Float.SIZE / Byte.SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(arraySize);
        setByteArray( buffer.putFloat(value).array() );
    }

    public void compress()
    {
        Deflater compresser = new Deflater();
        compresser.setInput(orgByteArray.toByteArray());
        compresser.finish();
        byte[] outBuf = new byte[orgByteArray.size()];
        outSize = compresser.deflate(outBuf);
        bos.write(outBuf, 0, outSize);
        compresser.end();
    }

    public static int decompress(byte[] out, byte[] in) {
        int outnum = -1;
        final Inflater decompresser = new Inflater();
        try
        {
            decompresser.setInput(in);
            outnum = decompresser.inflate(out);
        }
        catch(DataFormatException e)
        {
            Movement.logger.warn("failed to decompress data. : "+e.toString());
        }
        finally
        {
            decompresser.end();
        }
        return outnum;
    }

    public byte[] getOutput()
    {
        return bos.toByteArray();
    }
    public int getOutputLength()
    {
        return outSize;
    }
    public int getOrgSize()
    {
        return orgByteArray.size();
    }
}
