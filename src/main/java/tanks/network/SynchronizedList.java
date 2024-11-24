package tanks.network;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class SynchronizedList<T> extends ArrayList<T>
{
	public boolean add(T e)
	{
		synchronized (this)
		{
			return super.add(e);
		}
	}
	
	public void add(int i, T e)
	{
		synchronized (this)
		{
			super.add(i, e);
		}
	}

	public T get(int i)
	{
		synchronized (this)
		{
			return super.get(i);
		}
	}
	
	public T remove(int i)
	{
		synchronized (this)
		{
			return super.remove(i);
		}
	}
	
	public T set(int i, T e)
	{
		synchronized (this)
		{
			return super.set(i, e);
		}
	}
	
	public void clear()
	{
		synchronized (this)
		{
			super.clear();
		}
	}
}
