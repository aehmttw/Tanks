package basewindow;

import java.io.InputStream;

public abstract class BaseSoundPlayer
{
    public boolean musicPlaying = false;

    public BaseSoundPlayer()
    {

    }

    public abstract void loadMusic(String path);

    public abstract void playSound(String path);

    public abstract void playSound(String path, float pitch);

    public abstract void playSound(String path, float pitch, float volume);

    public abstract void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime);

    public abstract void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime, boolean stoppable);

    public abstract void addSyncedMusic(String path, float volume, boolean looped, long fadeTime);

    public abstract void removeSyncedMusic(String path, long fadeTime);

    public abstract void stopMusic();

    public abstract void registerCombinedMusic(String path, String id);

    public abstract void exit();

    public abstract void update();

    public abstract void createSound(String path, InputStream in);

    public abstract void createMusic(String path, InputStream in);

    public abstract void loadMusic(String path, InputStream in);
}
