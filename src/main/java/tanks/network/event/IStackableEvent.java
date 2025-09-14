package tanks.network.event;

import tanks.network.NetworkEventMap;

public interface IStackableEvent extends INetworkEvent
{
    int getIdentifier();

    default boolean isStackable()
    {
        return true;
    }

    static int key(IStackableEvent e)
    {
        return f(NetworkEventMap.get(e.getClass()) + f(e.getIdentifier()));
    }

    static int f(int i)
    {
        return 1664525 * i + 1013904223;
    }
}
