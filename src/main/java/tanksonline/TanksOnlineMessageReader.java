package tanksonline;

import tanks.Game;
import tanks.network.EventObjectBuffer;
import tanks.network.NetworkMessage;
import tanks.network.event.*;
import tanks.network.event.online.IOnlineServerEvent;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class TanksOnlineMessageReader
{
    public ByteBuf queue;

    public boolean queueMessage(ByteBuf m, UUID clientID)
    {
        return this.queueMessage(null, m, clientID);
    }

    public boolean queueMessage(TanksOnlineServerHandler s, ByteBuf m, UUID clientID)
    {
        boolean reply = false;

        try
        {
            ByteBuf input = queue;
            input.writeBytes(m);
            ByteBuf message;
            while (input.refCnt() > 0 && (message = NetworkMessage.tryDecode(input)) != null)
            {
                try
                {
                    reply = this.readMessage(s, message, clientID) || reply;
                }
                finally
                {
                    message.release();
                }
                if (s != null && s.closed)
                    break;
            }
            if (input.refCnt() > 0)
                input.discardReadBytes();
        }
        catch (Exception e)
        {
            System.err.println("A network exception has occurred: " + e.toString());
            Game.logger.println("A network exception has occurred: " + e.toString());
            e.printStackTrace();
            e.printStackTrace(Game.logger);
            if (s != null && !s.closed)
                s.sendEventAndClose(new EventKick("A network exception has occurred: " + e));
        }

        return reply;
    }

    public boolean readMessage(TanksOnlineServerHandler s, ByteBuf m, UUID clientID) throws Exception
    {
        INetworkEvent e = EventObjectBuffer.decode(m);

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
