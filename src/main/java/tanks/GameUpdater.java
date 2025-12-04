package tanks;

import basewindow.IUpdater;
import tanks.extension.Extension;

public class GameUpdater implements IUpdater
{
	@Override
	public void update()
	{
		try
		{
            for (Extension e: Game.extensionRegistry.extensions)
                e.preUpdate();

			Panel.panel.update();

            for (Extension e: Game.extensionRegistry.extensions)
                e.update();
		}
		catch (Throwable e)
		{
			if (e instanceof GameCrashedException)
				Game.displayCrashScreen(((GameCrashedException) e).originalException);
			else
				Game.displayCrashScreen(e);
		}
	}
}
