package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenTitle extends Screen
{	
	Button exit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 180, 350, 40, "Exit the game", new Runnable()
	{
		@Override
		public void run() 
		{
			System.exit(0);
		}
	}
			);
	
	Button options = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Options", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenOptions();
		}
	}
			);
	
	Button create = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, 350, 40, "Create levels", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenSavedLevels();
		}
	}
			);
	
	
	Button play = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Play!", new Runnable()
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
		play.update();
		exit.update();
		create.update();
		options.update();	
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(60);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 200, "Tanks");
		play.draw();
		exit.draw();
		create.draw();
		options.draw();		
	}
}
