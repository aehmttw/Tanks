package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenCrusades extends Screen
{
	public static final String crusadeDir = Game.directoryPath + "/crusades";

	int rows = 6;
	int yoffset = -150;
	int page = 0;

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenPlaySingleplayer();
		}
	}
	);

	Button quit2 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenPlaySingleplayer();
		}
	}
	);


	Button create = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Create crusade", new Runnable()
	{
		@Override
		public void run()
		{
			String name = System.currentTimeMillis() + "";
			BaseFile f = Game.game.fileManager.getFile(Game.homedir + crusadeDir + "/" + name + ".tanks");

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

	Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Next page", new Runnable()
	{
		@Override
		public void run()
		{
			page++;
		}
	}
	);

	Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Previous page", new Runnable()
	{
		@Override
		public void run()
		{
			page--;
		}
	}
	);

	ArrayList<Button> buttons = new ArrayList<Button>();


	public ScreenCrusades()
	{
		this.music = "tomato_feast_4.ogg";
		this.musicID = "menu";

		BaseFile crusadeDirFile = Game.game.fileManager.getFile(Game.homedir + crusadeDir);
		if (!crusadeDirFile.exists())
		{
			crusadeDirFile.mkdirs();
		}

		ArrayList<String> levels = new ArrayList<String>();

		try
		{
			ArrayList<String> ds = crusadeDirFile.getSubfiles();

			for (String p : ds)
			{
				if (p.endsWith(".tanks"))
					levels.add(p);
			}
		}
		catch (IOException e)
		{
			Game.exitToCrash(e);
		}

		buttons.add(new Button(0, 0, 350, 40, "Classic crusade", new Runnable()
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

		buttons.add(new Button(0, 0, 350, 40, "Wii crusade", new Runnable()
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


		for (String l: levels)
		{
			String[] pathSections = l.toString().replace("\\", "/").split("/");

			String name = pathSections[pathSections.length - 1].split("\\.")[0];
			buttons.add(new Button(0, 0, 350, 40, name.replace("_", " "), new Runnable()
			{
				@Override
				public void run()
				{
					Crusade c = findExistingCrusadeProgress(name);

					if (c == null)
						c = new Crusade(Game.game.fileManager.getFile(l), pathSections[pathSections.length - 1].split("\\.")[0]);

					Game.screen = new ScreenCrusadeDetails(c);
				}
			}
			));

		}

		for (int i = 0; i < buttons.size(); i++)
		{
			int page = i / (rows * 3);
			int offset = 0;

			if (page * rows * 3 + rows < buttons.size())
				offset = -190;

			if (page * rows * 3 + rows * 2 < buttons.size())
				offset = -380;

			buttons.get(i).posY = Drawing.drawing.interfaceSizeY / 2 + yoffset + (i % rows) * 60;

			if (i / rows % 3 == 0)
				buttons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset;
			else if (i / rows % 3 == 1)
				buttons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380;
			else
				buttons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380 * 2;
		}
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
		for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, buttons.size()); i++)
		{
			buttons.get(i).update();
		}

		if (ScreenPartyHost.isServer)
			quit2.update();
		else
		{
			quit.update();
			create.update();
		}

		if (page > 0)
			previous.update();

		if (buttons.size() > (1 + page) * rows * 3)
			next.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, buttons.size()); i++)
		{
			buttons.get(i).draw();
		}

		if (ScreenPartyHost.isServer)
			quit2.draw();
		else
		{
			quit.draw();
			create.draw();
		}


		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Crusades");

		if (page > 0)
			previous.draw();

		if (buttons.size() > (1 + page) * rows * 3)
			next.draw();

	}

}
