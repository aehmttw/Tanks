package tanks.item;

import basewindow.Color;
import tanks.*;
import tanks.bullet.*;
import tanks.tank.Mine;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerRemote;
import tanks.tankson.*;

@TanksONable("item")
public abstract class Item extends GameObject
{
	public static final String item_class_name = "item";

	// Items like bullets and mines can hit enemies, so this will be shown on the stats screen
	public boolean supportsHits = false;

	@Property(id = "name", name = "Item name", miscType = Property.MiscType.complexString)
	public String name = System.currentTimeMillis() + "";

	@Property(id = "icon", name = "Icon", miscType = Property.MiscType.itemIcon)
	public ItemIcon icon = new ItemIcon("item", "item.png");

    @Property(id = "auto_icon", name = "", category = "none")
    public boolean autoIcon = false;

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

			return (ShopItem) Serializer.fromTanksON(s);
		}

		@Deprecated
		public static ShopItem fromStringLegacy(String s)
		{
			String[] p = s.split(",");

			ItemStack<?> stack = ItemStack.fromStringLegacy(null, s);
			ShopItem si = new ShopItem(stack);
            si.price = Integer.parseInt(p[2]);

			return si;
		}
	}

	@TanksONable("crusade_shop_item")
	public static class CrusadeShopItem extends ShopItem
	{
		@Property(id = "unlock_level", name = "Unlocks after level")
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

			return (CrusadeShopItem) Serializer.fromTanksON(s);
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
	public static abstract class ItemStack<T extends Item> implements ICopyable<ItemStack<T>>, ITanksONEditable
	{
		@Property(id = "item", name = "Item")
		public T item;

		@Property(id = "amount", name = "Amount", desc = "Set to 0 for unlimited", minValue = 0)
		public int stackSize;

		@Property(id = "max", name = "Max stack size", desc = "Set to 0 for unlimited", minValue = 0)
		public int maxStackSize;

		public boolean isEmpty = false;

		public double cooldown = 0;
		public boolean destroy = false;
		public Player player;

		public int networkIndex = 0;

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

		public boolean attemptUse(Tank t)
		{
			if (this.usable(t) && !this.destroy)
			{
				use(t);
				return true;
			}

			return false;
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
				user.setBufferCooldown(this, 20);
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

			ItemStack<?> i = (ItemStack<?>) Serializer.fromTanksON(s);
			i.player = p;

            if (i.item.autoIcon)
                i.item.setAutomaticIcon();

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
			i.icon = Game.registryItemIcon.getItemIcon(image.replace(".png", "")).getCopy();
			ItemStack<?> is = i.getStack(pl);
			if (i instanceof ItemBullet)
			{
				ItemBullet bullet = (ItemBullet) i;

				String kind = p[7];
				switch (kind)
				{
					case "normal":
						bullet.bullet = DefaultItems.basic_bullet.bullet.getCopy();
						break;
					case "flamethrower":
						bullet.bullet = DefaultItems.flamethrower.bullet.getCopy();
						break;
					case "laser":
						bullet.bullet = DefaultItems.laser.bullet.getCopy();
						break;
					case "freezing":
						bullet.bullet = DefaultItems.freezing_bullet.bullet.getCopy();
						break;
					case "electric":
						bullet.bullet = DefaultItems.zap.bullet.getCopy();
						break;
					case "healing":
						bullet.bullet = DefaultItems.healing_ray.bullet.getCopy();
						break;
					case "arc":
						bullet.bullet = DefaultItems.artillery_shell.bullet.getCopy();
						break;
					case "explosive":
						bullet.bullet = DefaultItems.explosive_bullet.bullet.getCopy();
						break;
					case "boost":
						bullet.bullet = DefaultItems.booster_bullet.bullet.getCopy();
						break;
					case "air":
						bullet.bullet = DefaultItems.air.bullet.getCopy();
						break;
					case "homing":
						bullet.bullet = DefaultItems.homing_rocket.bullet.getCopy();
						break;
				}

				switch (p[8])
				{
					case "none":
						bullet.bullet.effect = new BulletEffect();
						break;
					case "fire":
						bullet.bullet.effect = BulletEffect.fire.getCopy();
						break;
					case "trail":
						bullet.bullet.effect = BulletEffect.trail.getCopy();
						break;
					case "dark_fire":
						bullet.bullet.effect = BulletEffect.dark_fire.getCopy();
						break;
					case "fire_and_smoke":
						bullet.bullet.effect = BulletEffect.fire_trail.getCopy();
						break;
					case "ice":
						bullet.bullet.effect = BulletEffect.ice.getCopy();
						break;
					case "ember":
						bullet.bullet.effect = BulletEffect.ember.getCopy();
						break;
				}

				bullet.bullet.speed = Double.parseDouble(p[9]);

				if (p[7].equals("arc"))
				{
					((BulletArc)bullet.bullet).maxRange = 1000 * bullet.bullet.speed / 3.125;
					if (bullet.bullet.effect.trailEffects.isEmpty() && !bullet.bullet.effect.enableParticles)
						bullet.bullet.effect = BulletEffect.long_trail.getCopy();
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
				bullet.bullet.pitch *= Bullet.bullet_size / bullet.bullet.size;

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

		@Override
		public String getName()
		{
			return this.item.name;
		}
	}

	public abstract ItemStack<?> getStack(Player p);

    public void setAutomaticIcon()
    {
        this.autoIcon = true;
        this.icon = this.getAutomaticIcon();
    }

    public ItemIcon getAutomaticIcon()
    {
        if (this instanceof ItemShield)
            return DefaultItemIcons.shield.getCopy();
        else if (this instanceof ItemMine)
        {
            Mine m = ((ItemMine) this).mine;
            ItemIcon ii = DefaultItemIcons.mine.getCopy();
            ii.colors.get(2).set(m.initialColor.red * 0.85 + m.finalColor.red * 0.15, m.initialColor.green * 0.85 + m.finalColor.green * 0.15, m.initialColor.blue * 0.85 + m.finalColor.blue * 0.15);
            return ii;
        }
        else if (this instanceof ItemBullet)
        {
            Bullet b = ((ItemBullet) this).bullet;
            Color primary = new Color().set(TankPlayer.default_primary_color);
            Color secondary = new Color().set(TankPlayer.default_secondary_color);

            if (b.overrideBaseColor)
                primary.set(b.baseColor);

            if (b.overrideOutlineColor)
                secondary.set(b.outlineColor);

            if (b.hitExplosion != null)
            {
                primary.set(255, 255, 0);
                secondary.set(255, 0, 0);
            }

            double straightTrailWidth = 0;
            Color straightTrail = new Color(0, 0, 0, 0);
            Color fireTrailStart = new Color(0, 0, 0, 0);
            Color fireTrailEnd = new Color(0, 0, 0, 0);

            Color homing1 = null;
            Color homing2 = null;

            for (Trail t: b.effect.trailEffects)
            {
                if (t.frontWidth >= t.backWidth)
                {
                    if (t.frontColor.alpha >= 0.25)
                    {
                        straightTrail.set(t.frontColor);
                        straightTrailWidth = t.frontWidth;
                    }
                }
                else
                {
                    fireTrailStart.set(t.frontColor);
                    fireTrailEnd.set(t.backColor);
                    fireTrailStart.alpha = 255;
                    fireTrailEnd.alpha = 180;
                }
            }

            if (b.effect.enableHomingParticles && b.homingSharpness != 0)
            {
                homing1 = new Color().set(b.effect.homingParticleColor);
                homing2 = new Color().set(b.effect.homingParticleColor);
                homing1.red = Math.max(0, homing1.red - 20);
                homing1.green = Math.max(0, homing1.green - 20);
                homing1.blue = Math.max(0, homing1.blue - 20);
                homing2.red = Math.min(255, homing2.red + 20);
                homing2.green = Math.min(255, homing2.green + 20);
                homing2.blue = Math.min(255, homing2.blue + 20);
            }

            if (b instanceof BulletArc)
            {
                ItemIcon ii = b instanceof BulletBlock ? DefaultItemIcons.bullet_block.getCopy() : DefaultItemIcons.bullet_arc.getCopy();
                ii.colors.get(0).set(straightTrail);
                ii.colors.get(1).set(secondary);
                ii.colors.get(2).set(primary);
                return ii;
            }
            else if (b instanceof BulletGas)
            {
                ItemIcon ii;
                if (b.accuracySpread > 0)
                    ii = DefaultItemIcons.bullet_air.getCopy();
                else
                    ii = DefaultItemIcons.bullet_flame.getCopy();

                Color noise = ((BulletGas) b).noise;
                ii.colors.get(0).set(secondary.red + noise.red / 2, secondary.green + noise.green / 2, secondary.blue + noise.blue / 2);
                ii.colors.get(1).set(primary.red + noise.red / 2, primary.green + noise.green / 2, primary.blue + noise.blue / 2);
                return ii;
            }
            else if (b instanceof BulletInstant)
            {
                ItemIcon ii;
                if (b.hitStun > 0)
                {
                    ii = DefaultItemIcons.bullet_electric.getCopy();
                    ii.colors.get(0).set(primary);
                    ii.colors.get(1).set(secondary.red * 0.635 + primary.red * 0.365, secondary.green * 0.635 + primary.green * 0.365, secondary.blue * 0.635 + primary.blue * 0.365);
                }
                else if (b.damage >= 0)
                {
                    ii = DefaultItemIcons.bullet_laser.getCopy();
                    ii.colors.get(0).set(primary);
                    ii.colors.get(1).set(secondary.red * 0.2 + primary.red * 0.8, secondary.green * 0.2 + primary.green * 0.8, secondary.blue * 0.2 + primary.blue * 0.8);
                    ii.colors.get(2).set(secondary.red * 0.4 + primary.red * 0.6, secondary.green * 0.4 + primary.green * 0.6, secondary.blue * 0.4 + primary.blue * 0.6);
                }
                else
                {
                    ii = DefaultItemIcons.bullet_healing.getCopy();
                    ii.colors.get(0).set(primary);
                    ii.colors.get(1).set(secondary.red * 0.6 + primary.red * 0.4, secondary.green * 0.6 + primary.green * 0.4, secondary.blue * 0.6 + primary.blue * 0.4);
                    ii.colors.get(2).set(secondary.red * 0.8 + primary.red * 0.2, secondary.green * 0.8 + primary.green * 0.2, secondary.blue * 0.8 + primary.blue * 0.2);
                }
                return ii;
            }
            else if (b instanceof BulletAirStrike)
            {
                ItemIcon ii = DefaultItemIcons.bullet_air_strike.getCopy();
                ii.colors.get(0).set(straightTrail);
                ii.colors.get(1).set(fireTrailEnd);
                ii.colors.get(2).set(fireTrailStart);
                ii.colors.get(3).set(fireTrailEnd.red, fireTrailEnd.green, fireTrailEnd.blue);
                ii.colors.get(4).set(fireTrailEnd.red * 0.15 + fireTrailStart.red * 0.85, fireTrailEnd.green * 0.15 + fireTrailStart.green * 0.85, fireTrailEnd.blue * 0.15 + fireTrailStart.blue * 0.85);

                return ii;
            }
            else if (b.freezing)
            {
                ItemIcon ii = DefaultItemIcons.bullet_freeze.getCopy();
                ii.colors.get(1).set(secondary);
                return ii;
            }
            else if (b.boosting)
            {
                ItemIcon ii = DefaultItemIcons.bullet_boost.getCopy();
                ii.colors.get(0).set(secondary);
                return ii;
            }
            else if (b.size < 8)
            {
                return DefaultItemIcons.bullet_mini.getCopy();
            }
            else if (b.size > 20)
            {
                ItemIcon ii = DefaultItemIcons.bullet_large.getCopy();
                ii.colors.get(0).set(secondary);
                ii.colors.get(1).set(primary);
                return ii;
            }
            else if (homing1 != null || fireTrailStart.alpha > 0)
            {
                ItemIcon ii;
                if (homing1 != null)
                    ii = DefaultItemIcons.bullet_homing.getCopy();
                else if (straightTrailWidth > 1.5)
                    ii = DefaultItemIcons.bullet_fire_trail.getCopy();
                else if (fireTrailStart.red + fireTrailStart.green + fireTrailStart.blue < 100)
                    ii = DefaultItemIcons.bullet_dark_fire.getCopy();
                else
                    ii = DefaultItemIcons.bullet_fire.getCopy();

                ii.colors.get(0).set(0, 0, 0, 0);
                if (straightTrailWidth <= 1.5)
                    ii.colors.get(0).set(straightTrail);
                else
                    ii.colors.get(1).set(straightTrail);

                if (homing1 != null)
                {
                    ii.colors.get(2).set(homing1);
                    ii.colors.get(3).set(homing2);
                }

                ii.colors.get(4).set(fireTrailEnd);
                ii.colors.get(5).set(fireTrailStart);
                ii.colors.get(6).set(fireTrailEnd.red, fireTrailEnd.green, fireTrailEnd.blue);
                ii.colors.get(7).set(fireTrailEnd.red * 0.15 + fireTrailStart.red * 0.85, fireTrailEnd.green * 0.15 + fireTrailStart.green * 0.85, fireTrailEnd.blue * 0.15 + fireTrailStart.blue * 0.85);
                return ii;
            }
            else
            {
                ItemIcon ii = DefaultItemIcons.bullet_normal.getCopy();
                ii.colors.get(0).set(straightTrail);
                ii.colors.get(1).set(secondary);
                ii.colors.get(2).set(primary);
                return ii;
            }
        }
        else
            return DefaultItemIcons.item.getCopy();
    }

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
        return (Item) Serializer.fromTanksON(s);
	}
}
