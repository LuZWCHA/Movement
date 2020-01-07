package com.nowandfuture.ffmpeg;

public abstract class FrameConverter<F> {
    protected Frame frame;

    public abstract Frame convert(F f);
    public abstract F convert(Frame frame);
}
