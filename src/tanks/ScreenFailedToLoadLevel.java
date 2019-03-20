package tanks;

public class ScreenFailedToLoadLevel extends Screen
{	
	String lvl;
	public ScreenFailedToLoadLevel(String lvl)
	{
		this.lvl = lvl;
		Game.currentSizeX = 28;
		Game.currentSizeY = 18;
		Game.resetTiles();
	}
	
	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenSavedLevels();
		}
	}
			);
	
	@Override
	public void update() 
	{
		quit.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		quit.draw();
		
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Tanks failed to load the level!");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, "This could be caused by a glitch in the editor,");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, "or by corruption of the level file.");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "If you manually modified the level file, please undo your changes.");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, "If this is not the case, please report the error!");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, "Check the log file for more information: ");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, Game.homedir.replace("\\", "/") + Game.logPath);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, "Please include the level file if reporting; it can be found at:");
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 180, lvl);
	}
}
