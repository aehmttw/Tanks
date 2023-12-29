package tanks.network;

import tanks.Game;
import tanks.network.event.INetworkEvent;

import java.util.HashMap;

public class NetworkEventMap 
{
	protected static HashMap<Integer, Class<? extends INetworkEvent>> map1 = new HashMap<>();
	protected static HashMap<Class<? extends INetworkEvent>, Integer> map2 = new HashMap<>();
	protected static int id = 0;
	
	public static void register(Class<? extends INetworkEvent> c)
	{
		try
		{
			c.getConstructor();
		}
		catch (Exception e)
		{
			Game.exitToCrash(new RuntimeException("The network event " + c + " does not have a no-parameter constructor. Please give it one."));
		}

		map1.put(id, c);
		map2.put(c, id);
		id++;
	}
	
	public static int get(Class<? extends INetworkEvent> c)
	{
		Integer i = map2.get(c);

		if (i == null)
			return -1;

		return i;
	}
	
	public static Class<? extends INetworkEvent> get(int i)
	{
		return map1.get(i);
	}

	public static void print()
	{
		for (int i = 0; i < id; i++)
		{
			System.out.println(i + " " + NetworkEventMap.get(i));
		}
	}
}
