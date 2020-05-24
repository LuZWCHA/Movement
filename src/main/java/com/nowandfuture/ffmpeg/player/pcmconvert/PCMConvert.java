package com.nowandfuture.ffmpeg.player.pcmconvert;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class PCMConvert {

    private FrequencyScanner fftScanner;
    short[] cacheData;

    private int fftThruput;

    public PCMConvert(int fftThruput) {
        this.fftThruput = fftThruput;
        fftScanner = new FrequencyScanner();
        cacheData = new short[fftThruput];
    }

    public byte[] readyDataByte(byte[] data,int sampleRate) {
        return readyDataByte(data,this.fftThruput,sampleRate);
    }

    public byte[] readyDataByte(byte[] data,int fftThruput,int sampleRate) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        short[] shorts = new short[data.length>>1];
        shortBuffer.get(shorts);
        return fft(shorts,fftThruput,sampleRate);
    }

    private byte[] fft(short[] sampleData, int fftThruput,int sampleRate) {
        if(cacheData.length != fftThruput){
            cacheData = new short[fftThruput];
        }
        byte[] result = new byte[sampleData.length / fftThruput];
        for (int i = 0; i < sampleData.length; i = i + fftThruput) {
            int end = i + fftThruput;
            if (end > sampleData.length) {
                break;
            }
            for (int j = i; j < end; j++) {
                cacheData[j % fftThruput] = sampleData[j];
            }
            double extractFrequency = fftScanner.getMaxFrequency(cacheData, sampleRate);
            result[i / fftThruput] = (byte) (extractFrequency > 127 ? 127 : extractFrequency);
        }

        return result;
    }
}
