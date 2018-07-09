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

public class Registry 
{
	
	public ArrayList<TankEntry> tankRegistries = new ArrayList<TankEntry>();
	protected double maxTankWeight = 0;
	
	public static void loadRegistry (String homedir) 
	{
		String path = homedir + Game.registryPath;
		try 
		{
			Scanner in = new Scanner (new File (path));
			while (in.hasNextLine()) 
			{
				String[] tankLine = in.nextLine().split(",");
				if (tankLine[0].charAt(0) == '#') 
				{ 
					continue; 
				}
				if (tankLine[2].toLowerCase().equals("default")) 
				{
					for (int i = 0; i < Game.defaultTanks.size(); i++)
					{
						if (tankLine[0].equals(Game.defaultTanks.get(i).name))
						{
							Game.defaultTanks.get(i).registerEntry(Game.registry);
							break;
						}
						Game.logger.println (new Date().toString() + " (syswarn) the default tank [" + tankLine[0] + "] does not exist!");
					}
				}
				else 
				{
					try 
					{
						@SuppressWarnings("resource")
						ClassLoader loader = new URLClassLoader ( new URL[] { new File(tankLine[3]).toURI().toURL() }); // super messy
						@SuppressWarnings("unchecked")
						Class<? extends Tank> clasz = (Class<? extends Tank>) loader.loadClass(tankLine[4]);
						new Registry.TankEntry(Game.registry, clasz, tankLine[0], Double.parseDouble(tankLine[1]));
					}
					catch (Exception e) 
					{
						e.printStackTrace();
						Game.logger.println (new Date().toString() + " (syswarn) error loading custom tank '" + tankLine[3] + "'. try adding the path to your jvm classpath. ignoring.");
					}
				}
			}
			in.close();
		} 
		catch (Exception e)
		{
			Game.logger.println (new Date().toString() + " (syswarn) tank-registry file is nonexistent or broken, using default.");
			
			for (int i = 0; i < Game.defaultTanks.size(); i++)
			{
				Game.defaultTanks.get(i).registerEntry(Game.registry);
			}
		}
	}
	
	public static void initRegistry (String homedir) 
	{
		String path = homedir + Game.registryPath;
		try 
		{
			new File (path).createNewFile();
		}
		catch (IOException e) 
		{
			Game.logger.println (new Date().toString() + " (syserr) file permissions are broken! Cannot initialize tank registry.");
			System.exit(1);
		}
		try 
		{
			PrintStream writer = new PrintStream (new File (path));
			
			for (int i = 0; i < Game.defaultTanks.size(); i++)
			{
				writer.println(Game.defaultTanks.get(i).getString());
			}
		} 
		catch (Exception e)
		{
			Game.logger.println (new Date().toString() + " (syserr) something broke. cannot initialize tank registry.");
			System.exit(1);
		}
		
	}
	
	static class TankEntry
	{
		public final Class<? extends Tank> tank;
		public final String name;
		public final double weight;
	
		protected double startWeight;
		protected double endWeight;
		
		public TankEntry(Registry r, Class<? extends Tank> tank, String name, double weight)
		{
			this.tank = tank;
			this.name = name;
			this.weight = weight;
	
			this.startWeight = r.maxTankWeight;
			r.maxTankWeight += weight;
			this.endWeight = r.maxTankWeight;
			
			r.tankRegistries.add(this);
		}
		
		public Tank getTank(double x, double y, double a)
		{
			try 
			{
				return tank.getConstructor(double.class, double.class, double.class).newInstance(x, y, a);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) 
			{
				e.printStackTrace();
				return null;
			}
		}
	}
	
	static class DefaultTankEntry
	{
		public final Class<? extends Tank> tank;
		public final String name;
		public final double weight;
	
		protected double startWeight;
		protected double endWeight;
		
		public DefaultTankEntry(Class<? extends Tank> tank, String name, double weight)
		{
			this.tank = tank;
			this.name = name;
			this.weight = weight;
		}
		
		public TankEntry registerEntry(Registry r)
		{
			return new TankEntry(r, this.tank, this.name, this.weight);
		}
		
		public String getString()
		{
			return this.name + "," + this.weight + ",default";
		}
	}
	
	public TankEntry getRandomTank()
	{
		double random = Math.random() * maxTankWeight;
		for (int i = 0; i < tankRegistries.size(); i++)
		{
			TankEntry r = tankRegistries.get(i);

			if (random >= r.startWeight && random < r.endWeight)
			{
				return r;
			}
		}
		
		return null;
	}
	
	public TankEntry getRegistry(String name)
	{		
		for (int i = 0; i < tankRegistries.size(); i++)
		{
			TankEntry r = tankRegistries.get(i);
			
			if (r.name.equals(name))
			{
				return r;
			}
		}
		
		return null;
	}
}
