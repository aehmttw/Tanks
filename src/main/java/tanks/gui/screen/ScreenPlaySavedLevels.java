package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.gui.SearchBox;

public class ScreenPlaySavedLevels extends Screen
{
	public String title = "My levels";

	public SavedFilesList allLevels;
	public SavedFilesList levels;

	public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () ->
	{
		if (ScreenPartyHost.isServer)
			Game.screen = ScreenPartyHost.activeScreen;
		else
			Game.screen = new ScreenPlaySingleplayer();
	}
	);

	SearchBox search = new SearchBox(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
	{
		@Override
		public void run()
		{
			newLevelsList();
			levels.filter(search.inputText);
			levels.sortButtons();
		}
	}, "");

	Button sort = new Button(this.centerX - this.objXSpace / 2 * 1.35, this.centerY - this.objYSpace * 4, this.objHeight, this.objHeight, "", new Runnable()
	{
		@Override
		public void run()
		{
			allLevels.sortedByTime = !allLevels.sortedByTime;
			allLevels.sort(allLevels.sortedByTime);
			newLevelsList();
			levels.filter(search.inputText);
			levels.sortButtons();

			if (allLevels.sortedByTime)
				sort.setHoverText("Sorting by last modified");
			else
				sort.setHoverText("Sorting by name");
		}
	}, "Sorting by name");

	public ScreenPlaySavedLevels()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		search.enableCaps = true;

		this.initializeLevels();
	}

	public void initializeLevels()
	{
		this.allLevels = new SavedFilesList(Game.homedir + Game.levelDir, ScreenSavedLevels.page,
				(int) (this.centerX - Drawing.drawing.interfaceSizeX / 2), (int) (-30 + this.centerY - Drawing.drawing.interfaceSizeY / 2),
				(name, file) ->
				{
					if (Game.loadLevel(file))
					{
						Game.screen = new ScreenGame();
						ScreenInterlevel.fromSavedLevels = true;
					}
				}, (file) -> "Last modified---" + Game.timeInterval(file.lastModified(), System.currentTimeMillis()) + " ago");

		this.levels = allLevels.clone();

		allLevels.sortedByTime = ScreenSavedLevels.sortByTime;
		allLevels.sort(ScreenSavedLevels.sortByTime);

		if (allLevels.sortedByTime)
			sort.setHoverText("Sorting by last modified");
		else
			sort.setHoverText("Sorting by name");

		levels = allLevels.clone();
		newLevelsList();
	}

	public void newLevelsList()
	{
		levels.buttons.clear();
		levels.buttons.addAll(allLevels.buttons);
		levels.sortButtons();
	}

	@Override
	public void update()
	{
		this.levels.update();
		this.search.update();
		this.quit.update();

		ScreenSavedLevels.page = this.levels.page;

		this.sort.imageSizeX = 25;
		this.sort.imageSizeY = 25;
		this.sort.fullInfo = true;

		ScreenSavedLevels.sortByTime = allLevels.sortedByTime;

		if (this.allLevels.sortedByTime)
			this.sort.image = "icons/sort_chronological.png";
		else
			this.sort.image = "icons/sort_alphabetical.png";

		this.sort.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		this.levels.draw();
		this.search.draw();
		this.quit.draw();

		this.sort.draw();

		if (levels.buttons.size() <= 0)
		{
			Drawing.drawing.setColor(0, 0, 0);
			Drawing.drawing.setInterfaceFontSize(24);

			if (!search.inputText.isEmpty())
			{
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "No levels found");
			}
			else
			{
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "You have no levels");
			}
		}

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, this.title);
	}

	@Override
	public void setupLayoutParameters()
	{
		if (Drawing.drawing.interfaceScaleZoom > 1 && ScreenPartyHost.isServer)
			this.centerY -= this.objYSpace / 2;
	}
}
