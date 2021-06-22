package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.rpc.RichPresenceEvent;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenCrusades extends Screen
{
	public static int page = 0;

	public SavedFilesList crusadesList;

	Button quit = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenPlaySingleplayer();
		}
	}
	);

	Button quit2 = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenPlaySingleplayer();
		}
	}
	);

	Button create = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Create crusade", new Runnable()
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
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		crusadesList = new SavedFilesList(Game.homedir + Game.crusadeDir, page,
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
				(name) -> null);


		crusadesList.buttons.add(0, new Button(0, 0, crusadesList.objWidth, crusadesList.objHeight, "Adventure crusade", new Runnable()
		{
			@Override
			public void run()
			{
				Crusade c = findExistingCrusadeProgress("internal/Adventure crusade");

				if (c == null)
				{
					ArrayList<String> al = Game.game.fileManager.getInternalFileContents("/crusades/adventure_crusade.tanks");
					c = new Crusade(al, "Adventure crusade", "/adventure_crusade.tanks");
				}

				Game.screen = new ScreenCrusadeDetails(c);
			}
		}
		));

		crusadesList.buttons.add(1, new Button(0, 0, crusadesList.objWidth, crusadesList.objHeight, "Classic crusade", new Runnable()
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

		crusadesList.sortButtons();

		Game.game.discordRPC.update(RichPresenceEvent.SINGLEPLAYER, RichPresenceEvent.CRUSADE_SELECT);
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

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, "Crusades");
	}
}
