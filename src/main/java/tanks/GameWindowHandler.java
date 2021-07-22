package tanks;

import basewindow.IWindowHandler;
import tanks.gui.screen.ScreenOptions;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.levelbuilder.ScreenLevelBuilderOverlay;
import tanks.gui.screen.levelbuilder.ScreenLevelEditor;

public class GameWindowHandler implements IWindowHandler
{
	@Override
	public void onWindowClose()
	{
		if (Game.screen instanceof ScreenLevelEditor)
			((ScreenLevelEditor) Game.screen).save();

		else if (Game.screen instanceof ScreenLevelBuilderOverlay)
			((ScreenLevelBuilderOverlay) Game.screen).screenLevelEditor.save();

		if (Game.steamNetworkHandler.initialized)
			Game.steamNetworkHandler.exit();

		ScreenOptions.saveOptions(Game.homedir);

		if (ScreenPartyHost.isServer)
			ScreenPartyHost.server.close("The party host has closed their game");

		try {
			if (Crusade.currentCrusade != null && !ScreenPartyHost.isServer)
				Game.player.saveCrusade();
		}
		catch (Exception e) {
			Game.exitToCrash(e);
		}
	}
}
