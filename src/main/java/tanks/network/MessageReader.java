package tanks.network;

import tanks.Game;
import tanks.gui.screen.*;
import tanks.network.event.*;
import tanks.network.event.online.IOnlineServerEvent;
import tanks.translation.Translation;

import io.netty.buffer.ByteBuf;

import java.util.*;
import java.util.stream.Collectors;

public class MessageReader
{
    public static final int max_event_size = 104857600;

    public static int downstreamBytes;
    public static int upstreamBytes;
    public static long lastMessageTime;

    public static HashMap<Integer, Integer> eventBytes = new HashMap<>();

    public static int upstreamBytesPerSec;
    public static int downstreamBytesPerSec;
    public static List<String> eventBytesPerSecText = new ArrayList<>();

    public ByteBuf queue;

    protected int lastID;

    public void queueMessage(ClientHandler c, ByteBuf m, UUID clientID)
    {
        this.queueMessage(null, c, m, clientID);
    }

    public void queueMessage(ServerHandler s, ByteBuf m, UUID clientID)
    {
        this.queueMessage(s, null, m, clientID);
    }

    public synchronized void queueMessage(ServerHandler s, ClientHandler c, ByteBuf m, UUID clientID)
    {
        try
        {
            ByteBuf input = queue;
            input.writeBytes(m);

            ByteBuf message;
            while (input.refCnt() > 0 && (message = NetworkMessage.tryDecode(input)) != null)
            {
                try
                {
                    this.readMessage(s, c, message, clientID);
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
            if (s != null)
            {
                System.err.println("A network exception has occurred: " + e.toString() + " (" + s.rawUsername + "/" + s.clientID + ")");
                Game.logger.println("A network exception has occurred: " + e.toString() + " (" + s.rawUsername + "/" + s.clientID + ")");
            }
            else
            {
                System.err.println("A network exception has occurred: " + e.toString());
                Game.logger.println("A network exception has occurred: " + e.toString());
            }

            e.printStackTrace();
            e.printStackTrace(Game.logger);

            if (ScreenPartyHost.isServer && s != null)
            {
                s.sendEventAndClose(new EventKick("A network exception has occurred: " + e.toString()));
                //Game.screen = new ScreenHostingEnded("A network exception has occurred: " + e.toString());
            }
            else if (ScreenPartyLobby.isClient)
            {
                EventKick ev = new EventKick("A network exception has occurred: " + e.toString());
                ev.clientID = null;
                Game.eventsIn.add(ev);

                Client.handler.close();
                ScreenPartyLobby.connections.clear();
            }
        }
    }

    public synchronized void readMessage(ServerHandler s, ClientHandler ch, ByteBuf m, UUID clientID) throws Exception
    {
        INetworkEvent e = EventObjectBuffer.decode(m);
        Class<? extends INetworkEvent> c = e.getClass();
        this.lastID = NetworkEventMap.get(c);

        try
        {
            if (e instanceof PersonalEvent)
                ((PersonalEvent) e).clientID = clientID;

            if (e instanceof IOnlineServerEvent && s != null)
                s.sendEventAndClose(new EventKick("This is a party, please join parties through the party menu"));
            else if (e instanceof IServerThreadEvent && s != null)
                ((IServerThreadEvent) e).execute(s);
            else if (e instanceof IClientThreadEvent && ch != null)
                ((IClientThreadEvent) e).execute(ch);
            else
            {
                if (ScreenPartyHost.isServer && s != null && s.clientID == null)
                {
                    s.sendEventAndClose(new EventKick("Invalid event received. Must send EventSendClientDetails first."));
                    return;
                }

                synchronized (Game.eventsIn)
                {
                    Game.eventsIn.add(e);
                }
            }
        }
        catch (Exception error)
        {
            System.err.println("A network exception has occurred:");
            error.printStackTrace();

            if (s != null)
                s.sendEventAndClose(new EventKick("A network exception has occurred: " + error));
            else if (ch != null)
            {
                EventKick ev = new EventKick("A network exception has occurred: " + error);
                ev.clientID = null;
                Game.eventsIn.add(ev);

                Client.handler.close();
                ScreenPartyLobby.connections.clear();
            }
        }
    }

    public static synchronized void recordSent(int id, int size)
    {
        upstreamBytes += size;
        eventBytes.put(id, eventBytes.getOrDefault(id, 0) + size);
        updateLastMessageTime();
    }

    public static synchronized void recordReceived(int size)
    {
        downstreamBytes += size;
        updateLastMessageTime();
    }

    public static synchronized void updateLastMessageTime()
    {
        long time = System.currentTimeMillis() / 1000;

        if (lastMessageTime < time)
        {
            lastMessageTime = time;
            upstreamBytesPerSec = upstreamBytes;
            downstreamBytesPerSec = downstreamBytes;
            eventBytesPerSecText = eventBytes.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(entry -> Translation.translate("%s: %.2f KB/s", NetworkEventMap.get(entry.getKey()).getSimpleName(), entry.getValue() / 1024.))
                .collect(Collectors.toList());
            eventBytes.clear();
            upstreamBytes = 0;
            downstreamBytes = 0;
        }
    }
}
