package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.Frame;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.Pointer;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import javax.annotation.Nonnull;
import javax.sound.sampled.*;
import javax.swing.*;
import java.nio.*;

public class Utils {

    public static void cloneFrameDeallocate(@Nonnull Frame frame){
        if(frame.image != null){
            for (Buffer b :
                    frame.image) {
                if(b.isDirect()){
                    b.clear();
                    Cleaner c = ((DirectBuffer)b).cleaner();
                    if(c != null) c.clean();
                }
            }
        }

        if(frame.samples != null){
            for (Buffer b :
                    frame.samples) {
                if(b.isDirect()){
                    b.clear();
                    Cleaner c = ((DirectBuffer)b).cleaner();
                    if(c != null) c.clean();
                }
            }
        }

        if(frame.opaque != null) {
            Pointer[] pointers = (Pointer[]) frame.opaque;
            if(pointers[0] != null)
                pointers[0].deallocate();
            if(pointers[1] != null)
                pointers[1].deallocate();
        }
    }

    public static ByteBuffer shortToByteValue(ShortBuffer arr, float vol) {
        int len  = arr.capacity();
        ByteBuffer bb = ByteBuffer.allocate(len * 2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for(int i = 0;i<len;i++){
            bb.putShort(i*2,(short)((float)arr.get(i)*vol));
        }
        return bb;
    }
    public static ByteBuffer floatToByteValue(FloatBuffer arr, float vol){
        int len = arr.capacity();
        float f;
        float v;
        ByteBuffer res = ByteBuffer.allocate(len*2);
        res.order(ByteOrder.LITTLE_ENDIAN);

        v = 32768.0f * vol;
        for(int i=0;i<len;i++){
            f = arr.get(i)*v;//Ref：https://stackoverflow.com/questions/15087668/how-to-convert-pcm-samples-in-byte-array-as-floating-point-numbers-in-the-range
            if(f>v) f = v;
            if(f<-v) f = v;
            //默认转为大端序
            res.putShort(i*2,(short)f);//注意乘以2，因为一次写入两个字节。
        }
        return res;
    }


    public static AudioFormat getAudioFormat(int sampleFormat,float sampleRate,int audioChannels,float frameRate){
        AudioFormat af = null;
        switch(sampleFormat){
            case avutil.AV_SAMPLE_FMT_U8://无符号short 8bit
                break;
            case avutil.AV_SAMPLE_FMT_S16://有符号short 16bit
            case avutil.AV_SAMPLE_FMT_FLT:
            case avutil.AV_SAMPLE_FMT_S16P://有符号short 16bit,平面型
            case avutil.AV_SAMPLE_FMT_FLTP://float 平面型 需转为16bit short
                af = new AudioFormat(sampleRate,16,audioChannels,true,false);
                break;
            case avutil.AV_SAMPLE_FMT_S32:
            case avutil.AV_SAMPLE_FMT_DBL:
            case avutil.AV_SAMPLE_FMT_U8P:
                break;
            case avutil.AV_SAMPLE_FMT_S32P://有符号short 32bit，平面型，但是32bit的话可能电脑声卡不支持，这种音乐也少见
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,sampleRate,32,audioChannels,audioChannels*2, frameRate,false);
                break;
            case avutil.AV_SAMPLE_FMT_DBLP:
            case avutil.AV_SAMPLE_FMT_S64://有符号short 64bit 非平面型
                break;
            case avutil.AV_SAMPLE_FMT_S64P://有符号short 64bit平面型
                break;
            default:
                System.out.println("no support format of audio");
        }
        return af;
    }

    public static int getOpenALFormat(AudioFormat format){
        int openALFormat = -1;
        switch(format.getChannels()) {
            case 1:
                switch(format.getSampleSizeInBits()) {
                    case 8:
                        openALFormat = AL10.AL_FORMAT_MONO8;
                        break;
                    case 16:
                        openALFormat = AL10.AL_FORMAT_MONO16;
                        break;
                }
                break;
            case 2:
                switch(format.getSampleSizeInBits()) {
                    case 8:
                        openALFormat = AL10.AL_FORMAT_STEREO8;
                        break;
                    case 16:
                        openALFormat = AL10.AL_FORMAT_STEREO16;
                        break;
                }
                break;
        }
        return openALFormat;
    }

    public static Line init(int sampleFormat,int sampleRate,float frameRate,int audioChannels,boolean isMixer) {
        AudioFormat af = null;
        switch(sampleFormat){
            case avutil.AV_SAMPLE_FMT_U8://无符号short 8bit
                break;
            case avutil.AV_SAMPLE_FMT_S16://有符号short 16bit
            case avutil.AV_SAMPLE_FMT_FLT:
            case avutil.AV_SAMPLE_FMT_S16P://有符号short 16bit,平面型
            case avutil.AV_SAMPLE_FMT_FLTP://float 平面型 需转为16bit short
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,sampleRate,16,audioChannels,audioChannels * 2, frameRate,false);
                break;
            case avutil.AV_SAMPLE_FMT_S32:
            case avutil.AV_SAMPLE_FMT_DBL:
            case avutil.AV_SAMPLE_FMT_U8P:
                break;
            case avutil.AV_SAMPLE_FMT_S32P://有符号short 32bit，平面型，但是32bit的话可能电脑声卡不支持，这种音乐也少见
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,sampleRate,32,audioChannels,audioChannels * 4, frameRate,false);
                break;
            case avutil.AV_SAMPLE_FMT_DBLP:
            case avutil.AV_SAMPLE_FMT_S64://有符号short 64bit 非平面型
                break;
            case avutil.AV_SAMPLE_FMT_S64P://有符号short 64bit平面型
                break;
            default:
                System.out.println("no support format of audio");
        }

        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, af);
        try {
            Mixer.Info[] mixerInfo =AudioSystem.getMixerInfo();
            Mixer mixer = AudioSystem.getMixer(mixerInfo[0]);
            if(isMixer)
                return mixer;
            return AudioSystem.getLine(dataLineInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static byte[] getMonoAudio(final Buffer[] samples,float vol,int sampleFormat){

        Buffer[] buf;
        FloatBuffer leftData,rightData;
        ShortBuffer ILData,IRData;
        ByteBuffer TLData,TRData;
        byte[] tl = null;
        byte[] tr = null;
        int k;
        buf = samples;
        switch(sampleFormat){
            case avutil.AV_SAMPLE_FMT_FLTP://平面型左右声道分开。
                leftData = (FloatBuffer)buf[0];
                TLData = floatToByteValue(leftData,vol);
                rightData = (FloatBuffer)buf[1];
                TRData = floatToByteValue(rightData,vol);
                tl = TLData.array();
                tr = TRData.array();
                break;
            case avutil.AV_SAMPLE_FMT_S16://非平面型左右声道在一个buffer中。
                ILData = (ShortBuffer)buf[0];
                TLData = shortToByteValue(ILData,vol);
                return TLData.array();
            case avutil.AV_SAMPLE_FMT_FLT://float非平面型
                leftData = (FloatBuffer)buf[0];
                TLData = floatToByteValue(leftData,vol);
                tl = TLData.array();
                tr = tl.clone();
                break;
            case avutil.AV_SAMPLE_FMT_S16P://平面型左右声道分开
                ILData = (ShortBuffer)buf[0];
                IRData = (ShortBuffer)buf[1];
                TLData = shortToByteValue(ILData,vol);
                TRData = shortToByteValue(IRData,vol);
                tl = TLData.array();
                tr = TRData.array();
            default:

        }
        return null;
    }

    public static byte[] getAudio(final Buffer[] samples,float vol,int sampleFormat){

        Buffer[] buf;
        FloatBuffer leftData,rightData;
        ShortBuffer ILData,IRData;
        ByteBuffer TLData,TRData;
        byte[] tl,tr;
        byte[] combine;
        int k;
        buf = samples;
        switch(sampleFormat){
            case avutil.AV_SAMPLE_FMT_FLTP://平面型左右声道分开。
                leftData = (FloatBuffer)buf[0];
                TLData = floatToByteValue(leftData,vol);
                rightData = (FloatBuffer)buf[1];
                TRData = floatToByteValue(rightData,vol);
                tl = TLData.array();
                tr = TRData.array();
                combine = new byte[tl.length + tr.length];
                k = 0;
                for(int i=0;i<tl.length;i=i+2) {//混合两个声道。
                    for (int j = 0; j < 2; j++) {
                        combine[j+4*k] = tl[i + j];
                        combine[j + 2+4*k] = tr[i + j];
                    }
                    k++;
                }
                return combine;
            case avutil.AV_SAMPLE_FMT_S16://非平面型左右声道在一个buffer中。
                ILData = (ShortBuffer)buf[0];
                TLData = shortToByteValue(ILData,vol);
                tl = TLData.array();
                return tl;
            case avutil.AV_SAMPLE_FMT_FLT://float非平面型
                leftData = (FloatBuffer)buf[0];
                TLData = floatToByteValue(leftData,vol);
                tl = TLData.array();
                return tl;
            case avutil.AV_SAMPLE_FMT_S16P://平面型左右声道分开
                ILData = (ShortBuffer)buf[0];
                IRData = (ShortBuffer)buf[1];
                TLData = shortToByteValue(ILData,vol);
                TRData = shortToByteValue(IRData,vol);
                tl = TLData.array();
                tr = TRData.array();
                combine = new byte[tl.length+tr.length];
                k = 0;
                for(int i=0;i<tl.length;i=i+2) {
                    for (int j = 0; j < 2; j++) {
                        combine[j+4*k] = tl[i + j];
                        combine[j + 2+4*k] = tr[i + j];
                    }
                    k++;
                }
                return combine;
            default:
                return new byte[]{};
        }
    }

    public static void playAudio(final Buffer[] samples, SourceDataLine sourceDataLine,float vol,int sampleFormat) {
        Buffer[] buf;
        FloatBuffer leftData,rightData;
        ShortBuffer ILData,IRData;
        ByteBuffer TLData,TRData;
        byte[] tl,tr;
        byte[] combine;
        int k;
        buf = samples;
        switch(sampleFormat){
            case avutil.AV_SAMPLE_FMT_FLTP://平面型左右声道分开。
                leftData = (FloatBuffer)buf[0];
                TLData = floatToByteValue(leftData,vol);
                rightData = (FloatBuffer)buf[1];
                TRData = floatToByteValue(rightData,vol);
                tl = TLData.array();
                tr = TRData.array();
                combine = new byte[tl.length+tr.length];
                k = 0;
                for(int i=0;i<tl.length;i=i+2) {//混合两个声道。
                    for (int j = 0; j < 2; j++) {
                        combine[j+4*k] = tl[i + j];
                        combine[j + 2+4*k] = tr[i + j];
                    }
                    k++;
                }
                sourceDataLine.write(combine,0,combine.length);
                break;
            case avutil.AV_SAMPLE_FMT_S16://非平面型左右声道在一个buffer中。
                ILData = (ShortBuffer)buf[0];
                TLData = shortToByteValue(ILData,vol);
                tl = TLData.array();

                sourceDataLine.write(tl,0,tl.length);
                break;
            case avutil.AV_SAMPLE_FMT_FLT://float非平面型
                leftData = (FloatBuffer)buf[0];
                TLData = floatToByteValue(leftData,vol);
                tl = TLData.array();

                sourceDataLine.write(tl,0,tl.length);
                break;
            case avutil.AV_SAMPLE_FMT_S16P://平面型左右声道分开
                ILData = (ShortBuffer)buf[0];
                IRData = (ShortBuffer)buf[1];
                TLData = shortToByteValue(ILData,vol);
                TRData = shortToByteValue(IRData,vol);
                tl = TLData.array();
                tr = TRData.array();
                combine = new byte[tl.length+tr.length];
                k = 0;
                for(int i=0;i<tl.length;i=i+2) {
                    for (int j = 0; j < 2; j++) {
                        combine[j+4*k] = tl[i + j];
                        combine[j + 2+4*k] = tr[i + j];
                    }
                    k++;
                }

                sourceDataLine.write(combine,0,combine.length);
                break;
            default:
                JOptionPane.showMessageDialog(null,"no support audio format","no support audio format",JOptionPane.ERROR_MESSAGE);
//                System.exit(0);
                break;
        }

    }
}
