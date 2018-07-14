package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class ScreenPaused extends ScreenGame
{
	
	Button resume = new Button(350, 40, "Continue playing", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenGame();
			Game.player.cooldown = 20;
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
	
	Button quit = new Button(350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.exitToTitle();
			//System.exit(0);
		}
	}
			);
	
	@Override
	public void update()
	{		
		if (KeyInputListener.keys.contains(KeyEvent.VK_ESCAPE))
		{
			if (!Panel.pausePressed)
				Game.screen = new ScreenGame();
			
			Panel.pausePressed = true;
		}
		else
			Panel.pausePressed = false;
		
		newLevel.update(Window.sizeX / 2, Window.sizeY / 2);
		quit.update(Window.sizeX / 2, Window.sizeY / 2 + 60);
		resume.update(Window.sizeX / 2, Window.sizeY / 2 - 60);
	}
	
	@Override
	public void draw(Graphics g)
	{
		super.draw(g);
		
		g.setColor(new Color(127, 178, 228, 64));
		g.fillRect(0, 0, (int) (Game.window.getSize().getWidth()) + 1, (int) (Game.window.getSize().getHeight()) + 1);
		newLevel.draw(g, Window.sizeX / 2, Window.sizeY / 2);
		quit.draw(g, Window.sizeX / 2, Window.sizeY / 2 + 60);
		resume.draw(g, Window.sizeX / 2, Window.sizeY / 2 - 60);
		g.setColor(Color.black);
		Window.drawText(g, Window.sizeX / 2, Window.sizeY / 2 - 150, "Game paused");
	}
}
