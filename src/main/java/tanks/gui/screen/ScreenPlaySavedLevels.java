package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;

public class ScreenPlaySavedLevels extends Screen
{
	public String title = "My levels";

	public SavedFilesList levels;

	public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
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
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		this.initializeLevels();
	}

	public void initializeLevels()
	{
		this.levels = new SavedFilesList(Game.homedir + Game.levelDir, ScreenSavedLevels.page,
				(int) (this.centerX - Drawing.drawing.interfaceSizeX / 2), (int) (-30 + this.centerY - Drawing.drawing.interfaceSizeY / 2),
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

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, this.title);
	}
	
	@Override
	public void setupLayoutParameters()
	{
		if (Drawing.drawing.interfaceScaleZoom > 1 && ScreenPartyHost.isServer)
			this.centerY -= this.objYSpace / 2;
	}
}
