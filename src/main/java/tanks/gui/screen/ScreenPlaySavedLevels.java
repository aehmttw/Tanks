package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ChatMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class ScreenPlaySavedLevels extends Screen implements IPartyMenuScreen
{
	public static final String levelDir = Game.directoryPath + "/levels";

	int rows = 6;
	int yoffset = -150;
	static int page = 0;

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			if (ScreenPartyHost.isServer)
				Game.screen = ScreenPartyHost.activeScreen;
			else
				Game.screen = new ScreenPlay();
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

	public ScreenPlaySavedLevels()
	{
		if (!Files.exists(Paths.get(Game.homedir + levelDir)))
		{
			new File(Game.homedir + levelDir).mkdir();
		}

		ArrayList<Path> levels = new ArrayList<Path>();

		try
		{
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(Game.homedir + levelDir));

			for (Path p : ds) {
				if (p.toString().endsWith(".tanks"))
					levels.add(p);
			}

			ds.close();
		}
		catch (IOException e)
		{
			Game.exitToCrash(e);
		}

		for (Path l: levels)
		{
			String[] pathSections = l.toString().replaceAll("\\\\", "/").split("/");

			buttons.add(new Button(0, 0, 350, 40, pathSections[pathSections.length - 1].split("\\.")[0], new Runnable()
			{
				@Override
				public void run() 
				{
					try
					{
						Game.loadLevel(l.toFile());
						Game.screen = new ScreenGame();
						ScreenInterlevel.fromSavedLevels = true;
					}
					catch (Exception e)
					{
						Game.logger.println(new Date().toString() + " (syserr) failed to load level " + l.toString());
						e.printStackTrace();
						e.printStackTrace(Game.logger);
						Game.screen = new ScreenFailedToLoadLevel(l.toString(), Game.screen);
					}
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

		if (page > 0)
			previous.update();

		if (buttons.size() > (1 + page) * rows * 3)
			next.update();
		
		ScreenPartyHost.chatbox.update();
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

		Drawing.drawing.drawInterfaceText(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 210, "My levels");

		if (page > 0)
			previous.draw();

		if (buttons.size() > (1 + page) * rows * 3)
			next.draw();
		
		ScreenPartyHost.chatbox.draw();
		long time = System.currentTimeMillis();
		
		Drawing.drawing.setColor(0, 0, 0);
		for (int i = 0; i < ScreenPartyHost.chat.size(); i++)
		{
			ChatMessage c = ScreenPartyHost.chat.get(i);
			if (time - c.time <= 30000 || ScreenPartyHost.chatbox.selected)
			{
				Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
			}
		}
	}

}
