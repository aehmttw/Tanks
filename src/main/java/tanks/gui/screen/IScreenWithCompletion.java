package tanks.gui.screen;

public interface IScreenWithCompletion
{
    Runnable getOnComplete();
    void setOnComplete(Runnable r);
}
