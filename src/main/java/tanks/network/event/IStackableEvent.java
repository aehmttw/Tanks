package tanks.network.event;

public interface IStackableEvent extends INetworkEvent
{
    int getIdentifier();

    default boolean isStackable()
    {
        return true;
    }

    static int f(int i)
    {
        return 1664525 * i + 1013904223;
    }
}
