package tanks.network.event;

import tanks.network.NetworkEventMap;

/** Given multiple events that implement this, if they have the same identifier, only the latest one is sent */
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
