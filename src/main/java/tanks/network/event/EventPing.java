package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.network.ClientHandler;
import tanks.network.ServerHandler;

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
    public void write(ByteBuf b)
    {
        b.writeBoolean(second);
    }

    @Override
    public void read(ByteBuf b)
    {
        second = b.readBoolean();
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
