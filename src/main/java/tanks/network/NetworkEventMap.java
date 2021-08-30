package tanks.network;

import tanks.event.INetworkEvent;

import java.util.HashMap;

public class NetworkEventMap 
{
	protected static HashMap<Integer, Class<? extends INetworkEvent>> map1 = new HashMap<Integer, Class<? extends INetworkEvent>>();
	protected static HashMap<Class<? extends INetworkEvent>, Integer> map2 = new HashMap<Class<? extends INetworkEvent>, Integer>();
	protected static int id = 0;
	
	public static void register(Class<? extends INetworkEvent> c)
	{
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
