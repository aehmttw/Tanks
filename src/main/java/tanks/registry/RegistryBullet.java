package tanks.registry;

import tanks.bullet.Bullet;
import tanks.item.DefaultItemIcons;
import tanks.item.ItemIcon;

import java.util.ArrayList;

public class RegistryBullet
{
	public ArrayList<BulletEntry> bulletEntries = new ArrayList<>();

	public static class BulletEntry
	{
		public final Class<? extends Bullet> bullet;
		public final String name;
		public final ItemIcon icon;

		public BulletEntry(RegistryBullet r, Class<? extends Bullet> bullet, String name, ItemIcon icon)
		{
			this.bullet = bullet;
			this.name = name;
			this.icon = icon;

			r.bulletEntries.add(this);
		}

		protected BulletEntry()
		{
			this.bullet = Bullet.class;
			this.name = "unknown";
			this.icon = DefaultItemIcons.item.getCopy();
		}

		protected BulletEntry(String name)
		{
			this.bullet = Bullet.class;
			this.name = name;
			this.icon = DefaultItemIcons.item.getCopy();
		}

		public static BulletEntry getUnknownEntry()
		{
			return new BulletEntry();
		}

		public static BulletEntry getUnknownEntry(String name)
		{
			return new BulletEntry(name);
		}
	}

	public String[] getEntryNames()
	{
		String[] entries = new String[this.bulletEntries.size()];

		for (int i = 0; i < this.bulletEntries.size(); i++)
		{
			entries[i] = this.bulletEntries.get(i).name;
		}

		return entries;
	}

	public ItemIcon[] getIcons()
	{
		ItemIcon[] entries = new ItemIcon[this.bulletEntries.size()];

		for (int i = 0; i < this.bulletEntries.size(); i++)
		{
			entries[i] = this.bulletEntries.get(i).icon;
		}

		return entries;
	}

	public BulletEntry getEntry(String name)
	{		
		for (int i = 0; i < bulletEntries.size(); i++)
		{
			BulletEntry r = bulletEntries.get(i);

			if (r.name.equals(name))
			{
				return r;
			}
		}

		return BulletEntry.getUnknownEntry(name);
	}

	public BulletEntry getEntry(int number)
	{		
		return bulletEntries.get(number);
	}
}
