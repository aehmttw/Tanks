package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;

import java.util.ArrayList;

public class ScreenKicked extends Screen
{
	public String reason;
	
	public ScreenKicked(String reason)
	{
		this.music = "menu_1.ogg";
		this.musicID = "menu";

		Drawing.drawing.playSound("leave.ogg");

		Panel.forceRefreshMusic = true;

		this.reason = reason;
		ScreenPartyLobby.connections.clear();
	}
		
	Button back = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Ok", () -> Game.screen = Game.lastOfflineScreen
	);

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
		Drawing.drawing.setInterfaceFontSize(this.textSize);

		ArrayList<String> lines = Drawing.drawing.wrapText(this.reason, Drawing.drawing.interfaceSizeX - 50, this.textSize);

		for (int lineNo = 0; lineNo < lines.size(); lineNo++)
		{
			Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5 + lineNo * 35, lines.get(lineNo));
		}

		back.draw();
	}
	
}
