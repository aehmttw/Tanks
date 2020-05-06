package tanksonline;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.event.*;
import tanks.event.online.IOnlineServerEvent;
import tanks.network.NetworkEventMap;

import java.util.UUID;

public class TanksOnlineMessageReader
{
    public ByteBuf queue;
    protected boolean reading = false;
    protected int endpoint;
    protected int frame = 0;

    public boolean queueMessage(ByteBuf m, UUID clientID)
    {
        return this.queueMessage(null, m, clientID);
    }

    public boolean queueMessage(TanksOnlineServerHandler s, ByteBuf m, UUID clientID)
    {
        boolean reply = false;

        try
        {
            queue.writeBytes(m);

            if (queue.readableBytes() >= 4)
            {
                if (!reading)
                {
                    endpoint = queue.readInt();
                }

                reading = true;

                while (queue.readableBytes() >= endpoint)
                {
                    reply = reply || this.readMessage(s, queue, clientID);
                    queue.discardReadBytes();

                    reading = false;

                    if (queue.readableBytes() >= 4)
                    {
                        endpoint = queue.readInt();

                        reading = true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("A network exception has occurred: " + e.toString());
            Game.logger.println("A network exception has occurred: " + e.toString());
            e.printStackTrace();
            e.printStackTrace(Game.logger);
        }

        return reply;
    }

    public boolean readMessage(TanksOnlineServerHandler s, ByteBuf m, UUID clientID) throws Exception
    {
        int i = m.readInt();
        Class<? extends INetworkEvent> c = NetworkEventMap.get(i);

        INetworkEvent e = c.getConstructor().newInstance();
        e.read(m);

        if (e instanceof PersonalEvent)
        {
            ((PersonalEvent) e).clientID = clientID;
            ((PersonalEvent) e).frame = frame;
        }

        if (e instanceof EventKeepConnectionAlive)
            return true;
        else if (e instanceof IOnlineServerEvent)
            ((IOnlineServerEvent) e).execute(s);
        else if (e instanceof EventSendClientDetails)
            s.sendEventAndClose(new EventKick("This is not the party you are looking for..."));

        return false;
    }
}