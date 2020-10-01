package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonList;

import java.util.ArrayList;

public class ScreenSharedLevels extends Screen implements IPartyMenuScreen
{
	public static int page = 0;

	public ButtonList sharedLevels;

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			if (ScreenPartyHost.isServer)
				Game.screen = ScreenPartyHost.activeScreen;
			else
				Game.screen = new ScreenPartyLobby();
		}
	}
			);

	public ScreenSharedLevels(ArrayList<ScreenPartyHost.SharedLevel> levels)
	{
		this.music = "tomato_feast_4.ogg";
		this.musicID = "menu";

		ArrayList<Button> buttons = new ArrayList<>();
		for (ScreenPartyHost.SharedLevel l: levels)
		{
			buttons.add(new Button(0, 0, 350, 40, l.name.replace("_", " "), new Runnable()
			{
				@Override
				public void run()
				{
					ScreenSaveSharedLevel sc = new ScreenSaveSharedLevel(l.name, l.level, Game.screen);
					Level lev = new Level(l.level);
					lev.preview = true;
					lev.loadLevel(sc);
					Game.screen = sc;
				}
			}
					, "Shared by " + l.creator));
		}

		sharedLevels = new ButtonList(buttons, page, 0, -30);
	}

	@Override
	public void update()
	{
		quit.update();
		sharedLevels.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		quit.draw();
		sharedLevels.draw();

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, "Shared levels");
	}
}
