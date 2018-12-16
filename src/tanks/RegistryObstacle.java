package tanks;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

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
				Scanner in = new Scanner(new File(path));
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
							new RegistryObstacle.ObstacleEntry(Game.registryObstacle, clasz, obstacleLine[0]);
						}
						catch (Exception e) 
						{
							e.printStackTrace();
							Game.logger.println(new Date().toString() + " (syswarn) error loading custom obstacle '" + obstacleLine[0] + "'. try adding the path to your jvm classpath. ignoring.");
						}
					}
				}
				in.close();
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
			new File(path).createNewFile();
		}
		catch (IOException e) 
		{
			Game.logger.println (new Date().toString() + " (syserr) file permissions are broken! cannot initialize obstacle registry.");
			System.exit(1);
		}
		try 
		{
			PrintStream writer = new PrintStream(new File(path));
			writer.println("# Warning! To use a custom Obstacle Registry, you MUST set use-custom-obstacle-registry ");
			writer.println("# in options.txt from false to true!");
			writer.println("# ");
			writer.println("# This is the Obstacle Registry file!");
			writer.println("# A registry entry is a line in the file");
			writer.println("# The parameters are name, custom/default, jar location, and class");
			writer.println("# Built in obstacles do not use the last 2 parameters");
			writer.println("# and have 'default' written for the third parameter");
			writer.println("# To make a custom obstacle, import the 'Tanks' jar into a java project,");
			writer.println("# write a class extending Obstacle, and export as a jar file.");
			writer.println("# To import a custom obstacle, put the jar file somewhere on your computer,");
			writer.println("# put 'custom' for parameter 2");
			writer.println("# and put its absolute file path as parameter 3 in this file.");
			writer.println("# Then, put a comma and write the Class name with package and all as parameter 5.");
			writer.println("# Example custom obstacle entry: 'myobstacle,1,custom,C:\\Users\\potato\\.tanks.d\\MyObstacle.jar,com.potato.MyObstacle'");
			writer.println("# Don't leave any blank lines!");

			for (int i = 0; i < Game.defaultObstacles.size(); i++)
			{
				writer.println(Game.defaultObstacles.get(i).getString());
			}
		} 
		catch (Exception e)
		{
			Game.logger.println(new Date().toString() + " (syserr) something broke! could not initialize obstacle registry:");
			e.printStackTrace(Game.logger);
			System.exit(1);
		}

	}

	static class ObstacleEntry
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

	static class DefaultObstacleEntry
	{
		public final Class<? extends Obstacle> obstacle;
		public final String name;

		public DefaultObstacleEntry(Class<? extends Obstacle> obstacle, String name)
		{
			this.obstacle = obstacle;
			this.name = name;
		}

		public ObstacleEntry registerEntry(RegistryObstacle r)
		{
			return new ObstacleEntry(r, this.obstacle, this.name);
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
