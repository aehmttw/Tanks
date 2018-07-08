package tanks;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class Registry 
{
	public ArrayList<TankRegistry> tankRegistries = new ArrayList<TankRegistry>();
	protected double maxTankWeight = 0;
	
	static class TankRegistry
	{
		public final Class<? extends Tank> tank;
		public final String name;
		public final double weight;
	
		protected double startWeight;
		protected double endWeight;
		
		public TankRegistry(Registry r, Class<? extends Tank> tank, String name, double weight)
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
	
	public TankRegistry getRandomTank()
	{
		double random = Math.random() * maxTankWeight;
		
		for (int i = 0; i < tankRegistries.size(); i++)
		{
			TankRegistry r = tankRegistries.get(i);
			
			if (random >= r.startWeight && random < r.endWeight)
			{
				return r;
			}
		}
		
		return null;
	}
	
	public TankRegistry getRegistry(String name)
	{		
		for (int i = 0; i < tankRegistries.size(); i++)
		{
			TankRegistry r = tankRegistries.get(i);
			
			if (r.name.equals(name))
			{
				return r;
			}
		}
		
		return null;
	}
}
