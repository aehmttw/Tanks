package tanks;

import basewindow.IUpdater;

public class GameUpdater implements IUpdater
{
	@Override
	public void update()
	{
		try
		{
			Panel.panel.update();
		}
		catch (Throwable e)
		{
			Game.exitToCrash(e);
		}
	}
}
