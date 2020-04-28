package basewindow;

public abstract class BaseSoundPlayer
{
    public BaseSoundPlayer()
    {

    }

    public abstract void playSound(String path);

    public abstract void playSound(String path, float pitch);

    public abstract void playSound(String path, float pitch, float volume);

    public abstract void exit();
}
