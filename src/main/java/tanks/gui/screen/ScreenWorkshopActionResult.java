package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;

import java.util.ArrayList;

public class ScreenWorkshopActionResult extends Screen
{
	public Screen next;
	public String message;
	public String details;

	public ScreenWorkshopActionResult(Screen next, String message, String details, boolean success)
	{
		this.music = next.music;
		this.musicID = next.musicID;
		this.next = next;

		if (success)
			Drawing.drawing.playSound("join.ogg", 1.5f);
		else
			Drawing.drawing.playSound("leave.ogg");

		Panel.forceRefreshMusic = true;

		this.message = message;
		this.details = details;
	}
		
	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Ok", () -> Game.screen = next);

	@Override
	public void update() 
	{
		back.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		Drawing.drawing.setColor(0, 0, 0);

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, this.message);

		Drawing.drawing.setInterfaceFontSize(this.textSize);
		ArrayList<String> lines = Drawing.drawing.wrapText(this.details, Drawing.drawing.interfaceSizeX - 50, this.textSize);

		for (int lineNo = 0; lineNo < lines.size(); lineNo++)
		{
			Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 0.5 + lineNo * 35, lines.get(lineNo));
		}

		back.draw();
	}
	
}
