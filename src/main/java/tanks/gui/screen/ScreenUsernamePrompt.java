package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenUsernamePrompt extends Screen
{
	public ScreenUsernamePrompt()
	{
		this.music = "tomato_feast_1.ogg";
		this.musicID = "menu";
	}

	Button gotoOptions = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Multiplayer options", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenOptionsMultiplayer();
		}
	}
			);
	
	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenPlay();
		}
	}
			);
	
	@Override
	public void update() 
	{
		gotoOptions.update();
		quit.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		gotoOptions.draw();
		quit.draw();
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, "You must choose a username to play with others!");

		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, "Would you like to go to multiplayer options and choose one now?");
	}

}
