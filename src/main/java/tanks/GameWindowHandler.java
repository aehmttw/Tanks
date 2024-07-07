package tanks;

import basewindow.IWindowHandler;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenOptions;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;

public class GameWindowHandler implements IWindowHandler
{
	@Override
	public boolean attemptCloseWindow()
	{
		if (!Game.warnBeforeClosing)
			return true;

		if (!Game.screen.allowClose)
			Game.screen.onAttemptClose();

		return Game.screen.allowClose;
	}

	@Override
	public void onWindowClose() 
	{
		if (Game.steamNetworkHandler.initialized)
			Game.steamNetworkHandler.exit();

		ScreenOptions.saveOptions(Game.homedir);

		if (ScreenPartyHost.isServer)
		{
			ScreenPartyHost.server.close("The party host has closed their game");
		}

		try
		{
			if (Crusade.currentCrusade != null && !ScreenPartyHost.isServer && !ScreenPartyLobby.isClient && Game.screen instanceof ScreenGame)
			{
				Crusade.currentCrusade.quit();
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

}
