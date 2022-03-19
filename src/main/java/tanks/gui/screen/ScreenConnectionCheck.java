package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class ScreenConnectionCheck extends Screen
{	
	public boolean connecting = true;
	public boolean waiting = true;

	public Screen screen;
	
	public ScreenConnectionCheck(Screen s)
	{
		this.screen = s;
	}

	Button back = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Ok", () -> Game.screen = new ScreenPlayMultiplayer()
	);

	@Override
	public void update() 
	{
		if (!this.connecting)
			back.update();
		else if (this.waiting)
		{
			new Thread(() ->
			{
				String ip = "%";
				try
				{
					ip = Inet4Address.getLocalHost().getHostAddress();
				}
				catch (UnknownHostException ignored) { }

				if (!ip.contains("%"))
					Game.screen = screen;

				Game.ip = ip;
				connecting = false;
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
		Drawing.drawing.setInterfaceFontSize(this.textSize);

		if (!this.connecting)
		{
			Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace, "You must connect to a network to play with others!");
			back.draw();
		}
		else
		{
			Drawing.drawing.displayInterfaceText(this.centerX, this.centerY, "One moment please...");
		}
	}

}
