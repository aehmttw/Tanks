package tanks;

import lwjglwindow.IWindowHandler;
import tanks.gui.screen.ScreenPartyHost;

public class GameWindowHandler implements IWindowHandler
{

	@Override
	public void onWindowClose() 
	{
		if (ScreenPartyHost.isServer)
		{
			ScreenPartyHost.server.close("The party host has closed their game");
		}
	}

}
