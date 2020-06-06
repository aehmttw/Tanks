package tanks;

import basewindow.IWindowHandler;
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

		try
		{
			Game.player.saveCrusade(Game.game.fileManager.getFile(Game.homedir + Game.savedCrusadePath));
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

}
