package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenUsernameWarning extends Screen
{
	public ScreenUsernameWarning()
	{
		this.music = "menu_options.ogg";
		this.musicID = "menu";
	}

	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Ok", () -> Game.screen = new ScreenOptionsPersonalize()
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
		back.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Notice!");

		Drawing.drawing.setInterfaceFontSize(this.textSize);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "The username you picked will be redacted to players");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace, "who have not disabled the chat filter.");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 0, "If you would like these players to see your username,");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.objYSpace * 0.5, "please pick another one.");

	}

}
