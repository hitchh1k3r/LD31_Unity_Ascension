package com.hitchh1k3rsguide.utils.sound;

import java.nio.IntBuffer;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALContext;
import org.lwjgl.openal.ALDevice;

public class SoundEngine
{

    private static final int NUMBER_OF_SOUND_CHANNELS = 16;
    private static final int NUMBER_OF_STREAM_CHANNELS = 2;

    SoundThread soundThread;

    private ALContext context;
    private ALDevice device;
    private boolean initialized;
    private int[] soundSources = new int[NUMBER_OF_SOUND_CHANNELS];
    private int[] streamSources = new int[NUMBER_OF_SOUND_CHANNELS];
    private volatile boolean[] soundsInUse = new boolean[NUMBER_OF_SOUND_CHANNELS];
    private volatile boolean[] streamsInUse = new boolean[NUMBER_OF_STREAM_CHANNELS];
    private HashMap<String, AudioSound> soundFX = new HashMap<String, AudioSound>();
    private volatile AudioStream[] streams = new AudioStream[NUMBER_OF_STREAM_CHANNELS];
    private int musicHandle = -1;

    public void initialize()
    {
        device = ALDevice.create();
        ALCCapabilities caps = device.getCapabilities();

        if (!caps.OpenALC10)
        {
            System.err.println("Cannot get an OpenAL 1.0 compatible audio context.");
            System.exit(-1);
        }

        String defaultDeviceSpecifier = ALC10.alcGetString(0L, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
        if (defaultDeviceSpecifier == null)
        {
            System.err.println("Cannot open audio device.");
            System.exit(-1);
        }

        IntBuffer attribs = BufferUtils.createIntBuffer(16);

        attribs.put(ALC10.ALC_FREQUENCY);
        attribs.put(44100);

        attribs.put(ALC10.ALC_REFRESH);
        attribs.put(60);

        attribs.put(ALC10.ALC_SYNC);
        attribs.put(ALC10.ALC_FALSE);

        attribs.put(0);
        attribs.flip();

        long contextHandle = ALC10.alcCreateContext(device.getPointer(), attribs);
        if (contextHandle == 0L)
        {
            System.err.println("Cannot open get OpenAL audio context.");
            System.exit(-1);
        }

        context = new ALContext(device, contextHandle);

        soundThread = new SoundThread();
        initialized = true;

        for (int i = 0; i < NUMBER_OF_SOUND_CHANNELS; ++i)
        {
            soundSources[i] = AL10.alGenSources();
        }
        for (int i = 0; i < NUMBER_OF_STREAM_CHANNELS; ++i)
        {
            streamSources[i] = AL10.alGenSources();
        }
    }

    private class SoundThread implements Runnable
    {

        private volatile boolean running = false;
        private Thread thread;

        public SoundThread()
        {
            thread = new Thread(this);
            thread.start();
        }

        public void close()
        {
            running = false;
            thread.interrupt();
            try
            {
                thread.join();
            }
            catch (InterruptedException e)
            {
            }
        }

        @Override
        public void run()
        {
            running = true;
            while (running)
            {
                for (int i = 0; i < SoundEngine.NUMBER_OF_SOUND_CHANNELS; ++i)
                {
                    if (soundsInUse[i])
                    {
                        if (AL10.alGetSourcei(soundSources[i], AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING)
                        {
                            soundsInUse[i] = false;
                        }
                    }
                }
                for (int i = 0; i < SoundEngine.NUMBER_OF_STREAM_CHANNELS; ++i)
                {
                    if (streams[i] != null)
                    {
                        if (!streams[i].update())
                        {
                            String loop = "";
                            if (streams[i].looping)
                            {
                                loop = streams[i].name;
                            }
                            stopStream(i);
                            if (!("".equals(loop)))
                            {
                                playStream(loop, true);
                            }
                        }
                    }
                }
                try
                {
                    Thread.sleep(50);
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }

    public void close()
    {
        initialized = false;
        soundThread.close();

        for (int i = 0; i < NUMBER_OF_SOUND_CHANNELS; ++i)
        {
            AL10.alDeleteSources(soundSources[i]);
        }
        for (int i = 0; i < NUMBER_OF_STREAM_CHANNELS; ++i)
        {
            AL10.alDeleteSources(streamSources[i]);
        }
        for (AudioSound sound : soundFX.values())
        {
            sound.dispose();
        }

        context.destroy();
        device.destroy();
    }

    private int aquireSoundSource()
    {
        for (int i = 0; i < NUMBER_OF_SOUND_CHANNELS; ++i)
        {
            if (!soundsInUse[i])
            {
                soundsInUse[i] = true;
                return soundSources[i];
            }
        }
        return -1;
    }

    private int aquireStreamIndex()
    {
        for (int i = 0; i < NUMBER_OF_STREAM_CHANNELS; ++i)
        {
            if (!streamsInUse[i])
            {
                streamsInUse[i] = true;
                return i;
            }
        }
        return -1;
    }

    private void freeStream(int streamIndex)
    {
        streamsInUse[streamIndex] = false;
        streams[streamIndex] = null;
    }

    public void playSound(String name)
    {
        if (initialized)
        {
            try
            {
                AudioSound sound = getOrMakeSound(name);
                int source = aquireSoundSource();
                if (source >= 0)
                {
                    sound.play(source);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.err.println("Tried to play a sound effect without an audio context.");
        }
    }

    public void preloadSounds(String[] sounds)
    {
        for (String sound : sounds)
        {
            getOrMakeSound(sound);
        }
    }

    private AudioSound getOrMakeSound(String name)
    {
        AudioSound sound = soundFX.get(name);
        if (sound == null)
        {
            try
            {
                sound = new AudioSound("/sounds/" + name);
                soundFX.put(name, sound);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return sound;
    }

    public int playStream(String name, boolean looping)
    {
        int streamID = -1;
        try
        {
            streamID = aquireStreamIndex();
            if (streamID >= 0)
            {
                AudioStream stream = new AudioStream(name, streamSources[streamID], looping);
                if (stream != null)
                {
                    streams[streamID] = stream;
                    stream.play();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return streamID;
    }

    public synchronized void stopStream(int streamID)
    {
        if (streamID >= 0 && streamID < NUMBER_OF_STREAM_CHANNELS)
        {
            AudioStream stream = streams[streamID];
            if (stream != null)
            {
                stream.stop();
                stream.dispose();
                freeStream(streamID);
            }
        }
    }

    public void playMusic(String song)
    {
        if (musicHandle >= 0)
        {
            stopMusic();
        }
        musicHandle = playStream(song, true);
    }

    public void stopMusic()
    {
        stopStream(musicHandle);
        musicHandle = -1;
    }

}
