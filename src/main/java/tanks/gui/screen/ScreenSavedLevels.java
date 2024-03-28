package tanks.gui.screen;

import tanks.Drawing;
import tanks.Function;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.gui.SearchBox;
import tanks.gui.screen.leveleditor.OverlayEditorMenu;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;

import java.util.ArrayList;
import java.util.Collections;

public class ScreenSavedLevels extends Screen
{
	public static int page = 0;
	public static boolean sortByTime = false;

	public SavedFilesList fullSavedLevelsList;
	public SavedFilesList savedLevelsList;

	Button quit = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenPlaySingleplayer());

	SearchBox search = new SearchBox(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
	{
		@Override
		public void run()
		{
			createNewLevelsList();
			savedLevelsList.filter(search.inputText);
			savedLevelsList.sortButtons();

			if (search.inputText.length() <= 0)
				savedLevelsList.page = page;
		}
	}, "");

	Button sort = new Button(this.centerX - this.objXSpace / 2 * 1.35, this.centerY - this.objYSpace * 4, this.objHeight, this.objHeight, "", new Runnable()
	{
		@Override
		public void run()
		{
			fullSavedLevelsList.sortedByTime = !fullSavedLevelsList.sortedByTime;
			fullSavedLevelsList.sort(fullSavedLevelsList.sortedByTime);
			createNewLevelsList();
			savedLevelsList.filter(search.inputText);
			savedLevelsList.sortButtons();

			if (fullSavedLevelsList.sortedByTime)
				sort.setHoverText("Sorting by last modified");
			else
				sort.setHoverText("Sorting by name");
		}
	}, "Sorting by name");

	Button newLevel = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "New level", () ->
	{
		String name = System.currentTimeMillis() + ".tanks";

		Level l = new Level("{28,18||0-0-player}");
		Game.screen = new ScreenLevelEditor(name, l);
		l.loadLevel((ILevelPreviewScreen) Game.screen);
	}
	);

	public ScreenSavedLevels()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		fullSavedLevelsList = new SavedFilesList(Game.homedir + Game.levelDir, page, 0, -30,
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
				(file) -> "Last modified---" + Game.timeInterval(file.lastModified(), System.currentTimeMillis()) + " ago");

		fullSavedLevelsList.sortedByTime = sortByTime;
		fullSavedLevelsList.sort(sortByTime);

		if (fullSavedLevelsList.sortedByTime)
			sort.setHoverText("Sorting by last modified");
		else
			sort.setHoverText("Sorting by name");

		savedLevelsList = fullSavedLevelsList.clone();
		createNewLevelsList();

		search.enableCaps = true;
	}

	public void createNewLevelsList()
	{
		savedLevelsList.buttons.clear();
		savedLevelsList.buttons.addAll(fullSavedLevelsList.buttons);
		savedLevelsList.sortButtons();
	}

	@Override
	public void update()
	{
		savedLevelsList.update();

		if (search.inputText.length() <= 0)
			page = savedLevelsList.page;

		quit.update();
		search.update();
		newLevel.update();

		this.sort.imageSizeX = 25;
		this.sort.imageSizeY = 25;
		this.sort.fullInfo = true;

		sortByTime = fullSavedLevelsList.sortedByTime;

		if (this.fullSavedLevelsList.sortedByTime)
			this.sort.image = "icons/sort_chronological.png";
		else
			this.sort.image = "icons/sort_alphabetical.png";

		this.sort.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		savedLevelsList.draw();

		if (savedLevelsList.buttons.size() <= 0)
		{
			Drawing.drawing.setColor(0, 0, 0);
			Drawing.drawing.setInterfaceFontSize(24);

			if (!search.inputText.isEmpty())
			{
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "No levels found");
			}
			else
			{
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 30, "You have no levels");
				Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + 30, "Create a level with the 'New level' button!");
			}
		}

		quit.draw();
		search.draw();
		newLevel.draw();

		this.sort.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, "My levels");
	}
}
