package tanks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class ScreenTitle extends Screen
{
	Button exit = new Button(350, 40, "Exit the game", new Runnable()
	{
		@Override
		public void run() 
		{
			System.exit(0);
		}
	}
			);
	
	Button options = new Button(350, 40, "Options...", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenOptions();
		}
	}
			);
	
	Button newLevel = new Button(350, 40, "Generate a new level", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.reset();
			Game.screen = new ScreenGame();
		}
	}
			);
	
	@Override
	public void update()
	{
		newLevel.update(Window.sizeX / 2, Window.sizeY / 2);
		exit.update(Window.sizeX / 2, Window.sizeY / 2 + 120);
		options.update(Window.sizeX / 2, Window.sizeY / 2 + 60);		
	}

	@Override
	public void draw(Graphics g)
	{
		this.drawDefaultBackground(g);
		
		g.setColor(Color.black);
		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (60 * Window.scale)));
		Window.drawText(g, Window.sizeX / 2, Window.sizeY / 2 - 200, "Tanks");
		newLevel.draw(g, Window.sizeX / 2, Window.sizeY / 2);
		exit.draw(g, Window.sizeX / 2, Window.sizeY / 2 + 120);
		options.draw(g, Window.sizeX / 2, Window.sizeY / 2 + 60);		
	}
	
}
