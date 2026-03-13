package lwjglwindow;

import basewindow.BaseSoundPlayer;

import java.io.InputStream;

/**
 * Does nothing, useful if you don't have a sound device plugged in!
 */
public class NoSoundPlayer extends BaseSoundPlayer
{
    public LWJGLWindow window;
    public String musicID = null;
    public float musicSpeed = 1;
    public float currentVolume;
    public long musicStart = 0;

    public NoSoundPlayer(LWJGLWindow window)
    {
        this.window = window;
    }

    @Override
    public void loadMusic(String path)
    {

    }

    public void loadMusic(String path, InputStream in)
    {
    }

    public void playSound(String path)
    {

    }

    public void playSound(String path, float pitch)
    {

    }

    public void playSound(String path, float pitch, float volume)
    {

    }

    @Override
    public void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime)
    {
        if (!(continueID != null && continueID.equals(this.musicID)))
        {
            this.musicStart = System.currentTimeMillis();
            musicSpeed = 1;
        }

        this.currentVolume = volume;
        this.musicID = continueID;
    }

    @Override
    public void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime, boolean stoppable)
    {

    }

    @Override
    public void addSyncedMusic(String path, float volume, boolean looped, long fadeTime)
    {

    }

    @Override
    public void removeSyncedMusic(String path, long fadeTime)
    {

    }

    @Override
    public void setMusicSpeed(float speed)
    {
        this.musicSpeed = speed;
    }

    @Override
    public void setMusicVolume(float volume)
    {
        this.currentVolume = volume;
    }

    @Override
    public float getMusicPos()
    {
        return 0;
    }

    @Override
    public void setMusicPos(float pos)
    {
        this.musicStart = System.currentTimeMillis() - (long) (pos * 1000);
    }

    @Override
    public long getMusicStartTime()
    {
        return this.musicStart;
    }

    @Override
    public void stopMusic()
    {
        this.musicID = null;
    }

    public void exit()
    {

    }

    @Override
    public void update()
    {

    }

    @Override
    public void createSound(String path, InputStream in)
    {

    }

    @Override
    public void createMusic(String path, InputStream in)
    {

    }

}
