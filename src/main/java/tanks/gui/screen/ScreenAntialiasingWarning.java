package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenAntialiasingWarning extends Screen
{
	public ScreenAntialiasingWarning()
	{
		this.music = "tomato_feast_1_options.ogg";
		this.musicID = "menu";
	}

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, 350, 40, "Ok", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenOptionsGraphics();
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

		String s = "disabled";

		if (Game.antialiasing)
			s = "enabled";

		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Notice!");

		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Antialiasing will be " + s + " the next time you start the game.");
	}

}
