package tanks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class ScreenCrashed extends Screen
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
	
	Button quit = new Button(350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.exitToTitle();
		}
	}
			);
	
	@Override
	public void update()
	{
		this.quit.update(Window.sizeX / 2, Window.sizeY - 160);
		this.exit.update(Window.sizeX / 2, Window.sizeY - 100);
	}

	@Override
	public void draw(Graphics g)
	{
		g.setColor(Color.blue);
		Window.fillRect(g, Window.sizeX / 2, Window.sizeY / 2, Window.sizeX * 1.2, Window.sizeY * 1.2);				

		g.setColor(Color.white);
		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (100 * Window.scale)));
		Window.drawText(g, 100, 100, ":(");

		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (48 * Window.scale)));
		Window.drawText(g, Window.sizeX / 2, 100, "Oh noes! Tanks ran into a problem!");

		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (24 * Window.scale)));
		Window.drawText(g, Window.sizeX / 2, 200, Game.crashMessage);
		Window.drawText(g, Window.sizeX / 2, 280, "Check the log file for more information: ");
		Window.drawText(g, Window.sizeX / 2, 320, Game.homedir.replace("\\", "/") + Game.logPath);

		Window.drawText(g, Window.sizeX / 2, 400, "You may return to the game if you wish,");
		Window.drawText(g, Window.sizeX / 2, 440, "but be warned that things may become unstable.");
		Window.drawText(g, Window.sizeX / 2, 480, "If you see this screen again, restart the game.");
		Window.drawText(g, Window.sizeX / 2, 520, "Also, you may want to report this crash!");

		this.quit.draw(g, Window.sizeX / 2, Window.sizeY - 160);
		this.exit.draw(g, Window.sizeX / 2, Window.sizeY - 100);

		return;
	}

}
