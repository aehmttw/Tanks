package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenUsernameInvalid extends Screen
{
	public ScreenUsernameInvalid()
	{
		this.music = "menu_options.ogg";
		this.musicID = "menu";
	}

	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Ok", () ->
	{
		Game.player.username = "";
		Game.screen = new ScreenTitle();
	}
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

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(this.textSize);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "The username you picked is invalid!");

		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "To prevent potential issues, it has been reset.");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 0.5, "Valid usernames are 1-18 characters long and");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY, "contain capital or lowercase letters,");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.objYSpace * 0.5, "numbers, and underscores.");

	}

}
