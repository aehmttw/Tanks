package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;

public class ScreenShareLevel extends Screen
{
	public SavedFilesList levels;

	public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenShareSelect();
		}
	}
			);

	public ScreenShareLevel()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		levels = new SavedFilesList(Game.homedir + Game.levelDir, ScreenSavedLevels.page, 0, -60,
				(name, file) ->
				{
					ScreenPreviewShareLevel sc = new ScreenPreviewShareLevel(name, Game.screen);
					if (Game.loadLevel(file, sc))
					{
						sc.level = Game.currentLevel;
						Game.screen = sc;
					}
				}, (file) -> null);
	}

	@Override
	public void update()
	{
		levels.update();
		quit.update();

		ScreenSavedLevels.page = levels.page;
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		levels.draw();
		quit.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, "Share level");
	}

	@Override
	public void setupLayoutParameters()
	{
		this.centerY -= this.objYSpace / 2;
	}
}
