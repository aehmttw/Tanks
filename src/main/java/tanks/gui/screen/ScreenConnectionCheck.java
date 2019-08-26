package tanks.gui.screen;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenConnectionCheck extends Screen
{	
	public boolean connecting = true;
	public boolean waiting = true;

	public Screen screen;
	
	public ScreenConnectionCheck(Screen s)
	{
		this.screen = s;
	}

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Ok", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenPlay();
		}
	}
			);

	@Override
	public void update() 
	{
		if (!this.connecting)
			back.update();
		else if (this.waiting)
		{
			new Thread(new Runnable()
			{

				@Override
				public void run() 
				{
					String ip = "%";
					try 
					{
						ip = Inet4Address.getLocalHost().getHostAddress();
					} 
					catch (UnknownHostException e) { }
					
					if (!ip.contains("%"))
						Game.screen = screen;
					
					Game.ip = ip;
					connecting = false;
				}
			}
					).start();
			
		}
		
		if (this.waiting)
			this.waiting = false;
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setFontSize(24);

		if (!this.connecting)
		{
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, "You must connect to a network to play with others!");
			back.draw();
		}
		else
		{
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "One moment please...");
		}
	}

}
