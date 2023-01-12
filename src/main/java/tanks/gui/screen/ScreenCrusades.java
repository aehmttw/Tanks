package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.gui.SearchBox;
import tanks.translation.Translation;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenCrusades extends Screen
{
	public static int page = 0;
	public static boolean sortByTime = false;

	public SavedFilesList fullCrusadesList;
	public SavedFilesList crusadesList;

	SearchBox search = new SearchBox(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
	{
		@Override
		public void run()
		{
			createNewCrusadesList();
			crusadesList.filter(search.inputText);
			crusadesList.sortButtons();
		}
	}, "");

	Button sort = new Button(this.centerX - this.objXSpace / 2 * 1.35, this.centerY - this.objYSpace * 4, this.objHeight, this.objHeight, "", new Runnable()
	{
		@Override
		public void run()
		{
			fullCrusadesList.sortedByTime = !fullCrusadesList.sortedByTime;
			fullCrusadesList.sort(fullCrusadesList.sortedByTime);
			createNewCrusadesList();
			crusadesList.filter(search.inputText);
			crusadesList.sortButtons();

			if (fullCrusadesList.sortedByTime)
				sort.setHoverText("Sorting by last modified");
			else
				sort.setHoverText("Sorting by name");
		}
	}, "Sorting by name");

	Button quit = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenPlaySingleplayer()
	);

	Button quit2 = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenPlaySingleplayer()
	);

	Button create = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Create crusade", () ->
	{
		String name = System.currentTimeMillis() + "";
		BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.crusadeDir + "/" + name + ".tanks");

		try
		{
			f.create();
			f.startWriting();
			f.println("properties");
			f.println("items");
			f.println("levels");
			f.stopWriting();

			Crusade c = new Crusade(f, name);
			Game.screen = new ScreenCrusadeDetails(c);
		}
		catch (IOException e)
		{
			Game.exitToCrash(e);
		}
	}
	);

	public ScreenCrusades()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		search.enableCaps = true;

		fullCrusadesList = new SavedFilesList(Game.homedir + Game.crusadeDir, page,
				(int) (this.centerX - Drawing.drawing.interfaceSizeX / 2), (int) (-30 + this.centerY - Drawing.drawing.interfaceSizeY / 2),
				(name, file) ->
				{
					Crusade c = findExistingCrusadeProgress(name);

					if (c == null)
						c = new Crusade(file, name.split("\\.")[0]);

					if (c.error == null)
						Game.screen = new ScreenCrusadeDetails(c);
					else
						Game.screen = new ScreenFailedToLoadCrusade(name, c.contents, c.error, Game.screen);
				},
				(file) -> "Last modified---" + Game.timeInterval(file.lastModified(), System.currentTimeMillis()) + " ago");


		addCrusade("adventure_crusade", "Meet all the enemies in---the main crusade of Tanks!");
		addCrusade("classic_crusade", "A retro crusade featuring---levels made long, long ago...");
		addCrusade("castle_crusade", "Invade, defend, and demolish---10 vast castles crawling with---some of the most difficult tanks!");
		addCrusade("beginner_crusade", "An easy crusade serving as---good practice for beginners!");

		fullCrusadesList.sortedByTime = sortByTime;
		fullCrusadesList.sort(sortByTime);
		crusadesList = fullCrusadesList.clone();

		if (fullCrusadesList.sortedByTime)
			sort.setHoverText("Sorting by last modified");
		else
			sort.setHoverText("Sorting by name");

		createNewCrusadesList();
	}

	public void addCrusade(String name, String desc)
	{
		fullCrusadesList.buttons.add(new Button(0, 0, fullCrusadesList.objWidth, fullCrusadesList.objHeight, Translation.translate(Game.formatString(name)), () ->
		{
			Crusade c = findExistingCrusadeProgress("internal/" + Game.formatString(name));

			if (c == null)
			{
				ArrayList<String> al = Game.game.fileManager.getInternalFileContents("/crusades/" + name +".tanks");
				c = new Crusade(al, Game.formatString(name), "/" + name + ".tanks");
			}

			c.description = desc;

			Game.screen = new ScreenCrusadeDetails(c);
		}
				, desc));
	}

	public void createNewCrusadesList()
	{
		crusadesList.buttons.clear();
		crusadesList.buttons.addAll(fullCrusadesList.buttons);
		crusadesList.sortButtons();
	}

	public Crusade findExistingCrusadeProgress(String name)
	{
		if (ScreenPartyHost.isServer)
			return null;

		BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.savedCrusadePath + name);

		if (f.exists())
			return Game.player.loadCrusade(f);
		else
			return null;
	}

	@Override
	public void update()
	{
		crusadesList.update();
		search.update();

		if (ScreenPartyHost.isServer)
			quit2.update();
		else
		{
			quit.update();
			create.update();
		}

		sortByTime = fullCrusadesList.sortedByTime;

		this.sort.imageSizeX = 25;
		this.sort.imageSizeY = 25;
		this.sort.fullInfo = true;

		if (this.fullCrusadesList.sortedByTime)
			this.sort.image = "icons/sort_chronological.png";
		else
			this.sort.image = "icons/sort_alphabetical.png";

		sort.update();

		page = crusadesList.page;
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		crusadesList.draw();

		if (ScreenPartyHost.isServer)
			quit2.draw();
		else
		{
			quit.draw();
			create.draw();
		}

		search.draw();

		if (crusadesList.buttons.size() <= 0)
		{
			Drawing.drawing.setColor(0, 0, 0);
			Drawing.drawing.setInterfaceFontSize(24);

			Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "No crusades found");
		}

		sort.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, "Crusades");
	}
}
