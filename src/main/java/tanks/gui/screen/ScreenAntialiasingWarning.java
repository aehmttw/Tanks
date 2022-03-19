package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenAntialiasingWarning extends Screen
{
	public ScreenAntialiasingWarning()
	{
		this.music = "menu_options.ogg";
		this.musicID = "menu";
	}

	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Ok", () -> Game.screen = new ScreenOptionsGraphics()
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

		String s = "disabled";

		if (Game.antialiasing)
			s = "enabled";

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Notice!");

		Drawing.drawing.setInterfaceFontSize(this.textSize);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace / 2, "Antialiasing will be %s", s);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY, "the next time you start the game.");

	}

}
