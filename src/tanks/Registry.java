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
	
	public static void loadRegistry (String homedir) {
		String path = homedir + "/.tanks.d/tank-registry.lmao";
		try {
			Scanner in = new Scanner (new File (path));
			while (in.hasNextLine()) {
				String[] tankLine = in.nextLine().split(",");
				if (tankLine[0].charAt(0) == '#') { continue; }
				if (tankLine[2].toLowerCase().equals("default")) {
					switch (tankLine[0]) {
						case "brown":
							new Registry.TankEntry(Game.registry, EnemyTankBrown.class, "brown", Double.parseDouble(tankLine[1]));
							break;
						case "gray":
							new Registry.TankEntry(Game.registry, EnemyTankGray.class, "gray", Double.parseDouble(tankLine[1]));
							break;
						case "mint":
							new Registry.TankEntry(Game.registry, EnemyTankMint.class, "mint", Double.parseDouble(tankLine[1]));
							break;
						case "yellow":
							new Registry.TankEntry(Game.registry, EnemyTankYellow.class, "yellow", Double.parseDouble(tankLine[1]));
							break;
						case "magenta":
							new Registry.TankEntry(Game.registry, EnemyTankMagenta.class, "magenta", Double.parseDouble(tankLine[1]));
							break;
						case "red":
							new Registry.TankEntry(Game.registry, EnemyTankRed.class, "red", Double.parseDouble(tankLine[1]));
							break;
						case "green":
							new Registry.TankEntry(Game.registry, EnemyTankGreen.class, "green", Double.parseDouble(tankLine[1]));
							break;
						case "purple":
							new Registry.TankEntry(Game.registry, EnemyTankPurple.class, "purple", Double.parseDouble(tankLine[1]));
							break;
						case "white":
							new Registry.TankEntry(Game.registry, EnemyTankWhite.class, "white", Double.parseDouble(tankLine[1]));
							break;
						case "orange":
							new Registry.TankEntry(Game.registry, EnemyTankOrange.class, "orange", Double.parseDouble(tankLine[1]));
							break;
						case "darkgreen":
							new Registry.TankEntry(Game.registry, EnemyTankDarkGreen.class, "darkgreen", Double.parseDouble(tankLine[1]));
							break;
						case "black":
							new Registry.TankEntry(Game.registry, EnemyTankBlack.class, "black", Double.parseDouble(tankLine[1]));
							break;
						case "pink":
							new Registry.TankEntry(Game.registry, EnemyTankPink.class, "pink", Double.parseDouble(tankLine[1]));
							break;
						default:
							Game.logger.println (new Date().toString() + " (syswarn) no such default tank '" + tankLine[0] + "' exists. ignoring.");
							break;
					}
				} else {
					try {
						@SuppressWarnings("resource")
						ClassLoader loader = new URLClassLoader ( new URL[] { new File(tankLine[3]).toURI().toURL() }); // super messy
						@SuppressWarnings("unchecked")
						Class<? extends Tank> clasz = (Class<? extends Tank>) loader.loadClass(tankLine[4]);
						new Registry.TankEntry(Game.registry, clasz, tankLine[0], Double.parseDouble(tankLine[1]));
					} catch (Exception e) {
						e.printStackTrace();
						Game.logger.println (new Date().toString() + " (syswarn) error loading custom tank '" + tankLine[3] + "'. try adding the path to your jvm classpath. ignoring.");
					}
				}
			}
			in.close();
		} catch (Exception e) {
			Game.logger.println (new Date().toString() + " (syswarn) tank-registry.lmao nonexistent or broken, using default.");
			new Registry.TankEntry(Game.registry, EnemyTankBrown.class, "brown", 1);
			new Registry.TankEntry(Game.registry, EnemyTankGray.class, "gray", 1);
			new Registry.TankEntry(Game.registry, EnemyTankMint.class, "mint", 1.0 / 2);
			new Registry.TankEntry(Game.registry, EnemyTankYellow.class, "yellow", 1.0 / 2);
			new Registry.TankEntry(Game.registry, EnemyTankMagenta.class, "magenta", 1.0 / 3);
			new Registry.TankEntry(Game.registry, EnemyTankRed.class, "red", 1.0 / 3);
			new Registry.TankEntry(Game.registry, EnemyTankGreen.class, "green", 1.0 / 4);
			new Registry.TankEntry(Game.registry, EnemyTankPurple.class, "purple", 1.0 / 4);
			new Registry.TankEntry(Game.registry, EnemyTankWhite.class, "white", 1.0 / 4);
			new Registry.TankEntry(Game.registry, EnemyTankOrange.class, "orange", 1.0 / 6);
			new Registry.TankEntry(Game.registry, EnemyTankDarkGreen.class, "darkgreen", 1.0 / 9);
			new Registry.TankEntry(Game.registry, EnemyTankBlack.class, "black", 1.0 / 10);
			new Registry.TankEntry(Game.registry, EnemyTankPink.class, "pink", 1.0 / 15);
		}
	}
	
	public static void initRegistry (String homedir) {
		String path = homedir + "/.tanks.d/tank-registry.lmao";
		try {
			new File (path).createNewFile();
		} catch (IOException e) {
			Game.logger.println (new Date().toString() + " (syserr) file permissions are screwed up! cannot initialize tank registry.");
			System.exit(1);
		}
		try {
			PrintStream writer = new PrintStream (new File (path));
			writer.println("brown,"+String.valueOf(1)+",default");
			writer.println("gray,"+String.valueOf(1)+",default");
			writer.println("mint,"+String.valueOf(1.0/2)+",default");
			writer.println("yellow,"+String.valueOf(1.0/2)+",default");
			writer.println("magenta,"+String.valueOf(1.0/3)+",default");
			writer.println("red,"+String.valueOf(1.0/3)+",default");
			writer.println("green,"+String.valueOf(1.0/4)+",default");
			writer.println("purple,"+String.valueOf(1.0/4)+",default");
			writer.println("white,"+String.valueOf(1.0/4)+",default");
			writer.println("orange,"+String.valueOf(1.0/6)+",default");
			writer.println("darkgreen,"+String.valueOf(1.0/9)+",default");
			writer.println("black,"+String.valueOf(1.0/10)+",default");
			writer.println("pink,"+String.valueOf(1.0/15)+",default");
		} catch (Exception e) {
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
	
	public TankEntry getRandomTank()
	{
		double random = Math.random() * maxTankWeight;
		System.out.println(random);
		for (int i = 0; i < tankRegistries.size(); i++)
		{
			TankEntry r = tankRegistries.get(i);
			System.out.println(r.name);
			System.out.println(r.startWeight + " " + r.endWeight);
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
