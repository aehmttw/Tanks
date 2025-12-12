package tanks;

import tanks.tank.Tank;

import java.util.*;

public abstract class ErrorHandler<K, V>
{
    public static String gameObjectString(GameObject o)
    {
        return String.format(
            "%s@(%.0f,%.0f)",
            o instanceof Tank ? ((Tank) o).name : o.getClass().getSimpleName(),
            o.posX, o.posY
        );
    }

    public static final ArrayList<ErrorHandler<?, ?>> errorHandlers = new ArrayList<>();

    public final HashMap<K, Integer> errorCounts = new HashMap<>();
    public double baseInterval, intervalTimer;
    public int triggerCount;

    public ErrorHandler(double interval, int triggerCount)
    {
        this.baseInterval = interval;
        this.intervalTimer = interval;
        this.triggerCount = triggerCount;
        errorHandlers.add(this);
    }

    public void updateTimer()
    {
        intervalTimer -= Panel.frameFrequency;
        if (intervalTimer <= -Panel.frameFrequency * 5)
            intervalTimer = baseInterval * (Game.disableErrorFixing ? 0.2 : 1);
    }

    public void checkForErrors(K obj)
    {
        if (intervalTimer > 0)
            return;

        V info = containsErrors(obj);
        if (Objects.equals(info, noErrorReturnValue()))
        {
            errorCounts.remove(obj);
            return;
        }

        int count = errorCounts.getOrDefault(obj, 0) + 1;
        errorCounts.put(obj, count);
        if (count >= triggerCount)
        {
            handleError(obj, info);
            errorCounts.remove(obj);
        }
    }

    public void reset()
    {
        errorCounts.clear();
        intervalTimer = baseInterval;
    }

    public V noErrorReturnValue()
    {
        return null;
    }

    public abstract V containsErrors(K obj);
    public abstract void handleError(K obj, V info);
}
