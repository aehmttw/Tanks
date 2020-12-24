package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.event.EventKick;
import tanks.gui.Button;
import tanks.network.ServerHandler;

public class ScreenHostingEnded extends Screen
{
	public String reason;

	public ScreenHostingEnded(String reason)
	{
		this.music = "tomato_feast_1.ogg";
		this.musicID = "menu";
		Panel.forceRefreshMusic = true;

		Drawing.drawing.playSound("leave.ogg");
		this.reason = reason;

		//synchronized(ScreenPartyHost.server.connections)
		{
			while (ScreenPartyHost.server.connections.size() > 0)
			{
				for (int i = 0; i < ScreenPartyHost.server.connections.size(); i++)
				{
					ServerHandler c = ScreenPartyHost.server.connections.get(i);
					c.sendEventAndClose(new EventKick(reason));
				}
			}
		}
		
		ScreenPartyHost.isServer = false;
		ScreenPartyHost.server.close();
		ScreenPartyHost.activeScreen = null;
	}

	Button back = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Ok", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenParty();
		}
	}
			);

	@Override
	public void update() 
	{
		back.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(this.textSize);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace, this.reason);

		back.draw();
	}

}
