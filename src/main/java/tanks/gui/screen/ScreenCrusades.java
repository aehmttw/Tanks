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

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenPlaySingleplayer();
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
				ArrayList<String> al = Game.game.fileManager.getInternalFileContents("/classic_crusade.tanks");

				Crusade.currentCrusade = new Crusade(al, "Classic Crusade", "/classic_crusade.tanks");
				Crusade.crusadeMode = true;
				Crusade.currentCrusade.begin();
				Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
			}
		}
		));

		buttons.add(new Button(0, 0, 350, 40, "Wii crusade", new Runnable()
		{
			@Override
			public void run()
			{
				ArrayList<String> al = Game.game.fileManager.getInternalFileContents("/wii_crusade.tanks");
				Crusade.currentCrusade = new Crusade(al, "Wii Crusade", "/wii_crusade.tanks");
				Crusade.crusadeMode = true;
				Crusade.currentCrusade.begin();
				Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
			}
		}
		));

		for (String l: levels)
		{
			String[] pathSections = l.toString().replaceAll("\\\\", "/").split("/");

			buttons.add(new Button(0, 0, 350, 40, pathSections[pathSections.length - 1].split("\\.")[0], new Runnable()
			{
				@Override
				public void run()
				{
					Crusade.currentCrusade = new Crusade(Game.game.fileManager.getFile(l), pathSections[pathSections.length - 1].split("\\.")[0]);
					Crusade.crusadeMode = true;
					Crusade.currentCrusade.begin();
					Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
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

	@Override
	public void update()
	{
		for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, buttons.size()); i++)
		{
			buttons.get(i).update();
		}

		quit.update();
		//newLevel.update();

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

		quit.draw();
		//newLevel.draw(g);

		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Crusades");

		if (page > 0)
			previous.draw();

		if (buttons.size() > (1 + page) * rows * 3)
			next.draw();

	}

}
