package tanks.registry;

import tanks.item.Item2;
import tanks.item.ItemEmpty2;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class RegistryItem
{
	public ArrayList<ItemEntry> itemEntries = new ArrayList<>();

	public static class ItemEntry
	{
		public final Class<? extends Item2> item;
		public final String name;
		public final String image;

		public ItemEntry(RegistryItem r, Class<? extends Item2> item, String name, String image)
		{
			this.item = item;
			this.name = name;
			this.image = image;
			r.itemEntries.add(this);
		}

		protected ItemEntry()
		{
			this.item = ItemEmpty2.class;
			this.name = "unknown";
			this.image = "item.png";
		}

		protected ItemEntry(String name)
		{
			this.item = ItemEmpty2.class;
			this.name = name;
			this.image = "item.png";
		}

		public Item2 getItem()
		{
			try 
			{
				return item.getConstructor().newInstance();
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) 
			{
				e.printStackTrace();
				return null;
			}
		}

		public static ItemEntry getUnknownEntry()
		{
			return new ItemEntry();
		}

		public static ItemEntry getUnknownEntry(String name)
		{
			return new ItemEntry(name);
		}
	}

	public ItemEntry getEntry(String name)
	{
		for (int i = 0; i < itemEntries.size(); i++)
		{
			ItemEntry r = itemEntries.get(i);

			if (r.name.equals(name))
			{
				return r;
			}
		}

		return ItemEntry.getUnknownEntry(name);
	}

	public ItemEntry getEntry(int number)
	{		
		return itemEntries.get(number);
	}
}
