package tanksonline;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.network.event.*;
import tanks.network.event.online.IOnlineServerEvent;
import tanks.network.MessageReader;
import tanks.network.NetworkEventMap;

import java.util.UUID;

public class TanksOnlineMessageReader
{
    public ByteBuf queue;
    protected boolean reading = false;
    protected int endpoint;

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

                    if (endpoint > MessageReader.max_event_size)
                    {
                        if (s != null)
                        {
                            s.sendEventAndClose(new EventKick("A network exception has occurred: message size " + endpoint + " is too big!"));
                        }

                        return false;
                    }
                }

                reading = true;

                while (queue.readableBytes() >= endpoint)
                {
                    reply = this.readMessage(s, queue, clientID) || reply;
                    queue.discardReadBytes();

                    reading = false;

                    if (queue.readableBytes() >= 4)
                    {
                        endpoint = queue.readInt();

                        if (endpoint > MessageReader.max_event_size)
                        {
                            if (s != null)
                            {
                                s.sendEventAndClose(new EventKick("A network exception has occurred: message size " + endpoint + " is too big!"));
                            }

                            return false;
                        }

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

        if (c == null)
            throw new Exception("Invalid network event: " + i);

        INetworkEvent e = c.getConstructor().newInstance();
        e.read(m);

        if (e instanceof PersonalEvent)
        {
            ((PersonalEvent) e).clientID = clientID;
        }

        if (e instanceof IOnlineServerEvent)
            ((IOnlineServerEvent) e).execute(s);
        else if (e instanceof EventSendClientDetails)
            s.sendEventAndClose(new EventKick("This is not the party you are looking for..."));

        return false;
    }
}
