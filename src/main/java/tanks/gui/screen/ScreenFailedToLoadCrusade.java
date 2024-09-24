package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

import java.util.Date;
import java.util.Properties;

public class ScreenFailedToLoadCrusade extends Screen
{
	public String fileName;
	public Screen screen;
	public String path;
	public int hashcode;

	public ScreenFailedToLoadCrusade(String name, String contents, Exception e, Screen s)
	{
		super(350, 40, 380, 60);

		this.fileName = name.substring(name.replace("\\", "/").lastIndexOf("/") + 1);
		Game.currentSizeX = 28;
		Game.currentSizeY = 18;
		this.screen = s;
		Game.resetTiles();
		Game.cleanUp();
		e.printStackTrace();

		this.music = "ready_music_3.ogg";
		Drawing.drawing.playSound("leave.ogg");

		hashcode = contents.hashCode();

		try
		{
			BaseFile dir = Game.game.fileManager.getFile(Game.homedir + Game.crashesPath);
			if (!dir.exists())
				dir.mkdirs();

			this.path = Game.homedir + Game.crashesPath + fileName + "-" + hashcode + ".crash";
			BaseFile f = Game.game.fileManager.getFile(path);
			f.create();

			f.startWriting();
			f.println("Crusade load failure report: " + Game.version + " - " + new Date().toString() + "\n");
			f.println("Crusade name: " + fileName);
			f.println("Crusade contents: " + contents + "\n");
			f.println(e.toString());
			for (StackTraceElement el: e.getStackTrace())
			{
				f.println("at " + el.toString());
			}

			f.println("\nSystem properties:");
			Properties p = System.getProperties();
			for (Object o: p.keySet())
				f.println(o + ": " + p.get(o));

			f.stopWriting();
		}
		catch (Exception ex) {ex.printStackTrace();}
	}

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = screen;
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

		Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Tanks failed to load the crusade!");

		if (!ScreenPartyLobby.isClient || Game.connectedToOnline)
		{
			Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, "This could be caused by a glitch in the editor,");
			Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, "or by corruption of the crusade file.");
			Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "If you manually modified the crusade file, please undo your changes.");
			Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, "If this is not the case, please report the error!");
			Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, "Check the report file for more information: ");
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, Game.homedir.replace("\\", "/") + Game.crashesPath + fileName + "-" + hashcode + ".crash");
		}
		else
		{
			Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "If this is your crusade, it may be corrupted!");
		}
	}
}
