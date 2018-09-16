package tanks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class ScreenCrashed extends Screen
{
	Button exit = new Button(Window.interfaceSizeX / 2, Window.interfaceSizeY - 100, 350, 40, "Exit the game", new Runnable()
	{
		@Override
		public void run() 
		{
			System.exit(0);
		}
	}
			);
	
	Button quit = new Button(Window.interfaceSizeX / 2, Window.interfaceSizeY - 160, 350, 40, "Quit to title", new Runnable()
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
		this.quit.update();
		this.exit.update();
	}

	@Override
	public void draw(Graphics g)
	{
		g.setColor(Color.blue);
		Window.fillInterfaceRect(g, Window.sizeX / 2, Window.sizeY / 2, Window.sizeX * 1.2, Window.sizeY * 1.2);				

		g.setColor(Color.white);
		Window.setInterfaceFontSize(g, 100);
		Window.drawInterfaceText(g, 100, 100, ":(");

		Window.setInterfaceFontSize(g, 48);
		Window.drawInterfaceText(g, Window.sizeX / 2, 100, "Oh noes! Tanks ran into a problem!");

		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (24 * Window.scale)));
		Window.drawInterfaceText(g, Window.sizeX / 2, 200, Game.crashMessage);
		Window.drawInterfaceText(g, Window.sizeX / 2, 280, "Check the log file for more information: ");
		Window.drawInterfaceText(g, Window.sizeX / 2, 320, Game.homedir.replace("\\", "/") + Game.logPath);

		Window.drawInterfaceText(g, Window.sizeX / 2, 400, "You may return to the game if you wish,");
		Window.drawInterfaceText(g, Window.sizeX / 2, 440, "but be warned that things may become unstable.");
		Window.drawInterfaceText(g, Window.sizeX / 2, 480, "If you see this screen again, restart the game.");
		Window.drawInterfaceText(g, Window.sizeX / 2, 520, "Also, you may want to report this crash!");

		this.quit.draw(g);
		this.exit.draw(g);

		return;
	}

}
