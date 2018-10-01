package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class ScreenTitle extends Screen
{
	Button exit = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 180, 350, 40, "Exit the game", new Runnable()
	{
		@Override
		public void run() 
		{
			System.exit(0);
		}
	}
			);
	
	Button build = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 120, 350, 40, "My levels", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenSavedLevels();
		}
	}
			);
	
	Button options = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 60, 350, 40, "Options", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenOptions();
		}
	}
			);
	
	/*Button newLevel = new Button(Window.interfaceSizeX / 2, Window.interfaceSizeY / 2, 350, 40, "Generate a new level", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.reset();
			Game.screen = new ScreenGame();
		}
	}
			);*/
	
	Button play = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2, 350, 40, "Play!", new Runnable()
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
		options.update();	
		build.update();		
	}

	@Override
	public void draw(Graphics g)
	{
		this.drawDefaultBackground(g);
		
		g.setColor(Color.black);
		Drawing.setInterfaceFontSize(g, 60);
		Drawing.drawInterfaceText(g, Drawing.sizeX / 2, Drawing.sizeY / 2 - 200, "Tanks");
		play.draw(g);
		exit.draw(g);
		options.draw(g);		
		build.draw(g);		
	}
	
}
