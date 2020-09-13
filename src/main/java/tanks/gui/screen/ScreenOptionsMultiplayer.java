package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.tank.TankPlayerRemote;

public class ScreenOptionsMultiplayer extends Screen
{
	public static final String chatFilterText = "Chat filter: ";
	public static final String anticheatText = "Anticheat: ";

	public static final String weakText = "\u00A7200100000255weak";
	public static final String strongText = "\u00A7000200000255strong";

	TextBox username = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, 350, 40, "Username", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.player.username = username.inputText;
			username.inputText = Game.player.username + "";
			
			if (!Game.player.username.equals(Game.chatFilter.filterChat(Game.player.username)))
				Game.screen = new ScreenUsernameWarning();
		}
	},
			Game.player.username, "Pick a username that players---will see in multiplayer");

	Button chatFilter = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.enableChatFilter = !Game.enableChatFilter;

			if (Game.enableChatFilter)
				chatFilter.text = chatFilterText + ScreenOptions.onText;
			else
				chatFilter.text = chatFilterText + ScreenOptions.offText;
		}
	},
			"Filters chat of potentially---inappropriate words");

	Button anticheat = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "", new Runnable()
	{
		@Override
		public void run()
		{
			if (!TankPlayerRemote.checkMotion)
			{
				TankPlayerRemote.checkMotion = true;
				TankPlayerRemote.weakTimeCheck = false;
				TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatStrongTimeOffset;
			}
			else if (!TankPlayerRemote.weakTimeCheck)
			{
				TankPlayerRemote.weakTimeCheck = true;
				TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatWeakTimeOffset;
			}
			else
				TankPlayerRemote.checkMotion = false;

			if (!TankPlayerRemote.checkMotion)
				anticheat.text = anticheatText + ScreenOptions.offText;
			else if (!TankPlayerRemote.weakTimeCheck)
				anticheat.text = anticheatText + strongText;
			else
				anticheat.text = anticheatText + weakText;
		}
	},
			"When this option is enabled---while hosting a party,---other players' positions and---velocities will be checked---and corrected if invalid.------Weaker settings work better---with less stable connections.");

	Button color = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Tank color", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenOptionsMultiplayerColor();
		}
	},
			"Personalize your tank---to stand out in multiplayer!");


	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenOptions();
		}
	}
			);
	
	public ScreenOptionsMultiplayer()
	{
		this.music = "tomato_feast_1_options.ogg";
		this.musicID = "menu";

		username.enableCaps = true;
		username.enableSpaces = false;

		if (Game.enableChatFilter)
			chatFilter.text = chatFilterText + ScreenOptions.onText;
		else
			chatFilter.text = chatFilterText + ScreenOptions.offText;

		if (!TankPlayerRemote.checkMotion)
			anticheat.text = anticheatText + ScreenOptions.offText;
		else if (!TankPlayerRemote.weakTimeCheck)
			anticheat.text = anticheatText + strongText;
		else
			anticheat.text = anticheatText + weakText;
	}
	
	@Override
	public void update() 
	{
		chatFilter.update();
		back.update();
		username.update();
		color.update();
		anticheat.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		back.draw();
		anticheat.draw();
		chatFilter.draw();
		color.draw();
		username.draw();

		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Multiplayer options");
	}

}
