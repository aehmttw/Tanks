package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ChatMessage;

public class ScreenPartyResumeCrusade extends Screen
{
	public int players;

	public ScreenPartyResumeCrusade()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		for (int i = 0; i < Game.players.size(); i++)
		{
			if (Game.players.get(i).remainingLives >= 1)
				players++;
		}
	}
	
	Button resume = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Resume crusade", new Runnable()
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

	Button selectOtherCrusade = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Start another crusade", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenPartyCrusades();
		}
	}
			);
	
	Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
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
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		resume.draw();
		selectOtherCrusade.draw();
		quit.draw();

		Drawing.drawing.setInterfaceFontSize(this.textSize);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "A crusade you have not yet finished was found");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "Would you like to continue playing that");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1, "crusade, or to start a new crusade?");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY, "Progress in the current crusade will be lost");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.objYSpace * 0.5, "if you decide to start a new crusade!");
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.objYSpace * 1.5, "Crusade: %s", Crusade.currentCrusade.name.replace("_", " "));
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.objYSpace * 2, "Battle: %d", (Crusade.currentCrusade.currentLevel + 1));
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.objYSpace * 2.5, "Players alive: %d", players);
	}

	public void setupLayoutParameters()
	{
		this.centerY -= 60;
	}

}
