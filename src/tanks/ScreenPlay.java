package tanks;

public class ScreenPlay extends Screen
{

	Button newLevel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, 350, 40, "Random level", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.reset();
			Game.screen = new ScreenGame();
		}
	}
			, "Generate a random level to play");
	
	Button crusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, 350, 40, "Crusades", new Runnable()
	{
		@Override
		public void run() 
		{
			if (Crusade.currentCrusade == null)
				Game.screen = new ScreenCrusades();
			else
				Game.screen = new ScreenResumeCrusade();

		}
	}
			, "Fight battles in an order,---and see how long you can survive!");
	
	Button online = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Online", "Online mode is coming soon!");
	
	Button party = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Party", "Party mode is coming soon!");
	
	Button tutorial = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "Tutorial", new Runnable()
	{
		@Override
		public void run() 
		{
			ScreenTutorial s = new ScreenTutorial();
			s.fromInitial = false;
			Game.screen = s;
		}
	}, "Learn how to play Tanks!"
	);

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 210, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenTitle();
		}
	}
	);
	
	@Override
	public void update() 
	{
		newLevel.update();
		crusade.update();
		online.update();
		party.update();
		tutorial.update();
		back.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		Drawing.drawing.setFontSize(24);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Select a game mode");
		back.draw();
		tutorial.draw();
		party.draw();
		online.draw();
		crusade.draw();
		newLevel.draw();

	}

}
