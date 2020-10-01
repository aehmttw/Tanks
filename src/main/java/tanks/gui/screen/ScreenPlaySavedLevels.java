package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;

public class ScreenPlaySavedLevels extends Screen implements IPartyMenuScreen
{
	public static final String levelDir = Game.directoryPath + "/levels";

	public String title = "My levels";

	public SavedFilesList levels;

	public Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			if (ScreenPartyHost.isServer)
				Game.screen = ScreenPartyHost.activeScreen;
			else
				Game.screen = new ScreenPlaySingleplayer();
		}
	}
			);

	public ScreenPlaySavedLevels()
	{
		this.music = "tomato_feast_4.ogg";
		this.musicID = "menu";

		this.initializeLevels();
	}

	public void initializeLevels()
	{
		this.levels = new SavedFilesList(Game.homedir + Game.levelDir, ScreenSavedLevels.page, 0, -30,
				(name, file) ->
				{
					if (Game.loadLevel(file))
					{
						Game.screen = new ScreenGame();
						ScreenInterlevel.fromSavedLevels = true;
					}
				}, (file) -> null);
	}

	@Override
	public void update()
	{
		this.levels.update();
		this.quit.update();

		ScreenSavedLevels.page = this.levels.page;
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		this.levels.draw();
		this.quit.draw();

		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, this.title);
	}
}
