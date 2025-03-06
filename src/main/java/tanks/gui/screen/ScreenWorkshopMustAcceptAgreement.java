package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;

import java.net.URL;
import java.util.ArrayList;

public class ScreenWorkshopMustAcceptAgreement extends ScreenWorkshopActionResult
{
	public ScreenWorkshopMustAcceptAgreement(Screen next, String message, String details, boolean success)
	{
		super(next, message, details, success);
		this.back.posX -= this.objXSpace / 2;
	}

	Button agreement = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "View agreement", () ->
	{
		try
		{
			Game.game.window.openLink(new URL("https://steamcommunity.com/sharedfiles/workshoplegalagreement"));
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	});

	@Override
	public void update()
	{
		super.update();
		agreement.update();
	}

	@Override
	public void draw()
	{
		super.draw();
		agreement.draw();
	}

}
