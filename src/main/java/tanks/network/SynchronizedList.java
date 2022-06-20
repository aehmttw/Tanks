package tanks.network;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class SynchronizedList<T> extends ArrayList<T>
{
	public synchronized boolean add(T e)
	{
		return super.add(e);
	}
	
	public synchronized void add(int i, T e)
	{
		super.add(i, e);
	}

	public synchronized T get(int i)
	{
		return super.get(i);
	}
	
	public synchronized T remove(int i)
	{
		return super.remove(i);
	}
	
	public synchronized T set(int i, T e)
	{
		return super.set(i, e);
	}
	
	public synchronized void clear()
	{
		super.clear();
	}
}
