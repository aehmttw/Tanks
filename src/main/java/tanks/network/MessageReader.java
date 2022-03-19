package tanks.network;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.event.*;
import tanks.event.online.IOnlineServerEvent;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;

import java.util.UUID;

public class MessageReader 
{
	public static final int max_event_size = 1048576;

	public boolean useQueue = true;
	public ByteBuf queue;
	protected boolean reading = false;
	protected int endpoint;

	public boolean queueMessage(ByteBuf m, UUID clientID)
	{
		return this.queueMessage(null, m, clientID);
	}

	public synchronized boolean queueMessage(ServerHandler s, ByteBuf m, UUID clientID)
	{
		boolean reply = false;
		
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

							Client.handler.ctx.close();
							ScreenPartyLobby.connections.clear();
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
							if (ScreenPartyHost.isServer && s != null)
							{
								s.sendEventAndClose(new EventKick("A network exception has occurred: message size " + endpoint + " is too big!"));
							}
							else if (ScreenPartyLobby.isClient)
							{
								EventKick ev = new EventKick("A network exception has occurred: message size " + endpoint + " is too big!");
								ev.clientID = null;
								Game.eventsIn.add(ev);

								Client.handler.ctx.close();
								ScreenPartyLobby.connections.clear();
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

				Client.handler.ctx.close();
				ScreenPartyLobby.connections.clear();
			}
		}

		return reply;
	}

	public synchronized boolean readMessage(ServerHandler s, ByteBuf m, UUID clientID) throws Exception
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

		if (e instanceof EventPing)
			return true;
		else if (e instanceof IOnlineServerEvent)
			s.sendEventAndClose(new EventKick("This is a party, please join parties through the party menu"));
		else if (e instanceof IServerThreadEvent)
			((IServerThreadEvent) e).execute(s);
		else
		{
			synchronized (Game.eventsIn)
			{
				Game.eventsIn.add(e);
			}
		}

		return false;
	}
}
