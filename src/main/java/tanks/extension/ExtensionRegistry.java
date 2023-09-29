package tanks.extension;

import basewindow.BaseFile;
import tanks.Game;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.jar.JarFile;

public class ExtensionRegistry
{
	public ArrayList<Extension> extensions = new ArrayList<>();

	public void loadRegistry()
	{
		Game.extensionRegistry.extensions.clear();

		String path = Game.homedir + Game.extensionRegistryPath;

		if (Game.enableExtensions)
		{
			try
			{
				BaseFile in = Game.game.fileManager.getFile(path);
				in.startReading();

				while (in.hasNextLine())
				{
					String line = in.nextLine();

					if (line == null || line.length() == 0)
						continue;

					String[] extensionLine = line.split(",");

					if (extensionLine[0].charAt(0) == '#')
						continue;

					if (extensionLine.length > 1)
						loadExtension(extensionLine[0], extensionLine[1]);
					else
						loadExtension(extensionLine[0], null);
				}

				in.stopReading();
			}
			catch (Exception e)
			{
				Game.exitToCrash(e);
			}

			try
			{
				if (Game.autoLoadExtensions)
				{
					ArrayList<String> files = Game.game.fileManager.getFile(Game.homedir + Game.extensionDir).getSubfiles();

					for (String file : files)
					{
						loadExtension(file.substring(file.replace("\\", "/").lastIndexOf("/") + 1), null);
					}
				}
			}
			catch (Exception e)
			{
				Game.exitToCrash(e);
			}
		}
	}

	public void loadExtension(String jar, String main) throws Exception
	{
		ClassLoader loader = new URLClassLoader(new URL[]{new File(Game.homedir + Game.extensionDir + jar).toURI().toURL()});

		if (main != null)
		{
			JarFile f = new JarFile(Game.homedir + Game.extensionDir + jar);
			Class<? extends Extension> clasz = (Class<? extends Extension>) loader.loadClass(main);
			Extension e = clasz.getConstructor().newInstance();
			e.jarFile = f;
			this.extensions.add(e);
		}
		else
		{
			try
			{
				JarFile f = new JarFile(Game.homedir + Game.extensionDir + jar);
				InputStream i = f.getInputStream(f.getEntry("extension.txt"));

				if (i != null)
				{
					try
					{
						Scanner s = new Scanner(new InputStreamReader(i));
						Class<? extends Extension> clasz = (Class<? extends Extension>) loader.loadClass(s.nextLine());
						Extension e = clasz.getConstructor().newInstance();
						e.jarFile = f;
						this.extensions.add(e);
					}
					catch (Exception e)
					{
						System.err.println("Failed to load extension " + jar);
						e.printStackTrace();
					}
				}
			}
			catch (Exception ignored) { }
		}
	}

	public void initRegistry()
	{
		String path = Game.homedir + Game.extensionRegistryPath;

		try 
		{
			Game.game.fileManager.getFile(path).create();

			BaseFile f = Game.game.fileManager.getFile(path);
			f.startWriting();
			f.println("# Warning!");
			f.println("# 1. Loading Tanks extensions can potentially break the game or infect your computer with a virus.");
			f.println("# Please be careful of what you download and add as an extension.");
			f.println("# 2. To enable loading extensions from this file, set enable-extensions in options.txt to true");
			f.println("# ");
			f.println("# This is the Extension Registry file!");
			f.println("# A registry entry is a line in the file");
			f.println("# A line is composed of 2 things: jar name, and class");
			f.println("# ");
			f.println("# To create an extension, import the 'Tanks' jar into a java project,");
			f.println("# write a class extending Extension, add your code to the setUp() method,");
			f.println("# and export as a jar file.");
			f.println("# ");
			f.println("# To load an extension, put the jar file inside the \"extensions\" folder,");
			f.println("# Then, add a line to the end of this file as shown below");
			f.println("# MyExtension.jar,com.potato.MyExtension'");
			f.println("# (Extensions will be loaded in the order listed here)");

			f.stopWriting();
		} 
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}
}
