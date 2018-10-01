package tanks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class ScreenCrashed extends Screen
{
	Button exit = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY - 100, 350, 40, "Exit the game", new Runnable()
	{
		@Override
		public void run() 
		{
			System.exit(0);
		}
	}
			);
	
	Button quit = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY - 160, 350, 40, "Return to title", new Runnable()
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
		Drawing.fillInterfaceRect(g, Drawing.sizeX / 2, Drawing.sizeY / 2, Drawing.sizeX * 1.2, Drawing.sizeY * 1.2);				

		g.setColor(Color.white);
		Drawing.setInterfaceFontSize(g, 100);
		Drawing.drawInterfaceText(g, 100, 100, ":(");

		Drawing.setInterfaceFontSize(g, 48);
		Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 100, "Oh noes! Tanks ran into a problem!");

		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (24 * Drawing.scale)));
		Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 200, Game.crashMessage);
		Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 280, "Check the log file for more information: ");
		Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 320, Game.homedir.replace("\\", "/") + Game.logPath);

		Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 400, "You may return to the game if you wish,");
		Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 440, "but be warned that things may become unstable.");
		Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 480, "If you see this screen again, restart the game.");
		Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 520, "Also, you may want to report this crash!");

		this.quit.draw(g);
		this.exit.draw(g);

		return;
	}

}
