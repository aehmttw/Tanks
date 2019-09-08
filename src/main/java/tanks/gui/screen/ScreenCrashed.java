package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenCrashed extends Screen
{
	public ScreenCrashed()
	{

	}

	Button exit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 100, 350, 40, "Exit the game", new Runnable()
	{
		@Override
		public void run() 
		{
			System.exit(0);
		}
	}
			);

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 160, 350, 40, "Return to title", new Runnable()
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
	public void draw()
	{
		Drawing drawing = Drawing.drawing;
		drawing.setColor(0, 0, 255);
		drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Drawing.drawing.interfaceSizeX * 1.2, Drawing.drawing.interfaceSizeY * 1.2);				

		drawing.setColor(255, 255, 255);
		drawing.setInterfaceFontSize(100);
		drawing.drawInterfaceText(100, 100, ":(");

		drawing.setInterfaceFontSize(48);
		drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, 100, "Oh noes! Tanks ran into a problem!");

		drawing.setInterfaceFontSize(24);
		drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, 200, Game.crashMessage);
		drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, 290, "Check the log file for more information: ");
		drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, 320, Game.homedir.replace("\\", "/") + Game.logPath);

		drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, 420, "You may return to the game if you wish,");
		drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, 450, "but be warned that things may become unstable.");
		drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, 480, "If you see this screen again, restart the game.");
		drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, 510, "Also, you may want to report this crash!");
		
		this.quit.draw();
		this.exit.draw();
	}

}
