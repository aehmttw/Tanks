package tanks.network.event;

import tanks.network.*;

public class EventPing extends PersonalEvent implements IServerThreadEvent, IClientThreadEvent
{
    public boolean second;

    public EventPing()
    {

    }

    public EventPing(boolean second)
    {
        this.second = second;
    }

    @Override
    public void execute()
    {

    }

    @Override
    public void execute(ServerHandler s)
    {
        s.lastLatency = System.currentTimeMillis() - s.lastPingSent;
        s.sendEvent(new EventPing(true));
        s.pingReceived = true;
    }

    @Override
    public void execute(ClientHandler c)
    {
        if (!second)
        {
            c.lastPingSent = System.currentTimeMillis();
            c.sendEvent(new EventPing(true));
        }
        else
            c.lastLatency = System.currentTimeMillis() - c.lastPingSent;
    }
}
