package tanks;

import java.awt.Graphics;

public class ScreenResumeCrusade extends Screen
{
	
	Button resume = new Button(Drawing.interfaceSizeX / 2 + 190, Drawing.interfaceSizeY / 2 + 240, 350, 40, "Resume crusade", new Runnable()
	{
		@Override
		public void run() 
		{
			Crusade.crusadeMode = true;
			Crusade.currentCrusade.loadLevel();
			Game.screen = new ScreenGame();
		}
	}
			);

	Button selectOtherCrusade = new Button(Drawing.interfaceSizeX / 2 - 190, Drawing.interfaceSizeY / 2 + 240, 350, 40, "Start another crusade", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenCrusades();
		}
	}
			);
	
	Button quit = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
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
		resume.update();
		selectOtherCrusade.update();
		quit.update();
	}

	@Override
	public void draw(Graphics g) 
	{
		this.drawDefaultBackground(g);
		resume.draw(g);
		selectOtherCrusade.draw(g);
		quit.draw(g);
		Drawing drawing = Drawing.window;
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 150, "A crusade you have not yet finished was found");
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 90, "Would you like to continue playing that");
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 60, "crusade, or to start a new crusade?");
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2, "Progress in the current crusade will be lost");
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 30, "if you decide to start a new crusade!");
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 90, "Crusade: " + Crusade.currentCrusade.name);
		drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 120, "Remaining Lives: " + Crusade.currentCrusade.remainingLives);
	}

}
