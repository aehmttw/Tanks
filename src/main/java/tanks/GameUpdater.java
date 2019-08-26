package tanks;

import lwjglwindow.IUpdater;
import tanks.gui.Panel;

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
