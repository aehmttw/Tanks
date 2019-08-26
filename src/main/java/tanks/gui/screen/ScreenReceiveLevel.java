package tanks.gui.screen;

import tanks.network.Server;

public class ScreenReceiveLevel extends Screen
{
	public ScreenReceiveLevel()
	{
		try 
		{
			new Server(8080).run();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}	
	
	@Override
	public void update()
	{
		
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
	}

}
