package tanks.network;

import java.util.UUID;

import tanks.Game;
import tanks.event.INetworkEvent;
import tanks.event.PersonalEvent;
import tanks.gui.screen.ScreenHostingEnded;
import tanks.gui.screen.ScreenKicked;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;

public class MessageExecutor 
{
	public void executeMessage(String m, UUID clientID)
	{
		if (m == "")
		{
			return;
		}
		try
		{		
			String[] parts = m.split("#");
			String message = parts[1];
			
			int i = Integer.parseInt(parts[0]);
			Class<? extends INetworkEvent> c = NetworkEventMap.get(i);
			INetworkEvent e = c.getConstructor(String.class).newInstance(message);
			
			if (e instanceof PersonalEvent)
				((PersonalEvent)e).clientID = clientID;
			
			e.execute();
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
	}
}
