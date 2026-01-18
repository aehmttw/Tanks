package tanks.registry;

import tanks.item.DefaultItemIcons;
import tanks.item.Item;
import tanks.item.ItemEmpty;
import tanks.item.ItemIcon;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class RegistryItem
{
	public ArrayList<ItemEntry> itemEntries = new ArrayList<>();

	public static class ItemEntry
	{
		public final Class<? extends Item> item;
		public final String name;
		public final ItemIcon icon;

		public ItemEntry(RegistryItem r, Class<? extends Item> item, String name, ItemIcon icon)
		{
			this.item = item;
			this.name = name;
			this.icon = icon;
			r.itemEntries.add(this);
		}

		protected ItemEntry()
		{
			this.item = ItemEmpty.class;
			this.name = "unknown";
			this.icon = DefaultItemIcons.item.getCopy();
		}

		protected ItemEntry(String name)
		{
			this.item = ItemEmpty.class;
			this.name = name;
			this.icon = DefaultItemIcons.item.getCopy();
		}

		public Item getItem()
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
