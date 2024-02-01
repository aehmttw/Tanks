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
			if (Game.enableExtensions)
			{
				for (int i = 0; i < Game.extensionRegistry.extensions.size(); i++)
				{
					Extension e = Game.extensionRegistry.extensions.get(i);

					e.preDraw();
				}
			}

			Panel.panel.draw();

			if (Game.enableExtensions)
			{
				for (int i = 0; i < Game.extensionRegistry.extensions.size(); i++)
				{
					Extension e = Game.extensionRegistry.extensions.get(i);

					e.drawPostMouse();
				}
			}
		}
		catch (Throwable e)
		{
			Game.exitToCrash(e);
		}
	}
}
