package lwjglwindow;

import basewindow.BaseSoundPlayer;
import org.apache.commons.io.IOUtils;
import org.lwjgl.openal.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

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
    public LWJGLWindow window;

    public long context;
    public long device;

    public HashMap<String, Integer> buffers = new HashMap<>();
    public ArrayList<Integer> sources = new ArrayList<>();

    public HashMap<String, Integer> musicBuffers = new HashMap<>();
    public ArrayList<Integer> musicSources = new ArrayList<>();
    public ArrayList<Integer> playingMusicSources = new ArrayList<>();

    public boolean musicsToLoad = false;
    public final HashMap<String, Integer> finishedMusicBuffers = new HashMap<>();

    public HashMap<String, Integer> syncedTracks = new HashMap<>();
    public HashMap<String, Integer> stoppingSyncedTracks = new HashMap<>();
    public HashMap<String, Float> syncedTrackCurrentVolumes = new HashMap<>();
    public HashMap<String, Float> syncedTrackMaxVolumes = new HashMap<>();
    public HashMap<String, Float> syncedTrackFadeRate = new HashMap<>();
    protected ArrayList<String> removeTracks = new ArrayList<>();

    public int currentMusic = -1;
    public int prevMusic = -1;

    public String musicID = null;
    public float musicSpeed = 1;

    public long fadeBegin = 0;
    public long fadeEnd = 0;

    public float prevVolume;
    public float currentVolume;

    public static final int total_sounds = 255;
    public static final int max_music = 50;

    protected void playMusicSource(int i)
    {
        alSourcePlay(i);
        this.playingMusicSources.add(i);
    }

    protected void stopMusicSource(int i)
    {
        alSourceStop(i);
        this.playingMusicSources.remove((Integer) i);
    }

    protected int newMusicSource()
    {
        if (musicSources.size() >= max_music)
        {
            for (int i = 0; i < musicSources.size(); i++)
            {
                if (!playingMusicSources.contains(musicSources.get(i)))
                {
                    alDeleteSources(musicSources.remove(i));
                    return alGenSources();
                }
            }

            alDeleteSources(musicSources.remove(0));
        }

        return alGenSources();
    }

    /**
     * Warning! This will give an exception if there are no audio devices plugged into the computer!
     */
    public SoundPlayer(LWJGLWindow window)
    {
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        device = alcOpenDevice(defaultDeviceName);

        int[] attributes = {0};
        context = alcCreateContext(device, attributes);
        alcMakeContextCurrent(context);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        this.window = window;
    }

    @Override
    public void loadMusic(String path)
    {
        new Thread(() ->
        {
            musicsToLoad = false;
            int i = setupMusic(path);

            synchronized (finishedMusicBuffers)
            {
                finishedMusicBuffers.put(path, i);
                musicsToLoad = true;
            }
        }).start();
    }

    public void loadMusic(String path, InputStream in)
    {
        new Thread(() ->
        {
            musicsToLoad = false;
            int i = setupMusic(path, in);

            synchronized (finishedMusicBuffers)
            {
                finishedMusicBuffers.put(path, i);
                musicsToLoad = true;
            }
        }).start();
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
        if (sources.size() >= total_sounds - max_music)
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
        int sourcePointer = newMusicSource();

        if (this.musicBuffers.get(path) == null)
            createMusic(path);

        int bufferPointer = this.musicBuffers.get(path);

        int loop = AL_FALSE;

        if (looped)
            loop = AL_TRUE;


        stopMusicSource(prevMusic);
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
            alSourcef(sourcePointer, AL_PITCH, musicSpeed);

            fadeBegin = System.currentTimeMillis();
            fadeEnd = System.currentTimeMillis() + fadeTime;
        }
        else
        {
            for (int i: this.syncedTracks.values())
                stopMusicSource(i);

            musicSpeed = 1;
            this.syncedTracks.clear();
            this.stoppingSyncedTracks.clear();
            this.syncedTrackMaxVolumes.clear();
            this.syncedTrackCurrentVolumes.clear();
            this.syncedTrackFadeRate.clear();
        }

        playMusicSource(sourcePointer);
        prevVolume = volume;

        this.musicID = continueID;

        musicSources.add(sourcePointer);
    }

    @Override
    public void addSyncedMusic(String path, float volume, boolean looped, long fadeTime)
    {
        int sourcePointer = newMusicSource();

        if (this.musicBuffers.get(path) == null)
            createMusic(path);

        int bufferPointer = this.musicBuffers.get(path);

        int loop = AL_FALSE;

        if (looped)
            loop = AL_TRUE;

        Integer i = this.stoppingSyncedTracks.remove(path);
        if (i != null)
        {
            stopMusicSource(i);
        }

        alSourcei(sourcePointer, AL_BUFFER, bufferPointer);
        alSourcef(sourcePointer, AL_LOOPING, loop);
        alSourcef(sourcePointer, AL_PITCH, musicSpeed);
        alSourcef(sourcePointer, EXTOffset.AL_SEC_OFFSET, alGetSourcef(currentMusic, EXTOffset.AL_SEC_OFFSET));
        this.syncedTracks.put(path, sourcePointer);

        if (fadeTime <= 0)
        {
            this.syncedTrackCurrentVolumes.put(path, volume);
            this.syncedTrackMaxVolumes.put(path, volume);
            this.syncedTrackFadeRate.put(path, 0f);
            alSourcef(sourcePointer, AL_GAIN, volume);
        }
        else
        {
            this.syncedTrackCurrentVolumes.put(path, 0f);
            this.syncedTrackMaxVolumes.put(path, volume);
            this.syncedTrackFadeRate.put(path, volume / fadeTime * 10);
            alSourcef(sourcePointer, AL_GAIN, 0f);
        }

        playMusicSource(sourcePointer);

        musicSources.add(sourcePointer);
    }

    @Override
    public void removeSyncedMusic(String path, long fadeTime)
    {
        Integer i = this.syncedTracks.get(path);

        if (i != null)
        {
            this.stoppingSyncedTracks.put(path, i);
            this.syncedTrackFadeRate.put(path, this.syncedTrackMaxVolumes.get(path) / fadeTime * 10);
            //alSourceStop(i);
        }
    }

    @Override
    public void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime, boolean stoppable)
    {
        this.playMusic(path, volume, looped, continueID, fadeTime);
    }

    @Override
    public void setMusicSpeed(float speed)
    {
        this.musicSpeed = speed;
        alSourcef(this.currentMusic, AL_PITCH, speed);
        alSourcef(this.prevMusic, AL_PITCH, speed);

        for (int i: this.syncedTracks.values())
            alSourcef(i, AL_PITCH, speed);
    }

    @Override
    public void setMusicVolume(float volume)
    {
        this.currentVolume = volume;
        alSourcef(this.currentMusic, AL_GAIN, volume);
        alSourcef(this.prevMusic, AL_GAIN, volume);

        for (int i: this.syncedTracks.values())
            alSourcef(i, AL_GAIN, volume);
    }

    @Override
    public float getMusicPos()
    {
        return alGetSourcef(currentMusic, EXTOffset.AL_SEC_OFFSET);
    }

    @Override
    public void setMusicPos(float pos)
    {
        alSourcef(this.currentMusic, EXTOffset.AL_SEC_OFFSET, pos);
        alSourcef(this.prevMusic, EXTOffset.AL_SEC_OFFSET, pos);

        for (int i: this.syncedTracks.values())
            alSourcef(i, EXTOffset.AL_SEC_OFFSET, pos);
    }

    @Override
    public void stopMusic()
    {
        stopMusicSource(currentMusic);
        stopMusicSource(prevMusic);
        alSourceUnqueueBuffers(currentMusic);
        alSourceUnqueueBuffers(prevMusic);
        this.currentMusic = -1;
        this.prevMusic = -1;
        this.musicID = null;

        for (int i: this.syncedTracks.values())
            stopMusicSource(i);

        this.syncedTracks.clear();
        this.stoppingSyncedTracks.clear();
        this.syncedTrackMaxVolumes.clear();
        this.syncedTrackCurrentVolumes.clear();
        this.syncedTrackFadeRate.clear();
    }

    protected void createSound(String path)
    {
        this.createSound(path, null);
    }

    public void createSound(String path, InputStream in)
    {
        ShortBuffer rawAudioBuffer = null;

        int channels = -1;
        int sampleRate = -1;

        try (MemoryStack stack = stackPush())
        {
            //Allocate space to store return information from the function
            IntBuffer channelsBuffer = stack.mallocInt(1);
            IntBuffer sampleRateBuffer = stack.mallocInt(1);

            if (in == null)
                in = this.window.getResource(path);
            else
                path = "/" + path;

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
        catch (Exception e)
        {
            System.err.println("Failed to create sound " + path);
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
        if (rawAudioBuffer != null)
            alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);

        //Free the memory allocated by STB
        free(rawAudioBuffer);

        this.buffers.put(path, bufferPointer);
    }

    protected int setupMusic(String path)
    {
        return this.setupMusic(path, null);
    }

    protected int setupMusic(String path, InputStream in)
    {
        ShortBuffer rawAudioBuffer = null;

        int channels = -1;
        int sampleRate = -1;

        try (MemoryStack stack = stackPush())
        {
            //Allocate space to store return information from the function
            IntBuffer channelsBuffer = stack.mallocInt(1);
            IntBuffer sampleRateBuffer = stack.mallocInt(1);

            if (in == null)
                in = this.window.getResource(path);
            else
                path = "/" + path;

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
        catch (Exception e)
        {
            System.err.println("Failed to create music " + path);
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

        return bufferPointer;
    }

    public void createMusic(String path, InputStream in)
    {
        this.musicBuffers.put(path, this.setupMusic(path, in));
    }

    protected void createMusic(String path)
    {
        this.musicBuffers.put(path, this.setupMusic(path));
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

    @Override
    public void update()
    {
        this.musicPlaying = this.currentMusic != -1 && AL10.alGetSourcef(this.currentMusic, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;

        if (this.prevMusic != -1 && this.fadeEnd < System.currentTimeMillis())
        {
            stopMusicSource(this.prevMusic);
            this.prevMusic = -1;

            if (this.currentMusic != -1)
                AL10.alSourcef(this.currentMusic, AL10.AL_GAIN, this.currentVolume);
        }

        if (this.prevMusic != -1 && this.currentMusic != -1)
        {
            double frac = (System.currentTimeMillis() - this.fadeBegin) * 1.0 / (this.fadeEnd - this.fadeBegin);

            AL10.alSourcef(this.prevMusic, AL10.AL_GAIN, (float) (this.prevVolume * (1 - frac)));
            AL10.alSourcef(this.currentMusic, AL10.AL_GAIN, (float) (this.currentVolume * frac));
        }

        if (this.musicsToLoad)
        {
            synchronized (this.finishedMusicBuffers)
            {
                for (String path : this.finishedMusicBuffers.keySet())
                {
                    this.musicBuffers.put(path, this.finishedMusicBuffers.get(path));
                }
            }
        }

        for (String s: this.syncedTracks.keySet())
        {
            int i = this.syncedTracks.get(s);
            float vol = this.syncedTrackCurrentVolumes.get(s);

            if (this.stoppingSyncedTracks.containsKey(s))
            {
                vol = (float) (vol - window.frameFrequency * this.syncedTrackFadeRate.get(s));
                AL10.alSourcef(i, AL10.AL_GAIN, vol);
                this.syncedTrackCurrentVolumes.put(s, vol);

                if (vol <= 0)
                {
                    stopMusicSource(i);
                    this.stoppingSyncedTracks.remove(s);
                    this.syncedTrackFadeRate.remove(s);
                    this.syncedTrackMaxVolumes.remove(s);
                    this.syncedTrackCurrentVolumes.remove(s);
                    removeTracks.add(s);
                }
            }
            else
            {
                if (vol < this.syncedTrackMaxVolumes.get(s))
                {
                    vol = (float) Math.min(vol + window.frameFrequency * this.syncedTrackFadeRate.get(s), this.syncedTrackMaxVolumes.get(s));
                    this.syncedTrackCurrentVolumes.put(s, vol);
                    AL10.alSourcef(i, AL10.AL_GAIN, vol);
                }
            }
        }

        for (String r: removeTracks)
        {
            this.syncedTracks.remove(r);
        }

        this.removeTracks.clear();
    }
}
