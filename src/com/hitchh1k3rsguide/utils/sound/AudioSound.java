package com.hitchh1k3rsguide.utils.sound;

import java.nio.ByteBuffer;

import org.lwjgl.openal.AL10;

public class AudioSound
{

    private final int alBufferID;

    public AudioSound(String filename) throws Exception
    {
        IAudioDecoder decoder = null;
        String ext = filename.substring(filename.lastIndexOf('.') + 1);
        if ("wav".equals(ext))
        {
            decoder = new WaveDecoder(AudioSound.class.getResourceAsStream(filename));
        }

        alBufferID = AL10.alGenBuffers();
        ByteBuffer data = decoder.read(-1);
        int format = decoder.getFormat();
        int rate = (int) decoder.getSampleRate();

        AL10.alBufferData(alBufferID, format, data, rate);
    }

    public void play(int alSourceID)
    {
        boolean looping = false;
        float gain = 1.0f;
        float pitch = 1.0f;
        float[] pos = new float[] { 0.0f, 0.0f, 0.0f };
        float[] vel = new float[] { 0.0f, 0.0f, 0.0f };

        AL10.alSourceStop(alSourceID);
        AL10.alSourcei(alSourceID, AL10.AL_BUFFER, alBufferID);
        AL10.alSourcef(alSourceID, AL10.AL_PITCH, pitch);
        AL10.alSourcef(alSourceID, AL10.AL_GAIN, gain);
        AL10.alSource3f(alSourceID, AL10.AL_POSITION, pos[0], pos[1], pos[2]);
        AL10.alSource3f(alSourceID, AL10.AL_VELOCITY, vel[0], vel[1], vel[2]);
        AL10.alSourcei(alSourceID, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
        AL10.alSourcePlay(alSourceID);
    }

    public void dispose()
    {
        AL10.alDeleteBuffers(alBufferID);
    }

}
