package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenOptionsPersonalize extends Screen
{
	public boolean fromMultiplayer;

	TextBox username = new TextBox(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Username", new Runnable()
	{
		@Override
		public void run()
		{
			Game.player.username = username.inputText;
			username.inputText = Game.player.username + "";

			if (!Game.player.username.equals(Game.chatFilter.filterChat(Game.player.username)))
				Game.screen = new ScreenUsernameWarning();
		}
	},
			Game.player.username, "Pick a username that players---will see in multiplayer");


	Button color = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Tank color", () -> Game.screen = new ScreenOptionsPlayerColor(),
			"Personalize your tank!");


	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () ->
	{
		if (!fromMultiplayer)
			Game.screen = new ScreenOptions();
		else
			Game.screen = new ScreenPlay();
	});

	public ScreenOptionsPersonalize()
	{
		this.music = "menu_options.ogg";
		this.musicID = "menu";

		username.enableCaps = true;
		username.enableSpaces = false;
		username.maxChars = 20;
	}

	public ScreenOptionsPersonalize(boolean fromMultiplayer)
	{
		this();
		this.fromMultiplayer = fromMultiplayer;
	}
	
	@Override
	public void update() 
	{
		back.update();
		username.update();
		color.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		back.draw();
		color.draw();
		username.draw();

		Button.drawGlow(color.posX - color.sizeX / 2 + color.sizeY / 2, color.posY + 2.5, color.sizeY * 3 / 4, color.sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);

		Drawing.drawing.setColor(Game.player.turretColorR, Game.player.turretColorG, Game.player.turretColorB);
		Drawing.drawing.fillInterfaceOval(color.posX - color.sizeX / 2 + color.sizeY / 2, color.posY, color.sizeY * 0.8, color.sizeY * 0.8);

		Drawing.drawing.setColor(Game.player.colorR, Game.player.colorG, Game.player.colorB);
		Drawing.drawing.fillInterfaceOval(color.posX - color.sizeX / 2 + color.sizeY / 2, color.posY, color.sizeY * 0.6, color.sizeY * 0.6);

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "My profile");
	}

}
