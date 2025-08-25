package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.SearchBoxInstant;
import tanks.minigames.Minigame;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class ScreenMinigames extends Screen
{
	public ButtonList fullModdedLevelsList;
	public ButtonList moddedLevelsList;

	public static int page = 0;

	SearchBoxInstant search = new SearchBoxInstant(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
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

	Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", ScreenPartyHost.isServer ? () -> Game.screen = ScreenPartyHost.activeScreen : () -> Game.screen = new ScreenPlaySingleplayer());

	public ScreenMinigames()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		ArrayList<Button> buttons = new ArrayList<>();

		for (String name : Game.registryMinigame.minigames.keySet())
		{
			buttons.add(new Button(0, 0, 0, 0, name, () ->
			{
				try
				{
					ScreenInterlevel.fromMinigames = true;
					Minigame m = Game.registryMinigame.getEntry(name).getConstructor().newInstance();
					m.loadLevel();
				}
				catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
				{
					Game.exitToCrash(e.getCause());
				}
			}, Game.registryMinigame.minigameDescriptions.get(name)));
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
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, "Minigames");
	}
}
