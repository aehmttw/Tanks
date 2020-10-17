package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenCrusades extends Screen
{
	public static int page = 0;

	public SavedFilesList crusadesList;

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenPlaySingleplayer();
		}
	}
	);

	Button quit2 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenPlaySingleplayer();
		}
	}
	);

	Button create = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Create crusade", new Runnable()
	{
		@Override
		public void run()
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
	}
	);

	public ScreenCrusades()
	{
		this.music = "tomato_feast_4.ogg";
		this.musicID = "menu";

		crusadesList = new SavedFilesList(Game.homedir + Game.crusadeDir, page, 0, -30, (name, file) ->
		{
			Crusade c = findExistingCrusadeProgress(name);

			if (c == null)
				c = new Crusade(file, name.split("\\.")[0]);

			if (c.error == null)
				Game.screen = new ScreenCrusadeDetails(c);
			else
				Game.screen = new ScreenFailedToLoadCrusade(name, c.contents, c.error, Game.screen);
		},
				(name) -> null);


		crusadesList.buttons.add(0, new Button(0, 0, crusadesList.objWidth, crusadesList.objHeight, "Classic crusade", new Runnable()
		{
			@Override
			public void run()
			{
				Crusade c = findExistingCrusadeProgress("internal/Classic crusade");

				if (c == null)
				{
					ArrayList<String> al = Game.game.fileManager.getInternalFileContents("/crusades/classic_crusade.tanks");
					c = new Crusade(al, "Classic crusade", "/classic_crusade.tanks");
				}

				Game.screen = new ScreenCrusadeDetails(c);
			}
		}
		));

		crusadesList.buttons.add(1, new Button(0, 0, crusadesList.objWidth, crusadesList.objHeight, "Wii crusade", new Runnable()
		{
			@Override
			public void run()
			{
				Crusade c = findExistingCrusadeProgress("internal/Wii crusade");

				if (c == null)
				{
					ArrayList<String> al = Game.game.fileManager.getInternalFileContents("/crusades/wii_crusade.tanks");
					c = new Crusade(al, "Wii crusade", "/wii_crusade.tanks");
				}

				Game.screen = new ScreenCrusadeDetails(c);
			}
		}
		));

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

		if (ScreenPartyHost.isServer)
			quit2.update();
		else
		{
			quit.update();
			create.update();
		}

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

		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, "Crusades");
	}
}
