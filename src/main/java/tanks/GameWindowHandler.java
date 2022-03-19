package tanks;

import basewindow.IWindowHandler;
import tanks.gui.screen.*;
import tanks.translation.Translation;

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
			ScreenPartyHost.server.close("The party host has closed their game");

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

		Game.game.window.setCursorLocked(false);
	}

}
