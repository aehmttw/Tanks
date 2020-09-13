package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ChatMessage;

public class ScreenPartyResumeCrusade extends Screen implements IPartyMenuScreen
{
	public int players;

	public ScreenPartyResumeCrusade()
	{
		this.music = "tomato_feast_4.ogg";
		this.musicID = "menu";

		for (int i = 0; i < Game.players.size(); i++)
		{
			if (Game.players.get(i).remainingLives >= 1)
				players++;
		}
	}
	
	Button resume = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Resume crusade", new Runnable()
	{
		@Override
		public void run() 
		{
			Crusade.crusadeMode = true;
			Crusade.currentCrusade.loadLevel();
			Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
		}
	}
			);

	Button selectOtherCrusade = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Start another crusade", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenPartyCrusades();
		}
	}
			);
	
	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = ScreenPartyHost.activeScreen;
		}
	}
			);
	
	@Override
	public void update() 
	{
		resume.update();
		selectOtherCrusade.update();
		quit.update();

		ScreenPartyHost.chatbox.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		resume.draw();
		selectOtherCrusade.draw();
		quit.draw();
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "A crusade you have not yet finished was found");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, "Would you like to continue playing that");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, "crusade, or to start a new crusade?");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Progress in the current crusade will be lost");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, "if you decide to start a new crusade!");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, "Crusade: " + Crusade.currentCrusade.name.replace("_", " "));
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, "Battle: " + (Crusade.currentCrusade.currentLevel + 1));
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, "Players alive: " + players);

		ScreenPartyHost.chatbox.draw();

		Drawing.drawing.setColor(0, 0, 0);

		long time = System.currentTimeMillis();
		for (int i = 0; i < ScreenPartyHost.chat.size(); i++)
		{
			ChatMessage c = ScreenPartyHost.chat.get(i);
			if (time - c.time <= 30000 || ScreenPartyHost.chatbox.selected)
			{
				Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
			}
		}
	}

}
