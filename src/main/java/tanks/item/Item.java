package tanks.item;

import tanks.*;
import tanks.tank.Tank;
import tanks.tank.TankPlayerRemote;
import tanks.tankson.Property;
import tanks.tankson.TanksJson;
import tanks.tankson.TanksONable;

import java.util.ArrayList;
import java.util.Arrays;

@TanksONable("item")
public abstract class Item implements IGameObject
{
	public static final String item_class_name = "item";

	public static ArrayList<String> icons = new ArrayList<>(Arrays.asList("item.png", "bullet_normal.png", "bullet_mini.png", "bullet_large.png", "bullet_fire.png", "bullet_fire_trail.png", "bullet_dark_fire.png", "bullet_flame.png",
			"bullet_laser.png", "bullet_healing.png", "bullet_electric.png", "bullet_freeze.png", "bullet_arc.png", "bullet_explosive.png", "bullet_boost.png", "bullet_air.png", "bullet_homing.png",
			"mine.png",
			"shield.png", "shield_gold.png"));

	// Items like bullets and mines can hit enemies, so this will be shown on the stats screen
	public boolean supportsHits = false;

	@Property(id = "name", name = "Item name")
	public String name = System.currentTimeMillis() + "";

	@Property(id = "icon", name = "Icon", miscType = Property.MiscType.itemIcon)
	public String icon = "item.png";

	@Property(id = "cooldown", name = "Cooldown")
	public double cooldownBase = 20;

	public boolean rightClick = false;

	@TanksONable("shop_item")
	public static class ShopItem
	{
		@Property(id = "stack")
		public ItemStack<?> itemStack;

		@Property(id = "price")
		public int price;

		public ShopItem(ItemStack<?> itemStack)
		{
			this.itemStack = itemStack;
		}

		public ShopItem()
		{

		}

		public String toString()
		{
			return TanksJson.toJson(this).toString();
		}

		public static ShopItem fromString(String s)
		{
			return (ShopItem) TanksJson.parseObject(s);
		}
	}

	@TanksONable("crusade_shop_item")
	public static class CrusadeShopItem extends ShopItem
	{
		@Property(id = "unlock_level")
		public int levelUnlock;

		public CrusadeShopItem(ItemStack<?> itemStack)
		{
			super(itemStack);
		}

		public CrusadeShopItem()
		{

		}

		public static CrusadeShopItem fromString(String s)
		{
			return (CrusadeShopItem) TanksJson.parseObject(s);
		}
	}

	@TanksONable("item_stack")
	public static abstract class ItemStack<T extends Item>
	{
		public T item;

		@Property(id = "amount", name = "Amount")
		public int stackSize;

		@Property(id = "max", name = "Max stack size")
		public int maxStackSize;

		public boolean isEmpty = false;

		public double cooldown = 0;
		public boolean destroy = false;
		public Player player;

		/**
		 * Creates a new item stack with given parameters. Make sure if you extend this class you provide the same
		 * constructor parameters in your subclass.
		 *
		 * @param p player
		 * @param item item
		 * @param max max stack size, set to 0 or negative for infinite
		 */
		public ItemStack(Player p, T item, int max)
		{
			this.player = p;
			this.item = item;
			this.maxStackSize = max;
		}

		public ItemStack<?> getCopy()
		{
			try
			{
				return (ItemStack<?>) this.getClass().getConstructor(Player.class, item.getClass(), int.class).newInstance(this.player, this.item, this.maxStackSize);
			}
			catch (Exception e)
			{
				Game.exitToCrash(e);
			}

			return null;
		}

		public boolean usable()
		{
			return this.usable(this.getUser());
		}

		public abstract boolean usable(Tank t);

		public void use()
		{
			this.use(this.getUser());
		}

		public void use(Tank user)
		{
			this.setOtherItemsCooldown();
			this.subtractItem();
			this.cooldown = this.item.cooldownBase;

			if (Crusade.crusadeMode && Crusade.currentCrusade != null && this.player != null)
				Crusade.currentCrusade.getCrusadePlayer(this.player).addItemUse(this);
		}


		public void attemptUse()
		{
			this.attemptUse(this.getUser());
		}

		public void attemptUse(Tank t)
		{
			if (this.usable(t))
			{
				use(t);
			}
		}

		public Tank getUser()
		{
			if (this.player == null)
				return null;
			else if (this.player == Game.player)
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

		public void updateCooldown(double reload)
		{
			this.cooldown = Math.max(0, this.cooldown - Panel.frameFrequency * reload);
		}

		public void setOtherItemsCooldown()
		{
			Tank user = this.getUser();

			if (user != null)
				user.setBufferCooldown(20);
		}

		public void subtractItem()
		{
			if (this.stackSize > 0)
			{
				this.stackSize--;

				if (this.stackSize <= 0)
					this.destroy = true;
			}
		}

		public static ItemStack<?> fromString(Player p, String s)
		{
			ItemStack<?> i = (ItemStack<?>) TanksJson.parseObject(s);
			i.player = p;
			return i;
		}
	}

	public abstract ItemStack<?> getStack(Player p);

	public String toString()
	{
//		try
//		{
//			String type = (String) this.getClass().getField("item_class_name").get(null);
//			StringBuilder s = new StringBuilder(type + "[");
//
//			for (Field f : this.getClass().getFields())
//			{
//				ItemProperty a = f.getAnnotation(ItemProperty.class);
//				if (a != null)
//				{
//					s.append(a.id());
//					s.append("=");
//
//					if (f.get(this) != null)
//						s.append(f.get(this));
//					else
//						s.append("*");
//
//					s.append(";");
//				}
//			}
//
//			return s.append("]").toString();
//		}
//		catch (Exception e)
//		{
//			Game.exitToCrash(e);
//		}
//
//		return null;

		return TanksJson.toJson(this).toString();
	}

	public static Item fromString(String s)
	{
		return (Item) TanksJson.parseObject(s);
	}
}
