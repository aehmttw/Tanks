package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import org.lwjgl.glfw.GLFW;

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
		
		if (Game.vsync)
			vsync.text = "V-Sync: on";
		else
			vsync.text = "V-Sync: off";
		
		if (Game.enable3d)
			graphics3d.text = "3D graphics: on";
		else
			graphics3d.text = "3D graphics: off";

		if (Game.enable3dBg)
			ground3d.text = "3D ground: on";
		else
			ground3d.text = "3D ground: off";

		if (Drawing.drawing.enableStats)
			showStats.text = "Info bar: on";
		else
			showStats.text = "Info bar: off";

		if (Game.angledView)
			altPerspective.text = "View: angled";
		else
			altPerspective.text = "View: bird's-eye";
	}

	Button graphics = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 120, 350, 40, "Graphics: fancy", new Runnable()
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

	Button graphics3d = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 120, 350, 40, "3D graphics: on", new Runnable()
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

	Button ground3d = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "3D ground: on", new Runnable()
	{
		@Override
		public void run()
		{
			Game.enable3dBg = !Game.enable3dBg;

			if (Game.enable3dBg)
				ground3d.text = "3D ground: on";
			else
				ground3d.text = "3D ground: off";
		}
	},
			"Enabling 3D ground may impact---performance in large levels------Requires 3D and fancy---graphics to take effect");


	Button altPerspective = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "View: bird's-eye", new Runnable()
	{
		@Override
		public void run()
		{
			Game.angledView = !Game.angledView;

			if (Game.angledView)
				altPerspective.text = "View: angled";
			else
				altPerspective.text = "View: bird's-eye";

		}
	},
			"Changes the angle at which---you view the game field");


	Button mouseTarget = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Mouse target: on", new Runnable()
	{
		@Override
		public void run() 
		{
			Panel.showMouseTarget = !Panel.showMouseTarget;

			if (Panel.showMouseTarget)
				mouseTarget.text = "Mouse target: on";
			else
				mouseTarget.text = "Mouse target: off";
			
			Game.game.window.setShowCursor(!Panel.showMouseTarget);
		}
	},
			"When enabled, your mouse pointer---will be replaced by a target");

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

	Button autostart = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Autostart: on", new Runnable()
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

	Button vsync = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 0, 350, 40, "V-Sync: on", new Runnable()
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

	Button showStats = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Info bar: on", new Runnable()
	{
		@Override
		public void run()
		{
			Drawing.drawing.showStats(!Drawing.drawing.enableStats);

			if (Drawing.drawing.enableStats)
				showStats.text = "Info bar: on";
			else
				showStats.text = "Info bar: off";
		}
	},
			"Shows the following information---" +
					"at the bottom of the screen:---" +
					"---" +
					"Game version---" +
					"Framerate---" +
					"Network latency (if in a party)---" +
					"Screen hints---" +
					"Memory usage");


	Button multiplayerOptions = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, 350, 40, "Multiplayer options", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenMultiplayerOptions();
		}
	}
			, "Options for username and chat filter");
	
	
	@Override
	public void update()
	{
		autostart.update();
		mouseTarget.update();
		graphics.update();
		graphics3d.update();
		ground3d.update();
		altPerspective.update();
		vsync.update();
		showStats.update();
		multiplayerOptions.update();
		back.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		back.draw();
		multiplayerOptions.draw();
		showStats.draw();
		autostart.draw();
		mouseTarget.draw();
		vsync.draw();
		altPerspective.draw();
		graphics3d.draw();
		ground3d.draw();
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
			writer.println("username=" + Game.player.username);
			writer.println("fancy_graphics=" + Game.fancyGraphics);
			writer.println("3d=" + Game.enable3d);
			writer.println("3d_ground=" + Game.enable3dBg);
			writer.println("angled_perspective=" + Game.angledView);
			writer.println("mouse_target=" + Panel.showMouseTarget);
			writer.println("auto_start=" + Game.autostart);
			writer.println("vsync=" + Game.vsync);
			writer.println("info_bar=" + Drawing.drawing.enableStats);
			writer.println("port=" + Game.port);
			writer.println("last_party=" + Game.lastParty);
			writer.println("chat_filter=" + Game.enableChatFilter);
			writer.println("use_custom_tank_registry=" + Game.enableCustomTankRegistry);
			writer.println("use_custom_obstacle_registry=" + Game.enableCustomObstacleRegistry);
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

				switch (optionLine[0].toLowerCase())
				{
					case "username":
						if (optionLine.length >= 2)
							Game.player.username = optionLine[1];
						else
							Game.player.username = "";
						break;
					case "fancy_graphics":
						Game.fancyGraphics = Boolean.parseBoolean(optionLine[1]);
						break;
					case "3d":
						Game.enable3d = Boolean.parseBoolean(optionLine[1]);
						break;
					case "3d_ground":
						Game.enable3dBg = Boolean.parseBoolean(optionLine[1]);
						break;
					case "mouse_target":
						Panel.showMouseTarget = Boolean.parseBoolean(optionLine[1]);
						break;
					case "auto_start":
						Game.autostart = Boolean.parseBoolean(optionLine[1]);
						break;
					case "info_bar":
						Drawing.drawing.showStats(Boolean.parseBoolean(optionLine[1]));
						break;
					case "angled_perspective":
						Game.angledView = Boolean.parseBoolean(optionLine[1]);
						break;
					case "vsync":
						Game.vsync = Boolean.parseBoolean(optionLine[1]);
						break;
					case "port":
						Game.port = Integer.parseInt(optionLine[1]);
						break;
					case "last_party":
						if (optionLine.length >= 2)
							Game.lastParty = optionLine[1];
						else
							Game.lastParty = "";
						break;
					case "chat_filter":
						Game.enableChatFilter = Boolean.parseBoolean(optionLine[1]);
						break;
					case "use_custom_tank_registry":
						Game.enableCustomTankRegistry = Boolean.parseBoolean(optionLine[1]);
						break;
					case "use_custom_obstacle_registry":
						Game.enableCustomObstacleRegistry = Boolean.parseBoolean(optionLine[1]);
						break;
				}
			}
			in.close();
		} 
		catch (Exception e)
		{
			Game.logger.println (new Date().toString() + " (syswarn) options file is nonexistent or broken, using default:");
			e.printStackTrace(Game.logger);
		}
	}
}
