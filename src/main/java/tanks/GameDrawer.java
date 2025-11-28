package tanks;

import basewindow.IDrawer;
import tanks.extension.Extension;

public class GameDrawer implements IDrawer
{
	@Override
	public void draw()
	{
		try
		{
            for (Extension e: Game.extensionRegistry.extensions)
                e.preDraw();

            Panel.panel.draw();

            for (Extension e: Game.extensionRegistry.extensions)
                e.draw();
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
