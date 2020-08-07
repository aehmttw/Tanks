package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
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

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Ok", new Runnable()
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
		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.reason);

		back.draw();
	}

}
