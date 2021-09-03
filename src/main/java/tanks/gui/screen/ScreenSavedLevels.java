package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.gui.screen.leveleditor.OverlayEditorMenu;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;

public class ScreenSavedLevels extends Screen
{
	public static int page = 0;

	public SavedFilesList savedFilesList;

	Button quit = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenPlaySingleplayer();
		}
	}
			);

	Button newLevel = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "New level", new Runnable()
	{
		@Override
		public void run() 
		{
			String name = System.currentTimeMillis() + ".tanks";

			Level l = new Level("{28,18||0-0-player}");
			Game.screen = new ScreenLevelEditor(name, l);
			l.loadLevel((ILevelPreviewScreen) Game.screen);
		}
	}
			);

	public ScreenSavedLevels()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		savedFilesList = new SavedFilesList(Game.homedir + Game.levelDir, page, 0, -30,
				(name, file) ->
				{
					ScreenLevelEditor s = new ScreenLevelEditor(name + ".tanks", null);

					if (Game.loadLevel(file, s))
					{
						s.level = Game.currentLevel;
						s.paused = true;
						Game.screen = new OverlayEditorMenu(s, s);
					}
				},
				(file) -> "Last opened---" + Game.timeInterval(file.lastModified(), System.currentTimeMillis()) + " ago");
	}

	@Override
	public void update()
	{		
		savedFilesList.update();
		page = savedFilesList.page;

		quit.update();
		newLevel.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		savedFilesList.draw();

		quit.draw();
		newLevel.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, "My levels");
	}
}
