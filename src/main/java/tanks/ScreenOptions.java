package tanks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Scanner;

import org.lwjgl.glfw.GLFW;

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
		
		if (Game.vsync)
			vsync.text = "V-Sync: on";
		else
			vsync.text = "V-Sync: off";
		
		if (Game.enable3d)
			graphics3d.text = "3D graphics: on";
		else
			graphics3d.text = "3D graphics: off";
		
		username.enableCaps = true;
		username.enableSpaces = false;

	}

	Button graphics = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, 350, 40, "Graphics: fancy", new Runnable()
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
			"Fast graphics disable most graphical effects---and use solid colors for the background------Fancy graphics may significantly reduce framerate"	);

	Button graphics3d = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, 350, 40, "3D graphics: on", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.enable3d = !Game.enable3d;

			if (Game.enable3d)
				graphics3d.text = "3D graphics: on";
			else
				graphics3d.text = "3D graphics: off";
		}
	},
			"3D graphics may impact performance");

	
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
			"When enabled, 2 small black rings---will appear around your mouse pointer"	);

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Back", new Runnable()
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
			"When enabled, levels will start playing---automatically 4 seconds after they are loaded---(if the play button isn't clicked earlier)");

	Button vsync = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "V-Sync: on", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.vsync = !Game.vsync;

			if (Game.vsync)
			{
				GLFW.glfwSwapInterval(1);
				vsync.text = "V-Sync: on";
			}
			else
			{
				GLFW.glfwSwapInterval(0);
				vsync.text = "V-Sync: off";
			}
		}
	},
			"Limits framerate to your screen's refresh rate---May decrease battery consumption---Also, might fix issues with inconsistent game speed");

	TextBox username = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 180, 350, 40, "Username", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.username = username.inputText;
			username.inputText = Game.username + "";
			
			if (!Game.username.equals(Game.chatFilter.filterChat(Game.username)))
				Game.screen = new ScreenUsernameWarning();
		}
	},
			Game.username, "Pick a username that players---will see in multiplayer");
	
	
	@Override
	public void update()
	{
		username.update();
		autostart.update();
		mouseTarget.update();
		graphics.update();
		graphics3d.update();
		vsync.update();
		back.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		back.draw();
		username.draw();
		vsync.draw();
		autostart.draw();
		mouseTarget.draw();
		graphics3d.draw();
		graphics.draw();
		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 210, "Options");
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
			writer.println("username=" + Game.username);
			writer.println("fancy_graphics=" + Game.fancyGraphics);
			writer.println("3d=" + Game.enable3d);
			writer.println("mouse_target=" + Panel.showMouseTarget);
			writer.println("auto_start=" + Game.autostart);
			writer.println("vsync=" + Game.vsync);
			writer.println("port=" + Game.port);
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
				
				if (optionLine[0].toLowerCase().equals("username")) 
				{
					if (optionLine.length >= 2)
						Game.username = optionLine[1];
					else
						Game.username = "";
				}
				if (optionLine[0].toLowerCase().equals("fancy_graphics")) 
					Game.fancyGraphics = Boolean.parseBoolean(optionLine[1]);
				if (optionLine[0].toLowerCase().equals("3d")) 
					Game.enable3d = Boolean.parseBoolean(optionLine[1]);
				else if (optionLine[0].toLowerCase().equals("mouse_target")) 
					Panel.showMouseTarget = Boolean.parseBoolean(optionLine[1]);
				else if (optionLine[0].toLowerCase().equals("auto_start")) 
					Game.autostart = Boolean.parseBoolean(optionLine[1]);
				else if (optionLine[0].toLowerCase().equals("vsync")) 
					Game.vsync = Boolean.parseBoolean(optionLine[1]);
				else if (optionLine[0].toLowerCase().equals("port")) 
					Game.port = Integer.parseInt(optionLine[1]);
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
