package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOptionsMultiplayer extends Screen
{
	public static final String chatFilterText = "Chat filter: ";
	public static final String autoReadyText = "Auto ready: ";
	public static final String anticheatText = "Anticheat: ";

	public static final String weakText = "\u00A7200100000255weak";
	public static final String strongText = "\u00A7000200000255strong";

	Button chatFilter = new Button(this.centerX, this.centerY + this.objYSpace * 0, this.objWidth, this.objHeight, "", new Runnable()
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

	Button autoReady = new Button(this.centerX, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "", new Runnable()
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

	Button hostOptions = new Button(this.centerX, this.centerY - this.objYSpace * 1, this.objWidth, this.objHeight, "Party host options", () -> Game.screen = new ScreenOptionsPartyHost(), "Options for parties you host");


	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions()
	);
	
	public ScreenOptionsMultiplayer()
	{
		this.music = "menu_options.ogg";
		this.musicID = "menu";

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

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Multiplayer options");
	}

}
