package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.ModAPI;
import tanks.ModLevel;
import tanks.gui.Button;
import tanks.gui.ButtonList;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class ScreenPlayModdedLevels extends Screen
{
	public String title = "My levels";

	public ButtonList levels;

	public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> {
		if (ScreenPartyHost.isServer)
			Game.screen = ScreenPartyHost.activeScreen;
		else
			Game.screen = new ScreenPlaySingleplayer();
	});

	public ScreenPlayModdedLevels()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		this.initializeLevels();
	}

	public void initializeLevels()
	{
		ArrayList<Button> buttons = new ArrayList<>();

		for (Class<? extends ModLevel> m : ModAPI.registeredCustomLevels)
		{
			buttons.add(new Button(0, 0, 0, 0, m.getSimpleName().replace('_', ' '), () ->
			{
				try
				{
					m.getConstructor().newInstance();
				}
				catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e)
				{
					Game.exitToCrash(e.getCause());
				}
			}));
		}

		this.levels = new ButtonList(buttons, ScreenModdedLevels.page, 0, -60);
	}

	@Override
	public void update()
	{
		this.levels.update();
		this.quit.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		this.levels.draw();
		this.quit.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, this.title);
	}
	
	@Override
	public void setupLayoutParameters()
	{
		if (Drawing.drawing.interfaceScaleZoom > 1 && ScreenPartyHost.isServer)
			this.centerY -= this.objYSpace / 2;
	}
}