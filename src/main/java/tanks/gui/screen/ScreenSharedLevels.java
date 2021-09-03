package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonList;

import java.util.ArrayList;

public class ScreenSharedLevels extends Screen
{
	public static int page = 0;

	public ButtonList sharedLevels;

	Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			if (ScreenPartyHost.isServer)
				Game.screen = new ScreenSharedSummary(ScreenPartyHost.activeScreen.sharedLevels, ScreenPartyHost.activeScreen.sharedCrusades);
			else if (ScreenPartyLobby.isClient)
				Game.screen = new ScreenSharedSummary(ScreenPartyLobby.sharedLevels, ScreenPartyLobby.sharedCrusades);
		}
	}
			);

	public ScreenSharedLevels(ArrayList<ScreenPartyHost.SharedLevel> levels)
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		ArrayList<Button> buttons = new ArrayList<>();
		for (ScreenPartyHost.SharedLevel l: levels)
		{
			buttons.add(new Button(0, 0, this.objWidth, this.objHeight, l.name.replace("_", " "), new Runnable()
			{
				@Override
				public void run()
				{
					ScreenSaveLevel sc = new ScreenSaveLevel(l.name, l.level, Game.screen);
					Level lev = new Level(l.level);
					lev.preview = true;
					lev.loadLevel(sc);
					Game.screen = sc;
				}
			}
					, "Shared by " + l.creator));
		}

		sharedLevels = new ButtonList(buttons, page, 0, -60);
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
		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, "Shared levels");
	}

	@Override
	public void setupLayoutParameters()
	{
		this.centerY -= this.objYSpace / 2;
	}
}
