package tanks.registry;

import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleUnknown;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class RegistryObstacle 
{
	public ArrayList<ObstacleEntry> obstacleEntries = new ArrayList<>();

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
