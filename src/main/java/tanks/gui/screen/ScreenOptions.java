package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.Selector;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerRemote;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

public class ScreenOptions extends Screen
{
	public static final String autostartText = "Autostart: ";
	public static final String infoBarText = "Info bar: ";

	public static final String onText = "\u00A7000200000255on";
	public static final String offText = "\u00A7200000000255off";

	public ScreenOptions()
	{
		this.music = "tomato_feast_1.ogg";
		this.musicID = "menu";

		if (Game.autostart)
			autostart.text = autostartText + onText;
		else
			autostart.text = autostartText + offText;

		if (Drawing.drawing.enableStats)
			showStats.text = infoBarText + onText;
		else
			showStats.text = infoBarText + offText;

		if (!Game.game.window.soundsEnabled)
			soundOptions.enabled = false;
	}

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

	Button autostart = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 0, 350, 40, "", new Runnable()
	{
		@Override
		public void run()
		{
			Game.autostart = !Game.autostart;

			if (Game.autostart)
				autostart.text = autostartText + onText;
			else
				autostart.text = autostartText + offText;
		}
	},
			"When enabled, levels will---start playing automatically---4 seconds after they are---loaded (if the play button---isn't clicked earlier)");


	Button showStats = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "", new Runnable()
	{
		@Override
		public void run()
		{
			Drawing.drawing.showStats(!Drawing.drawing.enableStats);

			if (Drawing.drawing.enableStats)
				showStats.text = infoBarText + onText;
			else
				showStats.text = infoBarText + offText;
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


	Button multiplayerOptions = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Multiplayer options", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenOptionsMultiplayer();
		}
	}
	);

	Button graphicsOptions = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Graphics options", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenOptionsGraphics();
		}
	}
	);

	Button soundOptions = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Sound options", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenOptionsSound();
		}
	}
	);

	Button inputOptions = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 0, 350, 40, "Input options", new Runnable()
	{
		@Override
		public void run()
		{
			if (Game.game.window.touchscreen)
				Game.screen = new ScreenOptionsInputTouchscreen();
			else
				Game.screen = new ScreenOptionsInputDesktop();
		}
	}
	);


	@Override
	public void update()
	{
		soundOptions.update();
		autostart.update();
		showStats.update();

		graphicsOptions.update();
		inputOptions.update();
		multiplayerOptions.update();

		back.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		back.draw();
		multiplayerOptions.draw();
		inputOptions.draw();
		graphicsOptions.draw();
		showStats.draw();
		autostart.draw();
		soundOptions.draw();

		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Options");
	}

	public static void initOptions(String homedir)
	{
		String path = homedir + Game.optionsPath;

		try
		{
			Game.game.fileManager.getFile(path).create();
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

		try
		{
			BaseFile f = Game.game.fileManager.getFile(path);
			f.startWriting();
			f.println("# This file stores game settings that you have set");
			f.println("username=" + Game.player.username);
			f.println("fancy_graphics=" + Game.fancyGraphics);
			f.println("super_graphics=" + Game.superGraphics);
			f.println("3d=" + Game.enable3d);
			f.println("3d_ground=" + Game.enable3dBg);
			f.println("vsync=" + Game.vsync);
			f.println("antialiasing=" + Game.antialiasing);
			f.println("angled_perspective=" + Game.angledView);
			f.println("mouse_target=" + Panel.showMouseTarget);
			f.println("vibrations=" + Game.enableVibrations);
			f.println("mobile_joystick=" + TankPlayer.controlStickMobile);
			f.println("snap_joystick=" + TankPlayer.controlStickSnap);
			f.println("dual_joystick=" + TankPlayer.shootStickEnabled);
			f.println("sound=" + Game.soundsEnabled);
			f.println("music=" + Game.musicEnabled);
			f.println("auto_start=" + Game.autostart);
			f.println("info_bar=" + Drawing.drawing.enableStats);
			f.println("port=" + Game.port);
			f.println("last_party=" + Game.lastParty);
			f.println("last_online_server=" + Game.lastOnlineServer);
			f.println("chat_filter=" + Game.enableChatFilter);
			f.println("anticheat=" + TankPlayerRemote.checkMotion);
			f.println("anticheat_weak=" + TankPlayerRemote.weakTimeCheck);
			f.println("tank_secondary_color=" + Game.player.enableSecondaryColor);
			f.println("tank_red=" + Game.player.colorR);
			f.println("tank_green=" + Game.player.colorG);
			f.println("tank_blue=" + Game.player.colorB);
			f.println("tank_red_2=" + Game.player.turretColorR);
			f.println("tank_green_2=" + Game.player.turretColorG);
			f.println("tank_blue_2=" + Game.player.turretColorB);
			f.println("use_custom_tank_registry=" + Game.enableCustomTankRegistry);
			f.println("use_custom_obstacle_registry=" + Game.enableCustomObstacleRegistry);
			f.stopWriting();
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
			BaseFile f = Game.game.fileManager.getFile(path);
			f.startReading();
			while (f.hasNextLine())
			{
				String line = f.nextLine();
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
					case "super_graphics":
						Game.superGraphics = Boolean.parseBoolean(optionLine[1]);
						break;
					case "3d":
						Game.enable3d = Boolean.parseBoolean(optionLine[1]);
						break;
					case "3d_ground":
						Game.enable3dBg = Boolean.parseBoolean(optionLine[1]);
						break;
					case "vsync":
						Game.vsync = Boolean.parseBoolean(optionLine[1]);
						break;
					case "antialiasing":
						Game.antialiasing = Boolean.parseBoolean(optionLine[1]);
						break;
					case "mouse_target":
						Panel.showMouseTarget = Boolean.parseBoolean(optionLine[1]);
						break;
					case "enable_vibrations":
						Game.enableVibrations = Boolean.parseBoolean(optionLine[1]);
						break;
					case "mobile_joystick":
						TankPlayer.controlStickMobile = Boolean.parseBoolean(optionLine[1]);
						break;
					case "snap_joystick":
						TankPlayer.controlStickSnap = Boolean.parseBoolean(optionLine[1]);
						break;
					case "dual_joystick":
						TankPlayer.setShootStick(Boolean.parseBoolean(optionLine[1]));
						break;
					case "sound":
						Game.soundsEnabled = Boolean.parseBoolean(optionLine[1]);
						break;
					case "music":
						Game.musicEnabled = Boolean.parseBoolean(optionLine[1]);
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
					case "port":
						Game.port = Integer.parseInt(optionLine[1]);
						break;
					case "last_party":
						if (optionLine.length >= 2)
							Game.lastParty = optionLine[1];
						else
							Game.lastParty = "";
						break;
					case "last_online_server":
						if (optionLine.length >= 2)
							Game.lastOnlineServer = optionLine[1];
						else
							Game.lastOnlineServer = "";
						break;
					case "chat_filter":
						Game.enableChatFilter = Boolean.parseBoolean(optionLine[1]);
						break;
					case "anticheat":
						TankPlayerRemote.checkMotion = Boolean.parseBoolean(optionLine[1]);
						break;
					case "anticheat_weak":
						TankPlayerRemote.weakTimeCheck = Boolean.parseBoolean(optionLine[1]);
						break;
					case "tank_secondary_color":
						Game.player.enableSecondaryColor = Boolean.parseBoolean(optionLine[1]);
						break;
					case "tank_red":
						Game.player.colorR = Integer.parseInt(optionLine[1]);
						break;
					case "tank_green":
						Game.player.colorG = Integer.parseInt(optionLine[1]);
						break;
					case "tank_blue":
						Game.player.colorB = Integer.parseInt(optionLine[1]);
						break;
					case "tank_red_2":
						Game.player.turretColorR = Integer.parseInt(optionLine[1]);
						break;
					case "tank_green_2":
						Game.player.turretColorG = Integer.parseInt(optionLine[1]);
						break;
					case "tank_blue_2":
						Game.player.turretColorB = Integer.parseInt(optionLine[1]);
						break;
					case "use_custom_tank_registry":
						Game.enableCustomTankRegistry = Boolean.parseBoolean(optionLine[1]);
						break;
					case "use_custom_obstacle_registry":
						Game.enableCustomObstacleRegistry = Boolean.parseBoolean(optionLine[1]);
						break;
				}
			}
			f.stopReading();

			if (Game.framework == Game.Framework.swing)
			{
				Game.vsync = false;
				Game.angledView = false;
				Game.enable3d = false;
				Game.enable3dBg = false;
				Game.superGraphics = false;
			}

			if (Game.framework == Game.Framework.libgdx)
			{
				Game.angledView = false;
				Panel.showMouseTarget = false;
				Game.vsync = true;
			}

			if (TankPlayerRemote.weakTimeCheck)
				TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatStrongTimeOffset;
			else
				TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatWeakTimeOffset;
		}
		catch (Exception e)
		{
			Game.logger.println (new Date().toString() + " (syswarn) options file is nonexistent or broken, using default:");
			e.printStackTrace(Game.logger);
		}
	}
}
