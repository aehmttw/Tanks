package tanks.hotbar.item;

import tanks.*;
import tanks.gui.IFixedMenu;
import tanks.gui.Scoreboard;
import tanks.gui.property.*;
import tanks.tank.Tank;
import tanks.tank.TankPlayerRemote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public abstract class Item implements IGameObject
{
	public static ArrayList<String> icons = new ArrayList<>(Arrays.asList("item.png", "bullet_normal.png", "bullet_mini.png", "bullet_large.png", "bullet_fire.png", "bullet_fire_trail.png", "bullet_dark_fire.png", "bullet_flame.png",
			"bullet_laser.png", "bullet_healing.png", "bullet_electric.png", "bullet_freeze.png", "bullet_arc.png", "bullet_explosive.png", "bullet_boost.png",
			"mine.png",
			"shield.png", "shield_gold.png"));

	// Items like bullets and mines can hit enemies, so this will be shown on the stats screen
	public boolean supportsHits = false;

	public boolean isConsumable;
	public int levelUnlock;
	public int price;
	public int maxStackSize = 100;
	public int stackSize = 1;
	public boolean unlimitedStack = false;
	public boolean inUse = false;
	public String name = System.currentTimeMillis() + "";
	public String icon;
	public LinkedHashMap<String, UIProperty> properties = new LinkedHashMap<>();

	public boolean destroy = false;
	
	public boolean rightClick;

	public Player player;

	public abstract boolean usable();

	public void use()
	{
		this.use(this.getUser());
	}

	public abstract void use(Tank user);

	public Item(Player p)
	{
		this();
		this.player = p;
	}

	public Item()
	{
		String[] s = new String[icons.size()];

		for (int i = 0; i < icons.size(); i++)
			s[i] = icons.get(i);

		new UIPropertyString(this.properties,"name", this.name);
		new UIPropertyImageSelector(this.properties, "icon", s,0);
		new UIPropertyInt(this.properties, "amount", 1);
		new UIPropertyInt(this.properties, "max-stack-size", 100);
		new UIPropertyInt(this.properties, "unlocks-after-level", 0);
		new UIPropertyInt(this.properties, "price", 1);
	}


	/**name-image-price-level-quantity-max_quantity-type
	 * <br>if (type == bullet):-class-effect-speed-bounces-damage-max_on_screen-cooldown-size*/
	public static Item parseItem(Player pl, String s)
	{
		String[] p = s.split(",");

		String name = p[0];
		String image = p[1];
		int price = Integer.parseInt(p[2]);
		int level = Integer.parseInt(p[3]);
		int quantity = Integer.parseInt(p[4]);
		int maxStack = Integer.parseInt(p[5]);

		Item i = Game.registryItem.getEntry(p[6]).getItem();
		i.player = pl;

		int l = 7;

		for (int j = 0; j < 7; j++)
		{
			l += p[j].length();
		}

		i.fromString(s.substring(l));
		
		i.name = name;
		i.icon = image;
		i.price = price;
		i.levelUnlock = level;
		i.stackSize = quantity;
		i.maxStackSize = maxStack;
		
		return i;
	}
	
	@Override
	public String toString()
	{
		return name + "," + icon + "," + price + "," + levelUnlock + "," + stackSize + "," + maxStackSize;
	}

	public void attemptUse()
	{
		if (this.usable())
		{
			use();

			for (IFixedMenu m : ModAPI.menuGroup)
			{
				if (m instanceof Scoreboard && ((Scoreboard) m).objectiveType.equals(Scoreboard.objectiveTypes.items_used)) {
					if (((Scoreboard) m).players.isEmpty())
						((Scoreboard) m).addTeamScore(this.player.tank.team, 1);
					else
						((Scoreboard) m).addPlayerScore(this.player, 1);
				}
			}
		}
	}

	public abstract void fromString(String s);

	public void exportProperties()
	{
		this.name = (String) this.getProperty("name");
		this.icon = (String) this.getProperty("icon");
		this.stackSize = (int) this.getProperty("amount");
		this.maxStackSize = (int) this.getProperty("max-stack-size");
		this.levelUnlock = (int) this.getProperty("unlocks-after-level");
		this.price = (int) this.getProperty("price");
	}

	public void importProperties()
	{
		this.setProperty("name", this.name);
		this.setProperty("icon", this.icon);
		this.setProperty("amount", this.stackSize);
		this.setProperty("max-stack-size", this.maxStackSize);
		this.setProperty("unlocks-after-level", this.levelUnlock);
		this.setProperty("price", this.price);
	}

	public Object getProperty(String s)
	{
		UIProperty p = this.properties.get(s);
		Object o = p.value;

		if (p instanceof UIPropertySelector)
			return ((UIPropertySelector) p).values[(int) o];
		else if (p instanceof UIPropertyImageSelector)
			return ((UIPropertyImageSelector) p).values[(int) o];

		return o;
	}

	public void setProperty(String s, Object value)
	{
		UIProperty p = this.properties.get(s);

		if (p instanceof UIPropertySelector)
		{
			for (int i = 0; i < ((UIPropertySelector) p).values.length; i++)
			{
				if (((UIPropertySelector) p).values[i].equals(value))
					p.value = i;
			}
		}
		else if (p instanceof UIPropertyImageSelector)
		{
			for (int i = 0; i < ((UIPropertyImageSelector) p).values.length; i++)
			{
				if (((UIPropertyImageSelector) p).values[i].equals(value))
					p.value = i;
			}
		}
		else
			p.value = value;
	}

	public Tank getUser()
	{
		if (this.player == Game.player)
		{
			return Game.playerTank;
		}
		else
		{
			for (Movable m: Game.movables)
			{
				if (m instanceof TankPlayerRemote && ((TankPlayerRemote) m).player.clientID.equals(this.player.clientID))
				{
					return (Tank) m;
				}
			}
		}

		return null;
	}

	public Item clone()
	{
		return Item.parseItem(this.player, this.toString());
	}

	public abstract String getTypeName();
}
