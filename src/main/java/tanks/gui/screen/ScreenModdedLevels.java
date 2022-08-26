package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.SearchBox;
import tanks.modapi.ModAPI;
import tanks.modapi.ModGame;
import tanks.modapi.ModLevel;

import java.util.ArrayList;

public class ScreenModdedLevels extends Screen
{
	public ButtonList fullModdedLevelsList;
	public ButtonList moddedLevelsList;

	public static int page = 0;

	SearchBox search = new SearchBox(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
	{
		@Override
		public void run()
		{
			createNewLevelsList();
			moddedLevelsList.filter(search.inputText);
			moddedLevelsList.sortButtons();
		}
	}, "");

	public void createNewLevelsList()
	{
		moddedLevelsList.buttons.clear();
		moddedLevelsList.buttons.addAll(fullModdedLevelsList.buttons);
		moddedLevelsList.sortButtons();
	}

	Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> Game.screen = (ScreenPartyHost.isServer ? ScreenPartyHost.activeScreen : new ScreenPlaySingleplayer()));

	public ScreenModdedLevels()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		ArrayList<Button> buttons = new ArrayList<>();

		try
		{
			for (Class<? extends ModLevel> m : ModAPI.registeredCustomLevels)
			{
				String description = m.getConstructor().newInstance().description;
				Button b = new Button(0, 0, 0, 0, m.getConstructor().newInstance().name, () ->
				{
					try
					{
						m.getConstructor().newInstance().loadLevel();
					}
					catch (Exception e)
					{
						Game.exitToCrash(e);
					}
				}, description);

				b.enableHover = description != null && description.length() > 0;

				buttons.add(b);
			}

			for (Class<? extends ModGame> m : ModAPI.registeredCustomGames)
			{
				String description = m.getConstructor().newInstance().description;
				Button b = new Button(0, 0, 0, 0, m.getConstructor().newInstance().name, () ->
				{
					try
					{
						ModGame g = m.getConstructor().newInstance();
						Game.currentGame = g;
						g.start();
					}
					catch (Exception e)
					{
						Game.exitToCrash(e);
					}
				},
						description);

				b.enableHover = description != null && description.length() > 0;

				buttons.add(b);
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}

		fullModdedLevelsList = new ButtonList(buttons, page, 0, -30);
		moddedLevelsList = fullModdedLevelsList.clone();

		createNewLevelsList();
		search.enableCaps = true;
	}

	@Override
	public void update()
	{
		search.update();

		moddedLevelsList.update();
		page = moddedLevelsList.page;

		quit.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		search.draw();

		moddedLevelsList.draw();

		quit.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, "Modded levels");
	}
}