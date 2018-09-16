package tanks;

import java.awt.Graphics;

public class ScreenAdjustWindow extends Screen
{

	@Override
	public void update()
	{
		
	}

	@Override
	public void draw(Graphics g)
	{
		Window.drawInterfaceText(g, Window.interfaceSizeX / 2, Window.interfaceSizeY / 2, "Use the arrow keys to fit the red squares inside the screen.");
	}

}
