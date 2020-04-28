package tanks;

import basewindow.IDrawer;

public class GameDrawer implements IDrawer
{
	@Override
	public void draw()
	{
		try
		{
			Panel.panel.draw();
		}
		catch (Throwable e)
		{
			Game.exitToCrash(e);
		}
	}
}
