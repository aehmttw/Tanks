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

		this.music = "tomato_feast_2.ogg";
		this.musicID = "menu";
	}
	
	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenPlay();
		}
	}
	);
	
	Button create = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Create a party", new Runnable()
	{
		@Override
		public void run() 
		{
			ScreenPartyHost.chat.clear();
			ScreenPartyHost.isServer = false;
			ScreenPartyHost.includedPlayers.clear();
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.activeScreen = null;
			ScreenSharedLevels.page = 0;

			Game.players.clear();
			Game.players.add(Game.player);

			ScreenPartyHost.disconnectedPlayers.clear();

			Drawing.drawing.playSound("join.ogg");

			Game.screen = new ScreenPartyHost();
		}
	}
	);
	
	Button join = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Join a party", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenJoinParty();
		}
	}
	);
	
	TextBox port = new TextBox(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Port", new Runnable()
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
		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Create or join a party");

		Drawing.drawing.setInterfaceFontSize(this.textSize);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Make sure that everyone is using the same port!");
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2, "All players should be connected to the same");
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, " network, unless the host is port forwarding.");

		back.draw();
		port.draw();
		create.draw();
		join.draw();
	}

}
