package tanks.network;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import tanks.Game;
import tanks.gui.screen.*;
import tanks.network.event.*;
import tanks.network.event.online.IOnlineServerEvent;

import java.util.UUID;

public class MessageReader
{
	public static final int max_event_size = 104857600;

	public static int downstreamBytes, upstreamBytes;
	public static int upstreamBytesPerSec, downstreamBytesPerSec;
    public static Int2IntOpenHashMap eventBytes = new Int2IntOpenHashMap(), eventBytesPerSec = new Int2IntOpenHashMap();
	public static long lastMessageTime;

	public boolean useQueue = true;
	public ByteBuf queue;
	protected boolean reading = false;
	protected int endpoint;

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
			byte[] bytes = new byte[59];
			m.getBytes(0, bytes);

			if (useQueue)
				queue.writeBytes(m);
			else
				queue = m;

			if (queue.readableBytes() >= 4)
			{
				if (!reading)
				{
					endpoint = queue.readInt();
					downstreamBytes += endpoint + 4;
					updateLastMessageTime();

					if (endpoint > max_event_size)
					{
						if (ScreenPartyHost.isServer && s != null)
						{
							s.sendEventAndClose(new EventKick("A network exception has occurred: message size " + endpoint + " is too big!"));
						}
						else if (ScreenPartyLobby.isClient)
						{
							EventKick ev = new EventKick("A network exception has occurred: message size " + endpoint + " is too big!");
							ev.clientID = null;
							Game.eventsIn.add(ev);

							Client.handler.close();
							ScreenPartyLobby.connections.clear();
						}

						return;
					}
				}

				reading = true;

				while (queue.readableBytes() >= endpoint)
				{
                    this.readMessage(s, c, queue, clientID);
                    queue.discardReadBytes();

					reading = false;

					if (queue.readableBytes() >= 4)
					{
						endpoint = queue.readInt();
						downstreamBytes += endpoint + 4;
						updateLastMessageTime();

						if (endpoint > MessageReader.max_event_size)
						{
							if (ScreenPartyHost.isServer && s != null)
							{
								s.sendEventAndClose(new EventKick("A network exception has occurred: message size " + endpoint + " is too big!"));
							}
							else if (ScreenPartyLobby.isClient)
							{
								EventKick ev = new EventKick("A network exception has occurred: message size " + endpoint + " is too big!");
								ev.clientID = null;
								Game.eventsIn.add(ev);

								Client.handler.close();
								ScreenPartyLobby.connections.clear();
							}

							return;
						}

						reading = true;
					}
				}
			}
		}
		catch (Exception e)
		{
			if (s != null)
			{
				System.err.println("A network exception has occurred: " + e + " (" + s.rawUsername + "/" + s.clientID + ")");
				Game.logger.println("A network exception has occurred: " + e + " (" + s.rawUsername + "/" + s.clientID + ")");
			}
			else
			{
				System.err.println("A network exception has occurred: " + e);
				Game.logger.println("A network exception has occurred: " + e);
			}

			e.printStackTrace();
			e.printStackTrace(Game.logger);

			if (ScreenPartyHost.isServer && s != null)
			{
				s.sendEventAndClose(new EventKick("A network exception has occurred: " + e));
				//Game.screen = new ScreenHostingEnded("A network exception has occurred: " + e.toString());
			}
			else if (ScreenPartyLobby.isClient)
			{
				EventKick ev = new EventKick("A network exception has occurred: " + e);
				ev.clientID = null;
				Game.eventsIn.add(ev);

				Client.handler.close();
				ScreenPartyLobby.connections.clear();
			}
		}
	}

	public synchronized void readMessage(ServerHandler s, ClientHandler ch, ByteBuf m, UUID clientID) throws Exception
	{
        int rb = m.readerIndex();
		int i = m.readShort();
		Class<? extends INetworkEvent> c = NetworkEventMap.get(i);

		if (c == null)
			throw new Exception("Invalid network event: " + i + " (Previous event: " + NetworkEventMap.get(this.lastID) + ")");

		INetworkEvent e = c.getConstructor().newInstance();
        try
        {
            e.read(m);
        }
        catch (Exception exc)
        {
            throw new RuntimeException("Failed to read network event " + c + " (previous event: " + NetworkEventMap.get(this.lastID) + "):" + exc.getMessage());
        }

        if (Game.recordEventData)
            eventBytes.addTo(i, m.readerIndex() - rb);

		if (e instanceof PersonalEvent)
            ((PersonalEvent) e).clientID = clientID;

		if (e instanceof IOnlineServerEvent)
			s.sendEventAndClose(new EventKick("This is a party, please join parties through the party menu"));
		else if (e instanceof IServerThreadEvent && s != null)
			((IServerThreadEvent) e).execute(s);
		else if (e instanceof IClientThreadEvent && ch != null)
			((IClientThreadEvent) e).execute(ch);
		else
		{
			synchronized (Game.eventsIn)
			{
				Game.eventsIn.add(e);
			}
		}

        this.lastID = i;
    }

	public static void updateLastMessageTime()
	{
		long time = System.currentTimeMillis() / 1000;

        if (lastMessageTime >= time)
            return;

        lastMessageTime = time;
        upstreamBytesPerSec = upstreamBytes;
        downstreamBytesPerSec = downstreamBytes;
        upstreamBytes = 0;
        downstreamBytes = 0;

        if (Game.recordEventData)
        {
            eventBytesPerSec = eventBytes;
            eventBytes = new Int2IntOpenHashMap();
        }
    }
}
