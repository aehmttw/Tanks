package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenPlay extends Screen
{
	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 180, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenTitle();
		}
	}
	);

	Button singleplayer = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, this.objWidth, this.objHeight, "Singleplayer", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenPlaySingleplayer();
		}
	}
	, "Play random levels, crusades,---the tutorial, or make your own levels!");

	Button multiplayer = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, this.objWidth, this.objHeight, "Multiplayer", new Runnable()
	{
		@Override
		public void run()
		{
			if (!Game.player.username.equals(""))
				Game.screen = new ScreenPlayMultiplayer();
			else
				Game.screen = new ScreenUsernamePrompt();
		}
	}
	, "Play in a party with others---who are on your local---network or port forwarding---or play on the online server!");

	public ScreenPlay()
	{
		this.music = "tomato_feast_2.ogg";
		this.musicID = "menu";
	}

	@Override
	public void update() 
	{
		singleplayer.update();
		multiplayer.update();
		back.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 180, "Select a game mode");
		back.draw();
		multiplayer.draw();
		singleplayer.draw();
	}

}
