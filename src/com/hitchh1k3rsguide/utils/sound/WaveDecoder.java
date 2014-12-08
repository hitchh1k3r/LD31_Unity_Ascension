package com.hitchh1k3rsguide.utils.sound;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

public class WaveDecoder implements IAudioDecoder
{

    private static final int BLOCK_SIZE = 4096;

    private int format;
    private int bitRate;
    private float sampleRate;
    private AudioInputStream audioInputStream;
    private boolean endOfStream = false;

    public WaveDecoder(InputStream is) throws Exception
    {
        BufferedInputStream bis = new BufferedInputStream(is);
        audioInputStream = AudioSystem.getAudioInputStream(bis);
        AudioFormat audioformat = audioInputStream.getFormat();

        bitRate = audioformat.getSampleSizeInBits();
        sampleRate = audioformat.getSampleRate();

        if (audioformat.getChannels() == 1)
        {
            if (bitRate == 8)
            {
                format = AL10.AL_FORMAT_MONO8;
            }
            else if (bitRate == 16)
            {
                format = AL10.AL_FORMAT_MONO16;
            }
            else
            {
                assert false : "Illegal sample size";
            }
        }
        else if (audioformat.getChannels() == 2)
        {
            if (bitRate == 8)
            {
                format = AL10.AL_FORMAT_STEREO8;
            }
            else if (bitRate == 16)
            {
                format = AL10.AL_FORMAT_STEREO16;
            }
            else
            {
                throw new RuntimeException("Illegal sample size: " + bitRate);
            }
        }
        else
        {
            throw new IllegalStateException("Only mono or stereo is supported");
        }
    }

    @Override
    public ByteBuffer read(int bytesToRead)
    {
        if (endOfStream)
            return BufferUtils.createByteBuffer(0);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream(bytesToRead > 0 ? bytesToRead : 1);
        byte[] block = new byte[BLOCK_SIZE];
        int bytesRead = 0, cnt = 0;
        while (!endOfStream && (bytesRead < bytesToRead || bytesToRead == -1))
        {
            cnt = readBlock(
                    block,
                    (bytesToRead > 0 && bytesRead > (bytesToRead - BLOCK_SIZE)) ? (bytesToRead - bytesRead)
                            : BLOCK_SIZE);
            bytesRead += cnt;
            bytes.write(block, 0, cnt);
        }

        return convertAudioBytes(bytes.toByteArray(), bitRate == 16);
    }

    private int readBlock(byte[] bytes, int maxLength)
    {
        int bytesRead = 0, cnt = 0;
        try
        {
            while (bytesRead < maxLength)
            {
                if ((cnt = audioInputStream.read(bytes, bytesRead, maxLength - bytesRead)) <= 0)
                {
                    endOfStream = true;
                    break;
                }
                bytesRead += cnt;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            endOfStream = true;
        }
        return bytesRead;
    }

    @Override
    public int getFormat()
    {
        return format;
    }

    @Override
    public float getSampleRate()
    {
        return sampleRate;
    }

    private static ByteBuffer convertAudioBytes(byte[] audio_bytes, boolean two_bytes_data)
    {
        ByteBuffer dest = ByteBuffer.allocateDirect(audio_bytes.length);
        dest.order(ByteOrder.nativeOrder());
        ByteBuffer src = ByteBuffer.wrap(audio_bytes);
        src.order(ByteOrder.LITTLE_ENDIAN);
        if (two_bytes_data)
        {
            ShortBuffer dest_short = dest.asShortBuffer();
            ShortBuffer src_short = src.asShortBuffer();
            while (src_short.hasRemaining())
                dest_short.put(src_short.get());
        }
        else
        {
            while (src.hasRemaining())
                dest.put(src.get());
        }
        dest.rewind();
        return dest;
    }

}
