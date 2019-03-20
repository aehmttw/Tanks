package tanks;

import lwjglwindow.Drawer;

public class GameDrawer extends Drawer
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
