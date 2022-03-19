package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.gui.SearchBox;

public class ScreenShareCrusade extends Screen
{
	public SavedFilesList allCrusades;
	public SavedFilesList crusades;

	public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenShareSelect());

	SearchBox search = new SearchBox(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
	{
		@Override
		public void run()
		{
			createNewCrusadesList();
			crusades.filter(search.inputText);
			crusades.sortButtons();
		}
	}, "");

	Button sort = new Button(this.centerX - this.objXSpace / 2 * 1.35, this.centerY - this.objYSpace * 4, this.objHeight, this.objHeight, "", new Runnable()
	{
		@Override
		public void run()
		{
			allCrusades.sortedByTime = !allCrusades.sortedByTime;
			allCrusades.sort(allCrusades.sortedByTime);
			createNewCrusadesList();
			crusades.filter(search.inputText);
			crusades.sortButtons();

			if (allCrusades.sortedByTime)
				sort.setHoverText("Sorting by last modified");
			else
				sort.setHoverText("Sorting by name");
		}
	}, "Sorting by name");

	public ScreenShareCrusade()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		allCrusades = new SavedFilesList(Game.homedir + Game.crusadeDir, ScreenCrusades.page, 0, -60,
				(name, file) ->
				{
					Game.screen = new ScreenCrusadePreview(new Crusade(file, name), Game.screen, true);

				}, (file) -> "Last modified---" + Game.timeInterval(file.lastModified(), System.currentTimeMillis()) + " ago");

		crusades = allCrusades.clone();

		allCrusades.sortedByTime = ScreenCrusades.sortByTime;
		allCrusades.sort(ScreenCrusades.sortByTime);
		crusades = allCrusades.clone();

		if (allCrusades.sortedByTime)
			sort.setHoverText("Sorting by last modified");
		else
			sort.setHoverText("Sorting by name");


		this.createNewCrusadesList();
	}

	public void createNewCrusadesList()
	{
		crusades.buttons.clear();
		crusades.buttons.addAll(allCrusades.buttons);
		crusades.sortButtons();
	}

	@Override
	public void update()
	{
		crusades.update();
		search.update();
		quit.update();

		ScreenCrusades.page = crusades.page;

		ScreenCrusades.sortByTime = allCrusades.sortedByTime;

		this.sort.imageSizeX = 25;
		this.sort.imageSizeY = 25;
		this.sort.fullInfo = true;

		if (this.allCrusades.sortedByTime)
			this.sort.image = "sort_chronological.png";
		else
			this.sort.image = "sort_alphabetical.png";

		sort.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		crusades.draw();
		search.draw();
		quit.draw();

		if (crusades.buttons.size() <= 0)
		{
			Drawing.drawing.setColor(0, 0, 0);
			Drawing.drawing.setInterfaceFontSize(24);

			Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "No crusades found");
		}

		sort.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, "Share crusade");
	}

	@Override
	public void setupLayoutParameters()
	{
		this.centerY -= this.objYSpace / 2;
	}
}
