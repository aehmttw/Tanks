package tanks;

import lwjglwindow.IUpdater;

public class GameUpdater implements IUpdater
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
