package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenPlay extends Screen
{
	public boolean showWorkshop = false;

	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTitle());

	Button singleplayer = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Singleplayer", () -> Game.screen = new ScreenPlaySingleplayer()
			, "Play random levels, crusades, minigames,---the tutorial, or make your own levels or crusades!");

	Button multiplayer = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Multiplayer", () ->
	{
		if (!Game.player.username.equals(""))
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

	Button workshop = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Steam workshop", () -> Game.screen = new ScreenSteamWorkshop()
			, "Publicly share and check out---levels and crusades other players---have made!");

	public ScreenPlay()
	{
		this.music = "menu_2.ogg";
		this.musicID = "menu";

		if (Game.steamNetworkHandler.initialized)
		{
			showWorkshop = true;
			singleplayer.posY -= this.objYSpace / 2;
			multiplayer.posY -= this.objYSpace / 2;
		}
	}

	@Override
	public void update() 
	{
		singleplayer.update();
		multiplayer.update();

		if (showWorkshop)
			workshop.update();

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

		if (showWorkshop)
			workshop.draw();

		multiplayer.draw();
		singleplayer.draw();
	}

}
