package com.nowandfuture.ffmpeg.player.pcmconvert;

import org.jtransforms.fft.DoubleFFT_1D;

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
        return getAmplitudes(data, sampleRate);
    }


    //spectrogram of amg
    private byte[] getMaxMagnitudes(short[] sampleData, int fftThruput, int sampleRate) {
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

    private byte[] getAmplitudes(byte[] sampleData, int sampleRate) {
        double[] array = fftScanner.toDouble(sampleData);

        int N = 1024 * 4;
        int count = 16;

        double[] zeroPadding = new double[N];

        System.arraycopy(array,0,zeroPadding,0,Math.min(array.length,N));
        zeroPadding = fftScanner.applyWindowBlackman(zeroPadding);
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
        double Y0 = 0;
        double logY0 = 0;

        fftThruput = fd.length / count / 2;
        byte[] result = new byte[count];

        int pos = 0;
        for(int i = 0; i < fd.length / 2; i += fftThruput){

            if(i + fftThruput > fd.length / 2) break;

            double maxValue = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < fftThruput; j++) {
                if (fd[i + j] > maxValue) {
                    maxValue = fd[i + j];
                }
            }

            long fre = (long) (N / count * (pos + .5f));
            double a = AWeightedFilter(fre);
//            System.out.println(fre + ", " + a + ", " + maxValue);
            result[pos++] = (byte) ((maxValue > Y0 ? (Math.log10(maxValue) * 20 - logY0) : 0) / maxDB * a * 128);
        }

        return result;
    }

    //not real a weighted filter ,just use it to simulate human auditory system
    //range 0-1
    private double AWeightedFilter(long frequent){
        long f2 = frequent * frequent;
        return 148693636/
                ((1 + 424.36 / f2) * Math.sqrt((1 + 11599.29 / f2) * (1 + 544496.41 / f2)) * (f2 + 148693636));
    }
}
