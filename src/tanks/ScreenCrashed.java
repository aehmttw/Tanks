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
		Drawing drawing = Drawing.window;
		drawing.fillInterfaceRect(g, Drawing.sizeX / 2, Drawing.sizeY / 2, Drawing.sizeX * 1.2, Drawing.sizeY * 1.2);				

		g.setColor(Color.white);
		Drawing.setInterfaceFontSize(g, 100);
		drawing.drawInterfaceText(g, 100, 100, ":(");

		Drawing.setInterfaceFontSize(g, 48);
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 100, "Oh noes! Tanks ran into a problem!");

		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (24 * Drawing.window.getScale())));
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 200, Game.crashMessage);
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 290, "Check the log file for more information: ");
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 320, Game.homedir.replace("\\", "/") + Game.logPath);

		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 420, "You may return to the game if you wish,");
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 450, "but be warned that things may become unstable.");
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 480, "If you see this screen again, restart the game.");
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, 510, "Also, you may want to report this crash!");

		this.quit.draw(g);
		this.exit.draw(g);

		return;
	}

}
