package tanks.item;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletArc;
import tanks.bullet.BulletGas;
import tanks.bullet.DefaultBullets;
import tanks.tank.Tank;
import tanks.tank.TankPlayerRemote;
import tanks.tankson.*;

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

	@Property(id = "name", name = "Item name", miscType = Property.MiscType.complexString)
	public String name = System.currentTimeMillis() + "";

	@Property(id = "icon", name = "Icon", miscType = Property.MiscType.itemIcon)
	public String icon = "item.png";

	@Property(id = "cooldown", name = "Cooldown", desc = "Minimum time between uses of this item \n \n 1 time unit = 0.01 seconds")
	public double cooldownBase = 20;

	public boolean rightClick = false;

	@TanksONable("shop_item")
	public static class ShopItem
	{
		@Property(id = "stack")
		public ItemStack<?> itemStack;

		@Property(id = "price", name = "Price")
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
			return Serializer.toTanksON(this);
		}

		public static ShopItem fromString(String s)
		{
			if (!s.startsWith("{"))
				return fromStringLegacy(s);

			return (ShopItem) TanksON.parseObject(s);
		}

		@Deprecated
		public static ShopItem fromStringLegacy(String s)
		{
			String[] p = s.split(",");

			ItemStack<?> stack = ItemStack.fromStringLegacy(null, s);
			ShopItem si = new ShopItem(stack);

			int price = Integer.parseInt(p[2]);
			si.price = price;

			return si;
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
			if (!s.startsWith("{"))
				return fromStringLegacy(s);

			return (CrusadeShopItem) TanksON.parseObject(s);
		}

		@Deprecated
		public static CrusadeShopItem fromStringLegacy(String s)
		{
			String[] p = s.split(",");

			ItemStack<?> stack = ItemStack.fromStringLegacy(null, s);
			CrusadeShopItem si = new CrusadeShopItem(stack);

			int price = Integer.parseInt(p[2]);
			int level = Integer.parseInt(p[3]);
			si.price = price;
			si.levelUnlock = level;

			return si;
		}
	}

	@TanksONable("item_stack")
	public static abstract class ItemStack<T extends Item> implements ICopyable<ItemStack<T>>
	{
		@Property(id = "item", name = "Item")
		public T item;

		@Property(id = "amount", name = "Amount", desc = "Set to 0 for unlimited")
		public int stackSize;

		@Property(id = "max", name = "Max stack size", desc = "Set to 0 for unlimited")
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

		@Override
		public ItemStack<T> getCopy()
		{
			try
			{
				ItemStack<T> i = (ItemStack<T>) this.getClass().getConstructor(Player.class, item.getClass(), int.class).newInstance(this.player, this.item, this.maxStackSize);
				this.clonePropertiesTo(i);
				return i;
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
			if (!s.startsWith("{"))
				return fromStringLegacy(p, s);

			ItemStack<?> i = (ItemStack<?>) TanksON.parseObject(s);
			i.player = p;
			return i;
		}

		@Deprecated
		public static ItemStack<?> fromStringLegacy(Player pl, String s)
		{
			if (s.contains("[") && s.contains("]"))
				s = s.substring(s.indexOf("[") + 1, s.indexOf("]"));

			String[] p = s.split(",");

			String name = p[0];
			String image = p[1];
			int quantity = Integer.parseInt(p[4]);
			int maxStack = Integer.parseInt(p[5]);

			Item i = Game.registryItem.getEntry(p[6]).getItem();
			i.name = name;
			i.icon = image;
			ItemStack<?> is = i.getStack(pl);
			if (i instanceof ItemBullet)
			{
				ItemBullet bullet = (ItemBullet) i;

				String kind = p[7];
				switch (kind)
				{
					case "normal":
						bullet.bullet = DefaultBullets.basic_bullet.getCopy();
						break;
					case "flamethrower":
						bullet.bullet = DefaultBullets.flamethrower.getCopy();
						break;
					case "laser":
						bullet.bullet = DefaultBullets.laser.getCopy();
						break;
					case "freezing":
						bullet.bullet = DefaultBullets.freezing_bullet.getCopy();
						break;
					case "electric":
						bullet.bullet = DefaultBullets.zap.getCopy();
						break;
					case "healing":
						bullet.bullet = DefaultBullets.healing_ray.getCopy();
						break;
					case "arc":
						bullet.bullet = DefaultBullets.artillery_shell.getCopy();
						break;
					case "explosive":
						bullet.bullet = DefaultBullets.explosive_bullet.getCopy();
						break;
					case "boost":
						bullet.bullet = DefaultBullets.booster_bullet.getCopy();
						break;
					case "air":
						bullet.bullet = DefaultBullets.air.getCopy();
						break;
					case "homing":
						bullet.bullet = DefaultBullets.homing_rocket.getCopy();
						break;
				}

				switch (p[8])
				{
					case "none":
						bullet.bullet.effect = Bullet.BulletEffect.none;
						break;
					case "fire":
						bullet.bullet.effect = Bullet.BulletEffect.fire;
						break;
					case "trail":
						bullet.bullet.effect = Bullet.BulletEffect.trail;
						break;
					case "dark_fire":
						bullet.bullet.effect = Bullet.BulletEffect.dark_fire;
						break;
					case "fire_and_smoke":
						bullet.bullet.effect = Bullet.BulletEffect.fire_trail;
						break;
					case "ice":
						bullet.bullet.effect = Bullet.BulletEffect.ice;
						break;
					case "ember":
						bullet.bullet.effect = Bullet.BulletEffect.ember;
						break;
				}

				bullet.bullet.speed = Double.parseDouble(p[9]);

				if (p[7].equals("arc"))
				{
					((BulletArc)bullet.bullet).maxRange = 1000 * bullet.bullet.speed / 3.125;
					if (bullet.bullet.effect == Bullet.BulletEffect.none)
						bullet.bullet.effect = Bullet.BulletEffect.long_trail;
				}

				if (p[7].equals("electric"))
					bullet.bullet.rebounds = Integer.parseInt(p[10]);
				else
					bullet.bullet.bounces = Integer.parseInt(p[10]);

				bullet.bullet.damage = Double.parseDouble(p[11]);

				if (p[7].equals("healing"))
					bullet.bullet.damage *= -1;
				else if (p[7].equals("boost") || p[7].equals("air"))
					bullet.bullet.damage = 0;

				bullet.bullet.maxLiveBullets = Integer.parseInt(p[12]);
				bullet.cooldownBase = Double.parseDouble(p[13]);

				if (p[7].equals("flamethrower") || p[7].equals("air"))
				{
					bullet.bullet.lifespan *= Double.parseDouble(p[14]) / Bullet.bullet_size;
					((BulletGas)(bullet.bullet)).endSize *= Double.parseDouble(p[14]) / Bullet.bullet_size;
				}
				else
					bullet.bullet.size = Double.parseDouble(p[14]);

				bullet.bullet.recoil = Double.parseDouble(p[15]);
				bullet.bullet.heavy = Boolean.parseBoolean(p[16]);

				if (p.length > 17)
				{
					bullet.bullet.accuracySpread = Double.parseDouble(p[17]);
					bullet.bullet.shotCount = Integer.parseInt(p[18]);
					bullet.bullet.multishotSpread = Double.parseDouble(p[19]);
				}
			}
			else if (i instanceof ItemMine)
			{
				ItemMine mine = (ItemMine) i;
				mine.mine.timer = Double.parseDouble(p[7]);
				mine.mine.triggeredTimer = Double.parseDouble(p[8]);
				mine.mine.explosion.radius = Double.parseDouble(p[9]);
				mine.mine.explosion.damage = Double.parseDouble(p[10]);
				mine.mine.maxLiveMines = Integer.parseInt(p[11]);
				mine.cooldownBase = Double.parseDouble(p[12]);
				mine.mine.size = Double.parseDouble(p[13]);
				mine.mine.explosion.destroysObstacles = Boolean.parseBoolean(p[14]);
			}
			else if (i instanceof ItemShield)
			{
				ItemShield shield = (ItemShield) i;
				shield.amount = Double.parseDouble(p[7]);
				shield.max = Double.parseDouble(p[8]);
				shield.cooldownBase = Double.parseDouble(p[9]);
			}

			is.stackSize = quantity;
			is.maxStackSize = maxStack;

			return is;
		}

		@Override
		public String toString()
		{
			return Serializer.toTanksON(this);
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

		return Serializer.toTanksON(this);
	}

	public static Item fromString(String s)
	{
		return (Item) TanksON.parseObject(s);
	}
}
