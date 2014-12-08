package com.hitchh1k3rsguide.utils.math;

import java.nio.ByteBuffer;

public class Serialization
{

    public static long deserializeLong(byte[] bytes)
    {
        ByteBuffer longBuffer = ByteBuffer.allocate(Long.SIZE / 8);
        longBuffer.put(bytes, 0, Long.SIZE / 8);
        longBuffer.flip();
        return longBuffer.getLong();
    }

    public static byte[] serializeLong(long value)
    {
        ByteBuffer longBuffer = ByteBuffer.allocate(Long.SIZE / 8);
        longBuffer.putLong(0, value);
        return longBuffer.array();
    }

    public static int deserializeInt(byte[] bytes)
    {
        ByteBuffer intBuffer = ByteBuffer.allocate(4);
        intBuffer.put(bytes, 0, 4);
        intBuffer.flip();
        return intBuffer.getInt();
    }

    public static byte[] serializeInt(int value)
    {
        ByteBuffer intBuffer = ByteBuffer.allocate(4);
        intBuffer.putInt(0, value);
        return intBuffer.array();
    }

    public static float deserializeFloat(byte[] bytes)
    {
        ByteBuffer floatBuffer = ByteBuffer.allocate(4);
        floatBuffer.put(bytes, 0, 4);
        floatBuffer.flip();
        return floatBuffer.getFloat();
    }

    public static byte[] serializeFloat(float value)
    {
        ByteBuffer floatBuffer = ByteBuffer.allocate(4);
        floatBuffer.putFloat(0, value);
        return floatBuffer.array();
    }

}
