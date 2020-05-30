package com.nowandfuture.ffmpeg.player.pcmconvert;

import net.minecraft.util.math.MathHelper;
import org.jtransforms.fft.DoubleFFT_1D;

public class FrequencyScanner {
    private double[] hammingWindow;
    private double[] blackmanWindow;

    public FrequencyScanner() {
        hammingWindow = null;
    }

    public double getMaxFrequency(short[] sampleData, int sampleRate) {
        if (sampleData == null || sampleData.length == 0) {
            return 0;
        }
        /* sampleData + zero padding */
        int len = sampleData.length + 24 * sampleData.length;
        DoubleFFT_1D fft = new DoubleFFT_1D(len);
        double[] a = new double[len << 1];

        System.arraycopy(applyWindowHamming(sampleData), 0, a, 0, sampleData.length);
        fft.realForward(a);

        /* find the peak magnitude and it's index */
        double maxMag = Double.NEGATIVE_INFINITY;
        double maxInd = -1;
        for (int i = 0; i < a.length >> 1; ++i) {
            double re = a[i << 1];
            double im = a[(i << 1) + 1];
            double mag = Math.sqrt(re * re + im * im);

            if (mag > maxMag) {
                maxMag = mag;
                maxInd = i;
            }
        }

        return (maxInd / (double)(a.length >> 1));
    }

    /**
     * build a Hamming window filter for samples of a given size
     * See http://www.labbookpages.co.uk/audio/firWindowing.html#windows
     *
     * @param size the sample size for which the filter will be created
     */
    private void buildHammWindow(int size) {
        if (hammingWindow != null && hammingWindow.length == size) {
            return;
        }
        hammingWindow = new double[size];
        for (int i = 0; i < size; ++i) {
            hammingWindow[i] = .54 - .46 *MathHelper.cos((float) (2 * Math.PI * i / (size - 1.0)));
        }
    }

    private void buildWindowBlackman(int size) {
        if (blackmanWindow != null && blackmanWindow.length == size) {
            return;
        }
        blackmanWindow = new double[size];
        for (int i = 0; i < size; ++i) {
            blackmanWindow[i] = .42 - .5 * MathHelper.cos((float) (2 * Math.PI * i / (size - 1.0))) + .08 * MathHelper.cos((float) (4 * Math.PI * i / (size - 1.0)));
        }
    }

    /**
     * apply a Hamming window filter to raw input data
     *
     * @param input an array containing unfiltered input data
     * @return a double array containing the filtered data
     */
    public double[] applyWindowHamming(short[] input) {
        double[] res = new double[input.length];

        buildHammWindow(input.length);
        for (int i = 0; i < input.length; ++i) {
            res[i] = (double) input[i] * hammingWindow[i];
        }
        return res;
    }

    /**
     * apply a Hamming window filter to raw input data
     *
     * @param input an array containing unfiltered input data
     * @return a double array containing the filtered data
     */
    public double[] applyWindowHamming(byte[] input) {
        double[] res = new double[input.length];

        buildHammWindow(input.length);
        for (int i = 0; i < input.length; ++i) {
            res[i] = (double) input[i] * hammingWindow[i];
        }
        return res;
    }

    /**
     * apply a Hamming window filter to raw input data
     *
     * @param input an array containing unfiltered input data
     * @return a double array containing the filtered data
     */
    public double[] applyWindowHamming(double[] input) {
        double[] res = new double[input.length];

        buildHammWindow(input.length);
        for (int i = 0; i < input.length; ++i) {
            res[i] = input[i] * hammingWindow[i];
        }
        return res;
    }

    public double[] applyWindowBlackman(double[] input) {
        double[] res = new double[input.length];

        buildWindowBlackman(input.length);
        for (int i = 0; i < input.length; ++i) {
            res[i] = input[i] * blackmanWindow[i];
        }
        return res;
    }

    public double[] toDouble(short[] input){
        double[] res = new double[input.length];
        for (int i = 0; i < input.length; ++i) {
            res[i] = input[i];
        }
        return res;
    }

    public double[] toDouble(byte[] input){
        double[] res = new double[input.length];
        for (int i = 0; i < input.length; ++i) {
            res[i] = input[i];
        }
        return res;
    }
}
