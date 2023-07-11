package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonList;

import java.util.ArrayList;

public class ScreenSharedCrusades extends Screen
{
	public static int page = 0;

	public ButtonList sharedCrusades;

	Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () ->
	{
		if (ScreenPartyHost.isServer)
			Game.screen = new ScreenSharedSummary(ScreenPartyHost.activeScreen.sharedLevels, ScreenPartyHost.activeScreen.sharedCrusades);
		else if (ScreenPartyLobby.isClient)
			Game.screen = new ScreenSharedSummary(ScreenPartyLobby.sharedLevels, ScreenPartyLobby.sharedCrusades);
	}
	);

	public ScreenSharedCrusades(ArrayList<ScreenPartyHost.SharedCrusade> crusades)
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		ArrayList<Button> buttons = new ArrayList<>();
		for (ScreenPartyHost.SharedCrusade l: crusades)
		{
			buttons.add(new Button(0, 0, this.objWidth, this.objHeight, l.name.replace("_", " "), () ->
			{
				Crusade c = new Crusade(l.crusade, l.name);
				Game.screen = new ScreenCrusadePreview(c, Game.screen, false);
			}
					, "Shared by " + l.creator));
		}

		sharedCrusades = new ButtonList(buttons, page, 0, -60);
	}

	@Override
	public void update()
	{
		quit.update();
		sharedCrusades.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		quit.draw();
		sharedCrusades.draw();

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, "Shared crusades");
	}

	@Override
	public void setupLayoutParameters()
	{
		this.centerY -= this.objYSpace / 2;
	}
}
