package tanks;

import lwjglwindow.Updater;

public class GameUpdater extends Updater
{
	@Override
	public void update()
	{
		try
		{
			Panel.panel.update();
		}
		catch(Exception e)
		{
			Game.exitToCrash(e);
		}
	}
}
