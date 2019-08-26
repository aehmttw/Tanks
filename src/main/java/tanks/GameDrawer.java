package tanks;

import lwjglwindow.IDrawer;
import tanks.gui.Panel;

public class GameDrawer implements IDrawer
{
	@Override
	public void draw()
	{
		try
		{
			Panel.panel.draw();
		}
		catch(Exception e)
		{
			Game.exitToCrash(e);
		}
	}
}
