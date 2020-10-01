package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;

public class ScreenShareLevel extends Screen implements IPartyMenuScreen
{
	public static final String levelDir = Game.directoryPath + "/levels";

	public SavedFilesList levels;

	public Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			if (ScreenPartyHost.isServer)
				Game.screen = ScreenPartyHost.activeScreen;
			else
				Game.screen = new ScreenPartyLobby();
		}
	}
			);

	public ScreenShareLevel()
	{
		this.music = "tomato_feast_4.ogg";
		this.musicID = "menu";

		levels = new SavedFilesList(Game.homedir + Game.levelDir, ScreenSavedLevels.page, 0, -30,
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

		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, "Share level");
	}
}
