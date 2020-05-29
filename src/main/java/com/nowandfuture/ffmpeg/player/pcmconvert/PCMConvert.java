package com.nowandfuture.ffmpeg.player.pcmconvert;

import org.jtransforms.fft.DoubleFFT_1D;

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
//        return fft(shorts,fftThruput,sampleRate);
        return fft1(data);
    }


    //spectrogram
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

    private byte[] fft1(byte[] sampleData) {

        double[] array = fftScanner.toDouble(sampleData);

        int N = 1024 * 4;
        int count = 16;

        double[] zeroPadding = new double[N];

        System.arraycopy(array,0,zeroPadding,0,Math.min(array.length,N));
        zeroPadding = fftScanner.applyWindow(zeroPadding);
        DoubleFFT_1D fft = new DoubleFFT_1D(N);
        fft.realForward(zeroPadding);

        double[] fd = new double[N/2];

        for (int i = 1; i < fd.length; i++) {
            double re = zeroPadding[i << 1];
            double im = zeroPadding[(i << 1) + 1];
            double mag = Math.sqrt(re * re + im * im);
            fd[i] = 2 * mag / N;//get real amp
        }

        fd[0] = zeroPadding[0] / N;
        fd[fd.length / 2] = zeroPadding[1] / N;

        double maxDB = Math.log10(128) * 20;
        double Y0 = 1 << ((int)((Math.log(N) / Math.log(2)) + 3) << 1);
        double logY0 = Math.log10(Y0) * 20;

        fftThruput = fd.length / count / 2;
        byte[] result = new byte[count];

        int pos = 0;
        for(int i = 0; i < fd.length / 2; i += fftThruput){

            if(i + fftThruput > fd.length / 2) break;

            double maxValue = Double.NEGATIVE_INFINITY;
            double maxInd = -1;
            for (int j = 0; j < fftThruput; j++) {
                if (fd[i + j] > maxValue) {
                    maxValue = fd[i + j];
                    maxInd = i;
                }
            }

            result[pos++] = (byte) ((maxValue > 0 ? (Math.log10(maxValue) * 20 - 0) : 0) / maxDB * 128);
        }

        return result;
    }
}
