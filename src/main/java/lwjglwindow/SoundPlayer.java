package lwjglwindow;

import basewindow.BaseSoundPlayer;
import org.apache.commons.io.IOUtils;
import org.lwjgl.openal.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class SoundPlayer extends BaseSoundPlayer
{
    public long context;
    public long device;

    public HashMap<String, Integer> buffers = new HashMap<String, Integer>();
    public ArrayList<Integer> sources = new ArrayList<Integer>();

    public HashMap<String, Integer> musicBuffers = new HashMap<String, Integer>();
    public ArrayList<Integer> musicSources = new ArrayList<Integer>();

    public int currentMusic = -1;
    public int prevMusic = -1;

    public String musicID = null;

    public long fadeBegin = 0;
    public long fadeEnd = 0;

    public float prevVolume;
    public float currentVolume;

    /**
     * Warning! This will give an exception if there are no audio devices plugged into the computer!
     */
    public SoundPlayer()
    {
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        device = alcOpenDevice(defaultDeviceName);

        int[] attributes = {0};
        context = alcCreateContext(device, attributes);
        alcMakeContextCurrent(context);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
    }

    @Override
    public void loadMusic(String path)
    {
        this.createMusic(path);
    }

    public void playSound(String path)
    {
        playSound(path, 1);
    }

    public void playSound(String path, float pitch)
    {
        playSound(path, pitch, 1);
    }

    public void playSound(String path, float pitch, float volume)
    {
        if (sources.size() >= 240)
            alDeleteSources(sources.remove(0));

        if (this.buffers.get(path) == null)
            createSound(path);

        int bufferPointer = this.buffers.get(path);

        int sourcePointer = alGenSources();

        alSourcei(sourcePointer, AL_BUFFER, bufferPointer);
        alSourcef(sourcePointer, AL_PITCH, pitch);
        alSourcef(sourcePointer, AL_GAIN, volume);

        alSourcePlay(sourcePointer);

        sources.add(sourcePointer);
    }

    @Override
    public void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime)
    {
        if (musicSources.size() >= 15)
            alDeleteSources(musicSources.remove(0));

        if (this.musicBuffers.get(path) == null)
            createMusic(path);

        int bufferPointer = this.musicBuffers.get(path);

        int loop = AL_FALSE;

        if (looped)
            loop = AL_TRUE;

        int sourcePointer = alGenSources();

        alSourceStop(prevMusic);
        alSourceUnqueueBuffers(prevMusic);
        prevMusic = currentMusic;
        currentMusic = sourcePointer;
        currentVolume = volume;

        alSourcei(sourcePointer, AL_BUFFER, bufferPointer);
        alSourcef(sourcePointer, AL_LOOPING, loop);
        alSourcef(sourcePointer, AL_GAIN, volume);

        if (continueID != null && continueID.equals(this.musicID))
        {
            alSourcef(sourcePointer, EXTOffset.AL_SEC_OFFSET, alGetSourcef(prevMusic, EXTOffset.AL_SEC_OFFSET));
            alSourcef(sourcePointer, AL_GAIN, 0);

            fadeBegin = System.currentTimeMillis();
            fadeEnd = System.currentTimeMillis() + fadeTime;
        }

        alSourcePlay(sourcePointer);
        prevVolume = volume;

        this.musicID = continueID;

        musicSources.add(sourcePointer);
    }

    @Override
    public void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime, boolean stoppable)
    {
        this.playMusic(path, volume, looped, continueID, fadeTime);
    }

    @Override
    public void stopMusic()
    {
        alSourceStop(currentMusic);
        alSourceStop(prevMusic);
        alSourceUnqueueBuffers(currentMusic);
        alSourceUnqueueBuffers(prevMusic);
        this.currentMusic = -1;
        this.prevMusic = -1;
        this.musicID = null;
    }

    @Override
    public void registerCombinedMusic(String path, String id)
    {

    }

    protected void createSound(String path)
    {
        ShortBuffer rawAudioBuffer = null;

        int channels = -1;
        int sampleRate = -1;

        try (MemoryStack stack = stackPush())
        {
            //Allocate space to store return information from the function
            IntBuffer channelsBuffer = stack.mallocInt(1);
            IntBuffer sampleRateBuffer = stack.mallocInt(1);

            InputStream in = getClass().getResourceAsStream(path);
            byte[] bytes = IOUtils.toByteArray(in);
            ByteBuffer b = MemoryUtil.memAlloc(bytes.length);
            b.put(bytes);
            b.flip();

            rawAudioBuffer = stb_vorbis_decode_memory(b, channelsBuffer, sampleRateBuffer);
            in.close();

            //Retreive the extra information that was stored in the buffers by the function
            channels = channelsBuffer.get(0);
            sampleRate = sampleRateBuffer.get(0);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //Find the correct OpenAL format
        int format = -1;
        if (channels == 1)
        {
            format = AL_FORMAT_MONO16;
        }
        else if (channels == 2)
        {
            format = AL_FORMAT_STEREO16;
        }

        //Request space for the buffer
        int bufferPointer = alGenBuffers();

        //Send the data to OpenAL
        alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);

        //Free the memory allocated by STB
        free(rawAudioBuffer);

        this.buffers.put(path, bufferPointer);
    }

    protected void createMusic(String path)
    {
        ShortBuffer rawAudioBuffer = null;

        int channels = -1;
        int sampleRate = -1;

        try (MemoryStack stack = stackPush())
        {
            //Allocate space to store return information from the function
            IntBuffer channelsBuffer = stack.mallocInt(1);
            IntBuffer sampleRateBuffer = stack.mallocInt(1);

            InputStream in = getClass().getResourceAsStream(path);
            byte[] bytes = IOUtils.toByteArray(in);
            ByteBuffer b = MemoryUtil.memAlloc(bytes.length);
            b.put(bytes);
            b.flip();

            rawAudioBuffer = stb_vorbis_decode_memory(b, channelsBuffer, sampleRateBuffer);
            in.close();

            //Retreive the extra information that was stored in the buffers by the function
            channels = channelsBuffer.get(0);
            sampleRate = sampleRateBuffer.get(0);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //Find the correct OpenAL format
        int format = -1;
        if (channels == 1)
        {
            format = AL_FORMAT_MONO16;
        }
        else if (channels == 2)
        {
            format = AL_FORMAT_STEREO16;
        }

        //Request space for the buffer
        int bufferPointer = alGenBuffers();

        //Send the data to OpenAL
        alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);

        //Free the memory allocated by STB
        free(rawAudioBuffer);

        this.musicBuffers.put(path, bufferPointer);
    }

    public void exit()
    {
        alcDestroyContext(context);
        alcCloseDevice(device);

        for (String s: this.buffers.keySet())
        {
            int i = this.buffers.get(s);
            alDeleteBuffers(i);
        }
    }
}
