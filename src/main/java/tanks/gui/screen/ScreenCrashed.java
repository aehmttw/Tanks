package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;

import java.net.URL;

public class ScreenCrashed extends Screen
{
	public String sadFace = ":(";
	public String ohNoes = "Oh noes!";
	public int hmm = 0;

	public ScreenCrashed()
	{
		super(350, 40, 380, 60);

		if (Math.random() < 0.01)
		{
			int r = (int) (Math.random() * 5);

			if (r == 0)
				sadFace = ":)";
			else if (r == 1)
				sadFace = ":O";
			else if (r == 2)
				sadFace = ">:(";
			else if (r == 3)
			{
				sadFace = ":3";
				ohNoes = "Oh nyos!";
			}
			else if (r == 4)
				sadFace = ";(";
		}

		if (Math.random() < 0.01)
		{
			sadFace = sadFace.substring(0, sadFace.length() - 1) + "-" + sadFace.charAt(sadFace.length() - 1);
			ohNoes = "Oh nose!";
		}

		this.music = "ready_music_3.ogg";
		this.musicID = "crash";
		if (Math.random() < 0.01)
		{
			this.music = "ready_music_4.ogg";
			this.hmm = 1;
		}
		else if (Math.random() < 0.01)
		{
			this.music = "ready_music_5.ogg";
			this.hmm = 2;
		}

		Panel.forceRefreshMusic = true;

		double imgsize = 25 * Drawing.drawing.interfaceScaleZoom;
		this.chatroom.image = "icons/link.png";
		this.chatroom.imageSizeX = imgsize;
		this.chatroom.imageSizeY = imgsize;
		this.chatroom.imageXOffset = 150 * this.chatroom.sizeX / 350;
	}

	Button exit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 100, this.objWidth, this.objHeight, "Exit the game", () -> System.exit(0));

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 160, this.objWidth, this.objHeight, "Return to title", Game::exitToTitle);

	Button chatroom = new Button(Drawing.drawing.interfaceSizeX / 2 - 380, Drawing.drawing.interfaceSizeY - 100, this.objWidth, this.objHeight, "Get help on Discord", () ->
	{
		try
		{
			Game.game.window.openLink(new URL(ScreenAbout.discord_link));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	);

	@Override
	public void update()
	{
		this.quit.update();
		this.exit.update();
		this.chatroom.update();
	}

	@Override
	public void draw()
	{
		Drawing drawing = Drawing.drawing;
		drawing.setColor(0, 0, 255);

		if (hmm == 1)
			drawing.setColor(255, 0, 0);
		else if (hmm == 2)
			drawing.setColor(0, 0, 0);

		Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);

		drawing.setColor(255, 255, 255);
		if (hmm == 1)
			drawing.setColor(0, 0, 0);
		else if (hmm == 2)
			drawing.setColor(255, 255 * (Math.sin(this.screenAge / 100 * 60 / 130 * Math.PI * 2) / 2 + 0.5), 0);

		drawing.setInterfaceFontSize(100);

		if (Drawing.drawing.interfaceScaleZoom > 1)
			drawing.drawInterfaceText(50, 100, sadFace);
		else
			drawing.drawInterfaceText(100, 100, sadFace);

		drawing.setInterfaceFontSize(48);
		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 100, ohNoes + " Tanks ran into a problem!");

		drawing.setInterfaceFontSize(24);

		drawing.displayInterfaceText(50, 200, false, "You may return to the game if you wish, but be warned that things may become unstable.");
		drawing.displayInterfaceText(50, 230, false, "If you see this screen again, restart the game.");
		drawing.displayInterfaceText(50, 290, false, "Also, you may want to report this crash!");

		drawing.displayInterfaceText(50, 350,  false, "Crash details:");

		double boxWidth = 1300;
		double width = Game.game.window.fontRendererDefault.getStringSizeX(Drawing.drawing.fontSize, Game.crashMessage) / Drawing.drawing.interfaceScale;
		double width2 = Game.game.window.fontRendererDefault.getStringSizeX(Drawing.drawing.fontSize, Game.crashLine) / Drawing.drawing.interfaceScale;

		double scale = Math.min(1, boxWidth / width);
		double scale2 = Math.min(1, boxWidth / width2);

		drawing.setInterfaceFontSize(24 * scale);
		drawing.drawInterfaceText(50, 380, Game.crashMessage, false);
		drawing.setInterfaceFontSize(24 * scale2);
		drawing.drawInterfaceText(50, 410, Game.crashLine, false);

		int extensions = Game.extensionRegistry.extensions.size();
		String extText = extensions == 0 ? "" : extensions == 1 ? " (with 1 extension)" : " (with " + extensions + " extensions)";

		drawing.setInterfaceFontSize(24);
		drawing.displayInterfaceText(50, 440,  false, "Game version: " + Game.version + extText + " " + Game.game.window.buildDate);

		drawing.displayInterfaceText(50, 500,  false, "Check the crash report file for more information: ");
		drawing.drawInterfaceText(50, 530, Game.homedir.replace("\\", "/") + Game.crashesPath + Game.crashTime + ".crash", false);

		this.quit.draw();
		this.exit.draw();
		this.chatroom.draw();
	}

}
