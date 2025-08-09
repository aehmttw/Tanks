package tanks;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.*;

public abstract class ErrorHandler<K, V>
{
    public static final ArrayList<ErrorHandler<?, ?>> errorHandlers = new ArrayList<>();

    public final Object2IntOpenHashMap<K> errorCounts = new Object2IntOpenHashMap<>();
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
        if (intervalTimer <= -Panel.frameFrequency * 10)
            intervalTimer = baseInterval;
    }

    public void checkForErrors(K obj)
    {
        if (Game.fixErrors && intervalTimer > 0)
            return;

        V info = containsErrors(obj);
        if (Objects.equals(info, noErrorReturnValue()))
        {
            errorCounts.removeInt(obj);
            return;
        }

        if (errorCounts.addTo(obj, 1) >= triggerCount)
        {
            handleError(obj, info);
            errorCounts.removeInt(obj);
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
