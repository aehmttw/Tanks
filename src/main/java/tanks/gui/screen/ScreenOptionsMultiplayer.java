package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.event.EventChangeUsername;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenOptionsMultiplayer extends Screen
{
	public static final String chatFilterText = "Chat filter: ";
	public static final String autoReadyText = "Auto ready: ";

	TextBox username = new TextBox(this.centerX, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "Username", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.player.username = username.inputText;
			username.inputText = Game.player.username + "";

			if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
			{
				EventChangeUsername e = new EventChangeUsername(Game.player.clientID, Game.player.username);
				e.execute();
				Game.eventsOut.add(e);
			}

			if (!Game.player.username.equals(Game.chatFilter.filterChat(Game.player.username)))
				Game.screen = new ScreenUsernameWarning();
		}
	},
			Game.player.username, "Pick a username that players---will see in multiplayer");

	Button chatFilter = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.enableChatFilter = !Game.enableChatFilter;

			if (Game.enableChatFilter)
				chatFilter.setText(chatFilterText, ScreenOptions.onText);
			else
				chatFilter.setText(chatFilterText, ScreenOptions.offText);
		}
	},
			"Filters chat of potentially---inappropriate words");

	Button autoReady = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "", new Runnable()
	{
		@Override
		public void run()
		{
			Game.autoReady = !Game.autoReady;

			if (Game.autoReady)
				autoReady.setText(autoReadyText, ScreenOptions.onText);
			else
				autoReady.setText(autoReadyText, ScreenOptions.offText);
		}
	},
			"When enabled, automatically presses---the ready button if there is no shop");

	Button color = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Tank color", () -> Game.screen = new ScreenOptionsMultiplayerColor(),
			"Personalize your tank---to stand out in multiplayer!");

	Button hostOptions = new Button(this.centerX, this.centerY + this.objYSpace * 0, this.objWidth, this.objHeight, "Party host options", () -> Game.screen = new ScreenOptionsPartyHost(), "Options for parties you host");


	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions()
	);
	
	public ScreenOptionsMultiplayer()
	{
		this.music = "menu_options.ogg";
		this.musicID = "menu";

		username.enableCaps = true;
		username.enableSpaces = false;

		if (Game.enableChatFilter)
			chatFilter.setText(chatFilterText, ScreenOptions.onText);
		else
			chatFilter.setText(chatFilterText, ScreenOptions.offText);

		if (Game.autoReady)
			autoReady.setText(autoReadyText, ScreenOptions.onText);
		else
			autoReady.setText(autoReadyText, ScreenOptions.offText);
	}
	
	@Override
	public void update() 
	{
		chatFilter.update();
		back.update();
		username.update();
		color.update();
		hostOptions.update();
		autoReady.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		back.draw();
		autoReady.draw();
		hostOptions.draw();
		chatFilter.draw();
		color.draw();
		username.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Multiplayer options");
	}
}
