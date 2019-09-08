package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenMultiplayerOptions extends Screen
{
	TextBox username = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Username", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.username = username.inputText;
			username.inputText = Game.username + "";
			
			if (!Game.username.equals(Game.chatFilter.filterChat(Game.username)))
				Game.screen = new ScreenUsernameWarning();
		}
	},
			Game.username, "Pick a username that players---will see in multiplayer");
	
	Button chatFilter = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Chat filter: on", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.enableChatFilter = !Game.enableChatFilter;

			if (Game.enableChatFilter)
				chatFilter.text = "Chat filter: on";
			else
				chatFilter.text = "Chat filter: off";
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
	
	public ScreenMultiplayerOptions()
	{
		username.enableCaps = true;
		username.enableSpaces = false;
		
		if (Game.enableChatFilter)
			chatFilter.text = "Chat filter: on";
		else
			chatFilter.text = "Chat filter: off";
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
