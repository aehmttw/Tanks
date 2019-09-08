package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenParty extends Screen
{
	public ScreenParty()
	{
		Game.eventsOut.clear();
		port.allowSpaces = false;
		port.enableSpaces = false;
		port.allowLetters = false;
		port.checkMaxValue = true;
		port.checkMinValue = true;
		port.maxChars = 5;
		port.maxValue = 65535;
		port.minValue = 0;
	}
	
	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 210, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenPlay();
		}
	}
	);
	
	Button create = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Create a party", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenPartyHost();
		}
	}
	);
	
	Button join = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Join a party", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenJoinParty();
		}
	}
	);
	
	TextBox port = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, 350, 40, "Port", new Runnable()
	{
		@Override
		public void run() 
		{
			try
			{
				Game.port = Integer.parseInt(port.inputText);
				ScreenOptions.saveOptions(Game.homedir);
			}
			catch (Exception ignored) { }
			port.inputText = Game.port + "";
		}
	},
			Game.port + "", "Sets port for multiplayer------Make sure all players are using---the same port");

	@Override
	public void update() 
	{
		back.update();
		create.update();
		join.update();
		port.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 210, "Create or join a party");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 150, "Make sure that everyone is using the same port!");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 120, "All players should be connected to the same");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 90, " network, unless the host is port forwarding.");

		back.draw();
		port.draw();
		create.draw();
		join.draw();
	}

}
