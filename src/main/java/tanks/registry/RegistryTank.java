package tanks.registry;

import basewindow.BaseFile;
import tanks.Game;
import tanks.tank.Tank;
import tanks.tank.TankUnknown;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;

public class RegistryTank 
{
	public ArrayList<TankEntry> tankEntries = new ArrayList<TankEntry>();
	protected double maxTankWeight = 0;

	public static void loadRegistry(String homedir) 
	{
		Game.registryTank.tankEntries.clear();
		Game.registryTank.maxTankWeight = 0;

		String path = homedir + Game.tankRegistryPath;

		boolean loadRegistry = Game.enableCustomTankRegistry;

		if (loadRegistry)
		{
			try 
			{
				BaseFile in = Game.game.fileManager.getFile(path);
				in.startReading();
				while (in.hasNextLine())
				{
					String line = in.nextLine();
					String[] tankLine = line.split(",");

					if (tankLine[0].charAt(0) == '#') 
					{ 
						continue; 
					}
					if (tankLine[2].toLowerCase().equals("default")) 
					{
						boolean foundTank = false;
						for (int i = 0; i < Game.defaultTanks.size(); i++)
						{
							if (tankLine[0].equals(Game.defaultTanks.get(i).name))
							{
								Game.defaultTanks.get(i).registerEntry(Game.registryTank, Double.parseDouble(tankLine[1]));
								foundTank = true;
								break;
							}
						}

						if (!foundTank)
							Game.logger.println (new Date().toString() + " (syswarn) the default tank '" + tankLine[0] + "' does not exist!");
					}
					else 
					{
						try 
						{
							@SuppressWarnings("resource")
							ClassLoader loader = new URLClassLoader( new URL[] { new File(tankLine[3]).toURI().toURL() }); // super messy
							@SuppressWarnings("unchecked")
							Class<? extends Tank> clasz = (Class<? extends Tank>) loader.loadClass(tankLine[4]);
							new TankEntry(Game.registryTank, clasz, tankLine[0], Double.parseDouble(tankLine[1]));
						}
						catch (Exception e) 
						{
							e.printStackTrace();
							Game.logger.println(new Date().toString() + " (syswarn) error loading custom tank '" + tankLine[0] + "'. try adding the path to your jvm classpath. ignoring.");
						}
					}
				}
				in.stopReading();
			} 
			catch (Exception e)
			{
				Game.logger.println (new Date().toString() + " (syswarn) tank registry file is nonexistent or broken, using default:");
				e.printStackTrace(Game.logger);
				loadRegistry = false;
			}
		}

		if (!loadRegistry)
		{
			for (int i = 0; i < Game.defaultTanks.size(); i++)
			{
				Game.defaultTanks.get(i).registerEntry(Game.registryTank);
			}
		}
	}

	public static void initRegistry(String homedir) 
	{
		String path = homedir + Game.tankRegistryPath;
		try 
		{
			Game.game.fileManager.getFile(path).create();
		}
		catch (IOException e) 
		{
			Game.logger.println (new Date().toString() + " (syserr) file permissions are broken! cannot initialize tank registry.");
			System.exit(1);
		}
		try 
		{
			BaseFile f = Game.game.fileManager.getFile(path);
			f.startWriting();
			f.println("# Warning! To use a custom Tank Registry, you MUST set use-custom-tank-registry ");
			f.println("# in options.txt from false to true!");
			f.println("# ");
			f.println("# This is the Tank Registry file!");
			f.println("# A registry entry is a line in the file");
			f.println("# The parameters are name, rarity, custom/default, jar location, and class");
			f.println("# Built in tanks do not use the last 2 parameters");
			f.println("# and have 'default' written for the third parameter");
			f.println("# To make a custom tank, import the 'Tanks' jar into a java project,");
			f.println("# write a class extending Tank or EnemyTank, and export as a jar file.");
			f.println("# To import a custom tank, put the jar file somewhere on your computer,");
			f.println("# put 'custom' for parameter 3");
			f.println("# and put its absolute file path as parameter 4 in this file.");
			f.println("# Then, put a comma and write the Class name with package and all as parameter 5.");
			f.println("# Example custom tank entry: 'mytank,1,custom,C:\\Users\\potato\\.tanks.d\\MyTank.jar,com.potato.MyTank'");
			f.println("# Don't leave any blank lines!");

			for (int i = 0; i < Game.defaultTanks.size(); i++)
			{
				f.println(Game.defaultTanks.get(i).getString());
			}

			f.stopWriting();
		} 
		catch (Exception e)
		{
			Game.logger.println(new Date().toString() + " (syserr) something broke! could not initialize tank registry:");
			e.printStackTrace(Game.logger);
			System.exit(1);
		}

	}

	public static class TankEntry
	{
		public final Class<? extends Tank> tank;
		public final String name;
		public final double weight;

		protected double startWeight;
		protected double endWeight;

		public TankEntry(RegistryTank r, Class<? extends Tank> tank, String name, double weight)
		{
			this.tank = tank;
			this.name = name;
			this.weight = weight;

			this.startWeight = r.maxTankWeight;
			r.maxTankWeight += weight;
			this.endWeight = r.maxTankWeight;

			r.tankEntries.add(this);
		}

		protected TankEntry()
		{
			this.tank = TankUnknown.class;
			this.name = "unknown";
			this.weight = 0;
		}

		protected TankEntry(String name)
		{
			this.tank = TankUnknown.class;
			this.name = name;
			this.weight = 0;
		}

		public Tank getTank(double x, double y, double a)
		{
			try 
			{
				return tank.getConstructor(String.class, double.class, double.class, double.class).newInstance(this.name, x, y, a);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) 
			{
				e.printStackTrace();
				return null;
			}
		}

		public static TankEntry getUnknownEntry()
		{
			return new TankEntry();
		}

		public static TankEntry getUnknownEntry(String name)
		{
			return new TankEntry(name);
		}
	}

	public static class DefaultTankEntry
	{
		public final Class<? extends Tank> tank;
		public final String name;
		public final double weight;

		public DefaultTankEntry(Class<? extends Tank> class1, String name, double weight)
		{
			this.tank = class1;
			this.name = name;
			this.weight = weight;
		}

		public void registerEntry(RegistryTank r)
		{
			new TankEntry(r, this.tank, this.name, this.weight);
		}

		public void registerEntry(RegistryTank r, double weight)
		{
			new TankEntry(r, this.tank, this.name, weight);
		}

		public String getString()
		{
			return this.name + "," + this.weight + ",default";
		}
	}

	public TankEntry getRandomTank()
	{
		if (this.tankEntries.size() <= 0)
			throw new RuntimeException("the tank registry file is empty. please register some tanks!");

		double random = Math.random() * maxTankWeight;
		for (int i = 0; i < tankEntries.size(); i++)
		{
			TankEntry r = tankEntries.get(i);

			if (random >= r.startWeight && random < r.endWeight)
			{
				return r;
			}
		}

		return TankEntry.getUnknownEntry();
	}

	public TankEntry getEntry(String name)
	{		
		for (int i = 0; i < tankEntries.size(); i++)
		{
			TankEntry r = tankEntries.get(i);

			if (r.name.equals(name))
			{
				return r;
			}
		}

		return TankEntry.getUnknownEntry(name);
	}

	public TankEntry getEntry(int number)
	{		
		return tankEntries.get(number);
	}
}
