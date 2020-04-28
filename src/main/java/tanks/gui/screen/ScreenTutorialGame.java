package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenTutorialGame extends ScreenGame
{
	public ScreenTutorialGame()
	{
		Game.startTime = 100;
	}
	
	public Button skip = new Button(Drawing.drawing.interfaceSizeX-200, Drawing.drawing.interfaceSizeY-50, 350, 40, "Skip Tutorial", new Runnable()
	{
		@Override
		public void run() 
		{
			try 
			{
				Game.game.fileManager.getFile(Game.homedir + Game.tutorialPath).create();
			} 
			catch (Exception e)
			{
				Game.exitToCrash(e);
			}

			ScreenInterlevel.tutorialInitial = false;
			ScreenInterlevel.tutorial = false;
			Game.exitToTitle();
		}
	});

	@Override
	public void setPerspective()
	{

	}

	@Override
	public void update() 
	{
		super.update();

		if (!paused)
			skip.update();
	}

	@Override
	public void draw() 
	{
		super.draw();

		if (!paused)
			skip.draw();
	}
}