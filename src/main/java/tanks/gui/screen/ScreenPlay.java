package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenPlay extends Screen
{
	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTitle()
	);

	Button singleplayer = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Singleplayer", () -> Game.screen = new ScreenPlaySingleplayer()
			, "Play random levels, crusades,---the tutorial, or make your own levels!");

	Button multiplayer = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Multiplayer", () ->
	{
		if (!Game.player.username.isEmpty())
		{
			if (Game.game.input.hotbarToggle.isPressed())
				Game.screen = new ScreenPlayMultiplayer();
			else
				Game.screen = new ScreenParty();
		}
		else
			Game.screen = new ScreenUsernamePrompt();
	}
			, "Play, chat, and share levels---and crusades with other players!");

	public ScreenPlay()
	{
		this.music = "menu_2.ogg";
		this.musicID = "menu";
	}

	@Override
	public void update() 
	{
		singleplayer.update();
		multiplayer.update();
		back.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Select a game mode");
		back.draw();
		multiplayer.draw();
		singleplayer.draw();
	}

}
