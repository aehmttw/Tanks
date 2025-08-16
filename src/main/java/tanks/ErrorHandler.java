package tanks;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
