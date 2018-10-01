package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class ScreenPlay extends Screen
{

	Button newLevel = new Button(Window.interfaceSizeX / 2, Window.interfaceSizeY / 2 - 90, 350, 40, "Random level", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.reset();
			Game.screen = new ScreenGame();
		}
	}
			, "Generate a random level to play");
	
	Button crusade = new Button(Window.interfaceSizeX / 2, Window.interfaceSizeY / 2 - 30, 350, 40, "Crusades", new Runnable()
	{
		@Override
		public void run() 
		{
			if (Crusade.currentCrusade == null)
				Game.screen = new ScreenCrusades();
			else
				Game.screen = new ScreenResumeCrusade();

		}
	}
			, "Fight battles in an order,---and see how long you can survive!");
	
	Button online = new Button(Window.interfaceSizeX / 2, Window.interfaceSizeY / 2 + 30, 350, 40, "Online", "Online mode is coming soon!");
	
	Button party = new Button(Window.interfaceSizeX / 2, Window.interfaceSizeY / 2 + 90, 350, 40, "Party", "Party mode is coming soon!");
	
	Button back = new Button(Window.interfaceSizeX / 2, Window.interfaceSizeY / 2 + 210, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenTitle();
		}
	}
	);
	
	@Override
	public void update() 
	{
		newLevel.update();
		crusade.update();
		online.update();
		party.update();
		back.update();
	}

	@Override
	public void draw(Graphics g) 
	{
		this.drawDefaultBackground(g);
		Window.setFontSize(g, 24);
		g.setColor(Color.black);
		Window.drawInterfaceText(g, Window.interfaceSizeX / 2, Window.interfaceSizeY / 2 - 150, "Select a game mode");
		back.draw(g);
		party.draw(g);
		online.draw(g);
		crusade.draw(g);
		newLevel.draw(g);

	}

}
