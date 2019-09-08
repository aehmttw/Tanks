package tanks.network;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.event.EventKeepConnectionAlive;
import tanks.event.INetworkEvent;
import tanks.event.IServerThreadEvent;
import tanks.event.PersonalEvent;
import tanks.gui.screen.ScreenHostingEnded;
import tanks.gui.screen.ScreenKicked;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;

import java.util.UUID;

public class MessageReader 
{
	public ByteBuf queue;
	protected boolean reading = false;
	protected int endpoint;

	public boolean queueMessage(ByteBuf m, UUID clientID)
	{
		return this.queueMessage(null, m, clientID);
	}

	public boolean queueMessage(ServerHandler s, ByteBuf m, UUID clientID)
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
			System.err.println("A network exception has occurred: " + m);
			Game.logger.println("A network exception has occurred: " + m);
			e.printStackTrace();
			e.printStackTrace(Game.logger);

			if (ScreenPartyHost.isServer)
				Game.screen = new ScreenHostingEnded("A network exception has occurred: " + m);
			else if (ScreenPartyLobby.isClient)
			{
				Game.screen = new ScreenKicked("A network exception has occurred: " + m);
				Client.handler.ctx.close();
				ScreenPartyLobby.connections.clear();
			}
		}

		return reply;
	}

	public boolean readMessage(ServerHandler s, ByteBuf m, UUID clientID) throws Exception
	{
		int i = m.readInt();
		Class<? extends INetworkEvent> c = NetworkEventMap.get(i);
	
		INetworkEvent e = c.getConstructor().newInstance();	
		e.read(m);

		if (e instanceof PersonalEvent)
			((PersonalEvent)e).clientID = clientID;

		if (e instanceof EventKeepConnectionAlive)
			return true;
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
