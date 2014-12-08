package com.hitchh1k3rsguide.utils.sound;

import java.nio.ByteBuffer;

public interface IAudioDecoder
{

    public ByteBuffer read(int bytesToRead);

    public int getFormat();

    public float getSampleRate();

}
