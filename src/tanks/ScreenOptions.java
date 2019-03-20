package tanks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Scanner;

public class ScreenOptions extends Screen
{
	public ScreenOptions()
	{
		if (Game.fancyGraphics)
			graphics.text = "Graphics: fancy";
		else
			graphics.text = "Graphics: fast";

		if (Panel.showMouseTarget)
			mouseTarget.text = "Mouse target: on";
		else
			mouseTarget.text = "Mouse target: off";

		if (Game.autostart)
			autostart.text = "Autostart: on";
		else
			autostart.text = "Autostart: off";
	}

	Button graphics = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, 350, 40, "Graphics: fancy", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.fancyGraphics = !Game.fancyGraphics;

			if (Game.fancyGraphics)
				graphics.text = "Graphics: fancy";
			else
				graphics.text = "Graphics: fast";
		}
	},
			"Fast graphics disable most graphical effects and use solid colors for the background---Fancy graphics may significantly reduce framerate"	);

	Button mouseTarget = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Mouse target: on", new Runnable()
	{
		@Override
		public void run() 
		{
			Panel.showMouseTarget = !Panel.showMouseTarget;

			if (Panel.showMouseTarget)
				mouseTarget.text = "Mouse target: on";
			else
				mouseTarget.text = "Mouse target: off";
		}
	},
			"When enabled, 2 small black rings will appear around your mouse pointer"	);

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			saveOptions(Game.homedir);
			Game.screen = new ScreenTitle();
		}
	}
			);

	Button autostart = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Autostart: on", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.autostart = !Game.autostart;

			if (Game.autostart)
				autostart.text = "Autostart: on";
			else
				autostart.text = "Autostart: off";
		}
	},
			"When enabled, levels will start playing automatically---4 seconds after they are loaded if the play button isn't clicked earlier"	);

	@Override
	public void update()
	{
		autostart.update();
		mouseTarget.update();
		graphics.update();
		back.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		back.draw();
		autostart.draw();
		mouseTarget.draw();
		graphics.draw();
		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 150, "Options");
	}

	public static void initOptions(String homedir)
	{
		String path = homedir + Game.optionsPath;

		try 
		{
			new File(path).createNewFile();
		}
		catch (IOException e) 
		{
			Game.logger.println (new Date().toString() + " (syserr) file permissions are broken! cannot initialize options file.");
			System.exit(1);
		}
		
		saveOptions(homedir);
	}

	public static void saveOptions(String homedir) 
	{
		String path = homedir + Game.optionsPath;

		PrintStream writer;
		
		try
		{
			writer = new PrintStream(new File(path));
			writer.println("# This file stores game settings that you have set");
			writer.println("fancy_graphics=" + Game.fancyGraphics);
			writer.println("mouse_target=" + Panel.showMouseTarget);
			writer.println("auto_start=" + Game.autostart);
			writer.println("use-custom-tank-registry=" + Game.enableCustomTankRegistry);
			writer.println("use-custom-obstacle-registry=" + Game.enableCustomObstacleRegistry);
		}
		catch (FileNotFoundException e)
		{
			Game.exitToCrash(e);
		}

	
	}
	
	public static void loadOptions(String homedir) 
	{
		String path = homedir + Game.optionsPath;

		try 
		{
			Scanner in = new Scanner(new File(path));
			while (in.hasNextLine()) 
			{
				String line = in.nextLine();
				String[] optionLine = line.split("=");

				if (optionLine[0].charAt(0) == '#') 
				{ 
					continue; 
				}
				
				if (optionLine[0].toLowerCase().equals("fancy_graphics")) 
					Game.fancyGraphics = Boolean.parseBoolean(optionLine[1]);
				else if (optionLine[0].toLowerCase().equals("mouse_target")) 
					Panel.showMouseTarget = Boolean.parseBoolean(optionLine[1]);
				else if (optionLine[0].toLowerCase().equals("auto_start")) 
					Game.autostart = Boolean.parseBoolean(optionLine[1]);
				else if (optionLine[0].toLowerCase().equals("use-custom-tank-registry")) 
					Game.enableCustomTankRegistry = Boolean.parseBoolean(optionLine[1]);
				else if (optionLine[0].toLowerCase().equals("use-custom-obstacle-registry")) 
					Game.enableCustomObstacleRegistry = Boolean.parseBoolean(optionLine[1]);
			}
			in.close();
		} 
		catch (Exception e)
		{
			Game.logger.println (new Date().toString() + " (syswarn) obstacle registry file is nonexistent or broken, using default:");
			e.printStackTrace(Game.logger);
		}


	}

}
