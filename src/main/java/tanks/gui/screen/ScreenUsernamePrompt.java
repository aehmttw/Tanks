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

	Button gotoOptions = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Multiplayer options", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenOptionsMultiplayer();
		}
	}
			);
	
	Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Back", new Runnable()
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

		Drawing.drawing.setInterfaceFontSize(this.textSize);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "You must choose a username to play with others!");

		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 0.5, "Would you like to go to multiplayer options and choose one now?");
	}

}
