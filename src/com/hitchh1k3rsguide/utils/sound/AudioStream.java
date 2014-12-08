package com.hitchh1k3rsguide.utils.sound;

import java.nio.ByteBuffer;

import org.lwjgl.openal.AL10;

public class AudioStream
{

    private static final int BUFFER_SIZE = 4096 * 16;
    private final int alFrontBuffer;
    private final int alBackBuffer;
    private final int alSourceID;
    private int streamOffset;
    public volatile boolean looping;
    public volatile String name;
    private IAudioDecoder decoder;

    public AudioStream(String name, int sourceID, boolean looping) throws Exception
    {
        String filename = "/sounds/" + name;
        this.looping = looping;
        this.name = name;
        decoder = null;
        String ext = filename.substring(filename.lastIndexOf('.') + 1);
        if ("wav".equals(ext))
        {
            decoder = new WaveDecoder(AudioSound.class.getResourceAsStream(filename));
        }

        alSourceID = sourceID;
        alFrontBuffer = AL10.alGenBuffers();
        alBackBuffer = AL10.alGenBuffers();
        streamOffset = 0;

        fillBuffer(alFrontBuffer);
        fillBuffer(alBackBuffer);
    }

    public void play()
    {
        boolean looping = false;
        float gain = 1.0f;
        float pitch = 1.0f;
        float[] pos = new float[] { 0.0f, 0.0f, 0.0f };
        float[] vel = new float[] { 0.0f, 0.0f, 0.0f };

        AL10.alSourceStop(alSourceID);
        // AL10.alSourcei(alSourceID, AL10.AL_BUFFER, alFrontBuffer);
        AL10.alSourceQueueBuffers(alSourceID, alFrontBuffer);
        AL10.alSourceQueueBuffers(alSourceID, alBackBuffer);
        AL10.alSourcef(alSourceID, AL10.AL_PITCH, pitch);
        AL10.alSourcef(alSourceID, AL10.AL_GAIN, gain);
        AL10.alSource3f(alSourceID, AL10.AL_POSITION, pos[0], pos[1], pos[2]);
        AL10.alSource3f(alSourceID, AL10.AL_VELOCITY, vel[0], vel[1], vel[2]);
        AL10.alSourcei(alSourceID, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
        AL10.alSourcePlay(alSourceID);
    }

    public void stop()
    {
        AL10.alSourceStop(alSourceID);
    }

    public void dispose()
    {
        for (int i = 0; i < 2; ++i)
        {
            AL10.alSourceUnqueueBuffers(alSourceID);
        }
        AL10.alSourceStop(alSourceID);
        AL10.alDeleteBuffers(alFrontBuffer);
        AL10.alDeleteBuffers(alBackBuffer);
        decoder = null;
    }

    public boolean update()
    {
        boolean active = true;

        int processed = AL10.alGetSourcei(alSourceID, AL10.AL_BUFFERS_PROCESSED);

        while (processed > 0)
        {
            int buffer = AL10.alSourceUnqueueBuffers(alSourceID);

            active = fillBuffer(buffer);

            AL10.alSourceQueueBuffers(alSourceID, buffer);

            processed--;
        }

        return active;
    }

    public boolean fillBuffer(int buffer)
    {
        if (decoder == null)
        {
            return false;
        }

        ByteBuffer data = decoder.read(BUFFER_SIZE);
        if (data.limit() <= 0)
        {
            return false;
        }

        streamOffset += data.limit();
        AL10.alBufferData(buffer, decoder.getFormat(), data, (int) decoder.getSampleRate());
        return true;
    }

}
