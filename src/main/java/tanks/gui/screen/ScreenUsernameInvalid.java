package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenUsernameInvalid extends Screen
{

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, 350, 40, "Ok", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.username = "";
			Game.screen = new ScreenTitle();
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
		back.draw();
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "The username you picked is invalid!");

		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, "To prevent potential issues, it has been reset.");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 20, "Valid usernames are 1-18 characters long and");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 0, "contain capital or lowercase letters,");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, "numbers, and underscores.");

	}

}
