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

		this.music = "menu_2.ogg";
		this.musicID = "menu";
	}
	
	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenPlay()
	);
	
	Button create = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Create a party", () ->
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
	);
	
	Button join = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Join a party", () -> Game.screen = new ScreenJoinParty()
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
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Create or join a party");

		Drawing.drawing.setInterfaceFontSize(this.textSize);

		double offset = 0;
		if (Game.steamNetworkHandler.initialized)
		{
			offset = 0.25;
			Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * (0.75 + offset), "Joining via Steam peer-to-peer doesn't require port forwarding.");
		}

		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * (2.5 + offset), "Make sure that everyone is using the same port!");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * (2 + offset), "All players should be connected to the same");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * (1.5 + offset), "network, unless the host is port forwarding.");

		back.draw();
		port.draw();
		create.draw();
		join.draw();
	}

}
