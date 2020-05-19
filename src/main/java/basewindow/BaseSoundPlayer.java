package basewindow;

public abstract class BaseSoundPlayer
{
    public boolean musicPlaying = false;

    public BaseSoundPlayer()
    {

    }

    public abstract void playSound(String path);

    public abstract void playSound(String path, float pitch);

    public abstract void playSound(String path, float pitch, float volume);

    public abstract void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime);

    public abstract void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime, boolean stoppable);

    public abstract void stopMusic();

    public abstract void registerCombinedMusic(String path, String id);

    public abstract void exit();
}
