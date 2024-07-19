package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Game;
import tanks.gui.Button;

import java.util.Date;

public class ScreenTutorialGame extends ScreenGame
{
	public boolean active = true;

	public ScreenTutorialGame()
	{
		Game.startTime = 0;
		Game.playerTank.setBufferCooldown(50);
	}

	public Button skip = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Skip tutorial", () ->
	{
		try
		{
			BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.tutorialPath);

			f.create();
			f.startWriting();
			f.println("Fake certificate of completion:");
			f.println("Tanks: The Crusades tutorial");
			f.println("Skipped " + new Date().toString());
			f.stopWriting();
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}

		active = false;
		ScreenInterlevel.tutorialInitial = false;
		ScreenInterlevel.tutorial = false;
		Game.exitToTitle();
	});

	@Override
	public void setPerspective()
	{

	}

	@Override
	public void update()
	{
		if (paused)
			skip.update();

		if (active)
			super.update();
	}

	@Override
	public void draw()
	{
		super.draw();

		if (paused)
			skip.draw();
	}
}