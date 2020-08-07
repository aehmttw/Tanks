package tanks.registry;

import basewindow.BaseFile;
import tanks.Game;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleUnknown;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;

public class RegistryObstacle 
{
	public ArrayList<ObstacleEntry> obstacleEntries = new ArrayList<ObstacleEntry>();

	public static void loadRegistry(String homedir) 
	{
		Game.registryObstacle.obstacleEntries.clear();

		String path = homedir + Game.obstacleRegistryPath;

		boolean loadRegistry = Game.enableCustomObstacleRegistry;

		if (loadRegistry)
		{
			try 
			{
				BaseFile in = Game.game.fileManager.getFile(path);
				in.startReading();
				while (in.hasNextLine()) 
				{
					String line = in.nextLine();
					String[] obstacleLine = line.split(",");

					if (obstacleLine[0].charAt(0) == '#') 
					{ 
						continue; 
					}
					if (obstacleLine[1].toLowerCase().equals("default")) 
					{
						boolean foundObstacle = false;
						for (int i = 0; i < Game.defaultObstacles.size(); i++)
						{
							if (obstacleLine[0].equals(Game.defaultObstacles.get(i).name))
							{
								Game.defaultObstacles.get(i).registerEntry(Game.registryObstacle);
								foundObstacle = true;
								break;
							}
						}

						if (!foundObstacle)
							Game.logger.println (new Date().toString() + " (syswarn) the default obstacle '" + obstacleLine[0] + "' does not exist!");
					}
					else 
					{
						try 
						{
							@SuppressWarnings("resource")
							ClassLoader loader = new URLClassLoader( new URL[] { new File(obstacleLine[2]).toURI().toURL() }); // super messy
							@SuppressWarnings("unchecked")
							Class<? extends Obstacle> clasz = (Class<? extends Obstacle>) loader.loadClass(obstacleLine[3]);
							new ObstacleEntry(Game.registryObstacle, clasz, obstacleLine[0]);
						}
						catch (Exception e) 
						{
							e.printStackTrace();
							Game.logger.println(new Date().toString() + " (syswarn) error loading custom obstacle '" + obstacleLine[0] + "'. try adding the path to your jvm classpath. ignoring.");
						}
					}
				}
				in.stopReading();
			} 
			catch (Exception e)
			{
				Game.logger.println (new Date().toString() + " (syswarn) obstacle registry file is nonexistent or broken, using default:");
				e.printStackTrace(Game.logger);

				loadRegistry = false;
			}
		}
		
		if (!loadRegistry)
		{
			for (int i = 0; i < Game.defaultObstacles.size(); i++)
			{
				Game.defaultObstacles.get(i).registerEntry(Game.registryObstacle);
			}
		}
	}

	public static void initRegistry(String homedir) 
	{
		String path = homedir + Game.obstacleRegistryPath;
		try 
		{
			Game.game.fileManager.getFile(path).create();
		}
		catch (IOException e) 
		{
			Game.logger.println (new Date().toString() + " (syserr) file permissions are broken! cannot initialize obstacle registry.");
			System.exit(1);
		}
		try 
		{
			BaseFile f = Game.game.fileManager.getFile(path);
			f.startWriting();
			f.println("# Warning! To use a custom Obstacle Registry, you MUST set use-custom-obstacle-registry ");
			f.println("# in options.txt from false to true!");
			f.println("# ");
			f.println("# This is the Obstacle Registry file!");
			f.println("# A registry entry is a line in the file");
			f.println("# The parameters are name, custom/default, jar location, and class");
			f.println("# Built in obstacles do not use the last 2 parameters");
			f.println("# and have 'default' written for the third parameter");
			f.println("# To make a custom obstacle, import the 'Tanks' jar into a java project,");
			f.println("# write a class extending Obstacle, and export as a jar file.");
			f.println("# To import a custom obstacle, put the jar file somewhere on your computer,");
			f.println("# put 'custom' for parameter 2");
			f.println("# and put its absolute file path as parameter 3 in this file.");
			f.println("# Then, put a comma and write the Class name with package and all as parameter 5.");
			f.println("# Example custom obstacle entry: 'myobstacle,1,custom,C:\\Users\\potato\\.tanks\\MyObstacle.jar,com.potato.MyObstacle'");
			f.println("# Don't leave any blank lines!");

			for (int i = 0; i < Game.defaultObstacles.size(); i++)
			{
				f.println(Game.defaultObstacles.get(i).getString());
			}

			f.stopWriting();
		} 
		catch (Exception e)
		{
			Game.logger.println(new Date().toString() + " (syserr) something broke! could not initialize obstacle registry:");
			e.printStackTrace(Game.logger);
			System.exit(1);
		}

	}

	public static class ObstacleEntry
	{
		public final Class<? extends Obstacle> obstacle;
		public final String name;

		public ObstacleEntry(RegistryObstacle r, Class<? extends Obstacle> obstacle, String name)
		{
			this.obstacle = obstacle;
			this.name = name;

			r.obstacleEntries.add(this);
		}

		protected ObstacleEntry()
		{
			this.obstacle = ObstacleUnknown.class;
			this.name = "unknown";
		}

		protected ObstacleEntry(String name)
		{
			this.obstacle = ObstacleUnknown.class;
			this.name = name;
		}

		public Obstacle getObstacle(double x, double y)
		{
			try 
			{
				return obstacle.getConstructor(String.class, double.class, double.class).newInstance(this.name, x, y);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) 
			{
				e.printStackTrace();
				return null;
			}
		}

		public static ObstacleEntry getUnknownEntry()
		{
			return new ObstacleEntry();
		}

		public static ObstacleEntry getUnknownEntry(String name)
		{
			return new ObstacleEntry(name);
		}
	}

	public static class DefaultObstacleEntry
	{
		public final Class<? extends Obstacle> obstacle;
		public final String name;

		public DefaultObstacleEntry(Class<? extends Obstacle> obstacle, String name)
		{
			this.obstacle = obstacle;
			this.name = name;
		}

		public void registerEntry(RegistryObstacle r)
		{
			new ObstacleEntry(r, this.obstacle, this.name);
		}

		public String getString()
		{
			return this.name + ",default";
		}
	}

	public ObstacleEntry getEntry(String name)
	{		
		for (int i = 0; i < obstacleEntries.size(); i++)
		{
			ObstacleEntry r = obstacleEntries.get(i);

			if (r.name.equals(name))
			{
				return r;
			}
		}

		return ObstacleEntry.getUnknownEntry(name);
	}

	public ObstacleEntry getEntry(int number)
	{		
		return obstacleEntries.get(number);
	}
}
