package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.gui.SearchBoxInstant;
import tanks.gui.TextBox;

public class ScreenShareLevel extends Screen
{
	public SavedFilesList allLevels;
	public SavedFilesList levels;
	public Screen previous;

	public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> Game.screen = previous);

	SearchBoxInstant search = new SearchBoxInstant(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
	{
		@Override
		public void run()
		{
			createNewLevelsList();
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
			createNewLevelsList();
			levels.filter(search.inputText);
			levels.sortButtons();

			if (allLevels.sortedByTime)
				sort.setHoverText("Sorting by last modified");
			else
				sort.setHoverText("Sorting by name");
		}
	}, "Sorting by name");

	public ScreenShareLevel()
	{
		super(350, 40, 380, 60);

		this.previous = Game.screen;
		this.music = "menu_4.ogg";
		this.musicID = "menu";

		boolean party = ScreenPartyLobby.isClient || ScreenPartyHost.isServer;

		allLevels = new SavedFilesList(Game.homedir + Game.levelDir, ScreenSavedLevels.page, 0, party ? -60 : -30,
				(name, file) ->
				{
					ScreenPreviewShareLevel sc = new ScreenPreviewShareLevel(name, Game.screen);
					if (Game.loadLevel(file, sc))
					{
						sc.level = Game.currentLevel;
						Game.screen = sc;
					}
				}, (file) -> "Last modified---" + Game.timeInterval(file.lastModified(), System.currentTimeMillis()) + " ago");

		this.allLevels.drawOpenFileButton = true;
		levels = allLevels.clone();
		allLevels.sortedByTime = ScreenSavedLevels.sortByTime;
		allLevels.sort(ScreenSavedLevels.sortByTime);

		if (allLevels.sortedByTime)
			sort.setHoverText("Sorting by last modified");
		else
			sort.setHoverText("Sorting by name");

		levels = allLevels.clone();
		createNewLevelsList();
	}

	public void createNewLevelsList()
	{
		levels.buttons.clear();
		levels.buttons.addAll(allLevels.buttons);
		levels.sortButtons();
	}

	@Override
	public void update()
	{
		levels.update();
		search.update();
		quit.update();

		ScreenSavedLevels.page = levels.page;

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

		levels.draw();
		search.draw();
		quit.draw();

		if (levels.buttons.size() <= 0)
		{
			Drawing.drawing.setColor(0, 0, 0);
			Drawing.drawing.setInterfaceFontSize(24);

			Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "No levels found");
		}

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, "Share level");

		this.sort.draw();
	}

	@Override
	public void setupLayoutParameters()
	{
		boolean party = ScreenPartyLobby.isClient || ScreenPartyHost.isServer;
		if (party)
			this.centerY -= this.objYSpace / 2;
	}
}
