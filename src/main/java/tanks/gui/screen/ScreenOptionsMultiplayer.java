package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenOptionsMultiplayer extends Screen
{
	public static final String chatFilterText = "Chat filter: ";

	TextBox username = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Username", new Runnable()
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
		username.enableCaps = true;
		username.enableSpaces = false;

		if (Game.enableChatFilter)
			chatFilter.text = chatFilterText + ScreenOptions.onText;
		else
			chatFilter.text = chatFilterText + ScreenOptions.offText;
	}
	
	@Override
	public void update() 
	{
		chatFilter.update();
		back.update();
		username.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		back.draw();
		chatFilter.draw();
		username.draw();

		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 210, "Multiplayer options");
	}

}
