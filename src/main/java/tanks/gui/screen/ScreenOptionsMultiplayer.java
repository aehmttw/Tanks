package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.network.SteamNetworkHandler;

public class ScreenOptionsMultiplayer extends Screen
{
	public static final String chatFilterText = "Chat filter: ";
	public static final String autoReadyText = "Auto ready: ";
	public static final String anticheatText = "Anticheat: ";
    public static final String publicPartyCount = "Public party count: ";

	public static final String weakText = "\u00A7200100000255weak";
	public static final String strongText = "\u00A7000200000255strong";

	Button chatFilter = new Button(this.centerX, this.centerY + this.objYSpace * -0.5, this.objWidth, this.objHeight, "", new Runnable()
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

	Button autoReady = new Button(this.centerX, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
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

    Button showPublicPartyCount = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            SteamNetworkHandler.showPublicPartyCount = !SteamNetworkHandler.showPublicPartyCount;

            if (SteamNetworkHandler.showPublicPartyCount)
                showPublicPartyCount.setText(publicPartyCount, ScreenOptions.onText);
            else
                showPublicPartyCount.setText(publicPartyCount, ScreenOptions.offText);
        }
    },
            "When enabled, the multiplayer button---will show the number of running Steam---public parties, if there are any");


    Button hostOptions = new Button(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Party host options", () -> Game.screen = new ScreenOptionsPartyHost(), "Options for parties you host");


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

        if (SteamNetworkHandler.showPublicPartyCount)
            showPublicPartyCount.setText(publicPartyCount, ScreenOptions.onText);
        else
            showPublicPartyCount.setText(publicPartyCount, ScreenOptions.offText);

        if (!Game.steamNetworkHandler.initialized)
        {
            chatFilter.posY += this.objYSpace * 0.5;
            autoReady.posY += this.objYSpace * 0.5;
            hostOptions.posY += this.objYSpace * 0.5;
        }
	}
	
	@Override
	public void update() 
	{
		chatFilter.update();
		back.update();
		hostOptions.update();
		autoReady.update();

        if (Game.steamNetworkHandler.initialized)
            showPublicPartyCount.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		back.draw();
		autoReady.draw();
		hostOptions.draw();
		chatFilter.draw();

        if (Game.steamNetworkHandler.initialized)
            showPublicPartyCount.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Multiplayer options");
	}

}
