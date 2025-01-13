package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenAcceptSteamInvite extends Screen
{
	public ScreenAcceptSteamInvite()
	{
		this.music = "menu_1.ogg";
		this.musicID = "menu";
	}

	Button connect = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Join party!", () ->
	{
		ScreenJoinParty s = new ScreenJoinParty();
		String s1 = s.ip.inputText;
		s.ip.inputText = "lobby:" + Long.toHexString(Game.steamLobbyInvite);
		Game.steamLobbyInvite = -1;
		s.join.function.run();
		s.ip.inputText = s1;
		Game.lastOfflineScreen = new ScreenTitle();
	});
	
	Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Decline invitation", () ->
	{
		Game.screen = new ScreenTitle();
		Game.steamLobbyInvite = -1;
	});
	
	@Override
	public void update() 
	{
		connect.update();
		quit.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		connect.draw();
		quit.draw();

		Drawing.drawing.setInterfaceFontSize(this.textSize);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "You were invited to join a party!");
	}

}
