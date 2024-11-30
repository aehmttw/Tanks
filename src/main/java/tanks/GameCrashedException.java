package tanks;

public class GameCrashedException extends RuntimeException
{
    public Throwable originalException;

    public GameCrashedException(Throwable in)
    {
        this.originalException = in;
    }
}
