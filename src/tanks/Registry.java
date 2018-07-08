package tanks;

public class Registry 
{
	static class TankRegistry
	{
		Class<? extends Tank> tank;
		String name;
		int weight;
		
		public TankRegistry(Class<? extends Tank> tank, String name, int weight)
		{
			this.tank = tank;
			this.name = name;
			this.weight = weight;
		}
	}
}
