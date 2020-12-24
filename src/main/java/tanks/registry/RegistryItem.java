package tanks.registry;

import basewindow.BaseFile;
import tanks.Game;
import tanks.Player;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemEmpty;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;

public class RegistryItem
{
	public ArrayList<ItemEntry> itemEntries = new ArrayList<ItemEntry>();

	public static void loadRegistry(String homedir) 
	{
		Game.registryItem.itemEntries.clear();

		String path = homedir + Game.itemRegistryPath;

		boolean loadRegistry = Game.enableCustomItemRegistry;

		if (loadRegistry)
		{
			try 
			{
				BaseFile in = Game.game.fileManager.getFile(path);
				in.startReading();
				while (in.hasNextLine()) 
				{
					String line = in.nextLine();
					String[] itemLine = line.split(",");

					if (itemLine[0].charAt(0) == '#')
					{ 
						continue; 
					}
					if (itemLine[2].toLowerCase().equals("default"))
					{
						boolean foundItem = false;
						for (int i = 0; i < Game.defaultItems.size(); i++)
						{
							if (itemLine[0].equals(Game.defaultItems.get(i).name))
							{
								Game.defaultItems.get(i).registerEntry(Game.registryItem);
								foundItem = true;
								break;
							}
						}

						if (!foundItem)
							Game.logger.println (new Date().toString() + " (syswarn) the default item '" + itemLine[0] + "' does not exist!");
					}
					else 
					{
						try 
						{
							@SuppressWarnings("resource")
							ClassLoader loader = new URLClassLoader( new URL[] { new File(itemLine[3]).toURI().toURL() }); // super messy
							@SuppressWarnings("unchecked")
							Class<? extends Item> clasz = (Class<? extends Item>) loader.loadClass(itemLine[4]);
							new ItemEntry(Game.registryItem, clasz, itemLine[0], itemLine[1]);
						}
						catch (Exception e) 
						{
							e.printStackTrace();
							Game.logger.println(new Date().toString() + " (syswarn) error loading custom item '" + itemLine[0] + "'. try adding the path to your jvm classpath. ignoring.");
						}
					}
				}
				in.stopReading();
			} 
			catch (Exception e)
			{
				Game.logger.println (new Date().toString() + " (syswarn) item registry file is nonexistent or broken, using default:");
				e.printStackTrace(Game.logger);

				loadRegistry = false;
			}
		}
		
		if (!loadRegistry)
		{
			for (int i = 0; i < Game.defaultItems.size(); i++)
			{
				Game.defaultItems.get(i).registerEntry(Game.registryItem);
			}
		}
	}

	public static void initRegistry(String homedir) 
	{
		String path = homedir + Game.itemRegistryPath;
		try 
		{
			Game.game.fileManager.getFile(path).create();
		}
		catch (IOException e) 
		{
			Game.logger.println (new Date().toString() + " (syserr) file permissions are broken! cannot initialize item registry.");
			System.exit(1);
		}
		try 
		{
			BaseFile f = Game.game.fileManager.getFile(path);
			f.startWriting();
			f.println("# Warning! To use a custom Item Registry, you MUST set use-custom-item-registry ");
			f.println("# in options.txt from false to true!");
			f.println("# ");
			f.println("# This is the Item Registry file!");
			f.println("# A registry entry is a line in the file");
			f.println("# The parameters are name, default image, custom/default, jar location, and class");
			f.println("# Built in items do not use the last 2 parameters");
			f.println("# and have 'default' written for the third parameter");
			f.println("# To make a custom item, import the 'Tanks' jar into a java project,");
			f.println("# write a class extending Item, and export as a jar file.");
			f.println("# To import a custom item, put the jar file somewhere on your computer,");
			f.println("# put 'custom' for parameter 2");
			f.println("# and put its absolute file path as parameter 3 in this file.");
			f.println("# Then, put a comma and write the Class name with package and all as parameter 5.");
			f.println("# Example custom item entry: 'myitem,item.png,custom,C:\\Users\\potato\\.tanks\\MyItem.jar,com.potato.MyItem'");
			f.println("# Don't leave any blank lines!");

			for (int i = 0; i < Game.defaultItems.size(); i++)
			{
				f.println(Game.defaultItems.get(i).getString());
			}

			f.stopWriting();
		} 
		catch (Exception e)
		{
			Game.logger.println(new Date().toString() + " (syserr) something broke! could not initialize item registry:");
			e.printStackTrace(Game.logger);
			System.exit(1);
		}

	}

	public static class ItemEntry
	{
		public final Class<? extends Item> item;
		public final String name;
		public final String image;

		public ItemEntry(RegistryItem r, Class<? extends Item> item, String name, String image)
		{
			this.item = item;
			this.name = name;
			this.image = image;

			r.itemEntries.add(this);
		}

		protected ItemEntry()
		{
			this.item = ItemEmpty.class;
			this.name = "unknown";
			this.image = "item.png";
		}

		protected ItemEntry(String name)
		{
			this.item = ItemEmpty.class;
			this.name = name;
			this.image = "item.png";
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

	public static class DefaultItemEntry
	{
		public final Class<? extends Item> item;
		public final String name;
		public final String image;

		public DefaultItemEntry(Class<? extends Item> item, String name, String image)
		{
			this.item = item;
			this.name = name;
			this.image = image;
		}

		public void registerEntry(RegistryItem r)
		{
			new ItemEntry(r, this.item, this.name, this.image);
		}

		public String getString()
		{
			return this.name + ",default";
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
