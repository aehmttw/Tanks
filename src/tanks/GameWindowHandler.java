package tanks;

import lwjglwindow.IWindowHandler;
import tanks.network.ServerHandler;

public class GameWindowHandler implements IWindowHandler
{

	@Override
	public void onWindowClose() 
	{
		if (ScreenPartyHost.isServer)
		{
			//synchronized(ScreenPartyHost.server.connections)
			{
				for (int i = 0; i < ScreenPartyHost.server.connections.size(); i++)
				{
					ServerHandler h = ScreenPartyHost.server.connections.get(i);
					h.kick(h.ctx, "The party host has closed their game");
				}
			}
		}
	}

}
