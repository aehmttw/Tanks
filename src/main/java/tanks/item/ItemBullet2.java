package tanks.item;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.item.legacy.ItemBullet;
import tanks.minigames.Minigame;
import tanks.tank.Tank;

public class ItemBullet2 extends Item2
{
	public static final String item_class_name = "bullet";

	public Bullet bullet;

	public ItemBullet2()
	{
		this.rightClick = false;
		this.supportsHits = true;
	}

	public ItemBullet2(Bullet b)
	{
		this();
		this.bullet = b;
	}

	@Override
	public ItemStack<?> getStack(Player p)
	{
		return new ItemStackBullet(p, this, 0);
	}

	public static class ItemStackBullet extends ItemStack<ItemBullet2>
	{
		public double fractionUsed = 0;
		public int liveBullets;

		public ItemStackBullet(Player p, ItemBullet2 item, int max)
		{
			super(p, item, max);
		}

		@Override
		public void use(Tank m)
		{
			try
			{
				double remainingQty = this.stackSize - this.fractionUsed;
				double useAmt = 1;

				if (this.unlimited)
					remainingQty = Double.MAX_VALUE;

				if (this.item.cooldownBase <= 0)
					useAmt = Panel.frameFrequency;

				int q = (int) Math.min(this.item.bullet.shotCount, Math.ceil(remainingQty / useAmt));

				double speedmul = m.getAttributeValue(AttributeModifier.bullet_speed, 1);

				for (int i = 0; i < q; i++)
				{
					double baseOff = 0;

					if (q > 1)
					{
						if (this.item.bullet.multishotSpread >= 360)
							baseOff = Math.PI * 2 * i / q;
						else
							baseOff = Math.toRadians(this.item.bullet.multishotSpread) * ((i * 1.0 / (q - 1)) - 0.5);
					}

					Bullet b = this.item.bullet.getClass().getConstructor(double.class, double.class, Tank.class, boolean.class, ItemBullet.class)
							.newInstance(m.posX, m.posY, m, false, this);
					this.item.bullet.cloneProperties(b);

					if (this.item.cooldownBase <= 0)
					{
						b.frameDamageMultipler = Panel.frameFrequency;
						this.fractionUsed += Panel.frameFrequency;
					}
					else
						this.fractionUsed++;

					this.setOtherItemsCooldown();
					this.cooldown = this.item.cooldownBase;

					double off = baseOff + (Math.random() - 0.5) * Math.toRadians(this.item.bullet.accuracySpread);
					m.fireBullet(b, this.item.bullet.speed * speedmul, off);

					if (Game.currentLevel instanceof Minigame)
					{
						((Minigame) Game.currentLevel).onBulletFire(b);
					}

					while (this.fractionUsed >= 1 && !this.unlimited)
					{
						this.stackSize--;
						this.fractionUsed--;
					}

					// TODO: make this work with fraction used, involves fixing the hits too
					if (Crusade.crusadeMode && Crusade.currentCrusade != null && this.player != null)
						Crusade.currentCrusade.getCrusadePlayer(this.player).addItemUse(this);

					if (this.stackSize <= 0)
						this.destroy = true;
				}
			}
			catch (Exception e)
			{
				Game.exitToCrash(e);
			}
		}

		@Override
		public boolean usable(Tank t)
		{
			return t != null
					&& (this.item.bullet.maxLiveBullets <= 0 || this.liveBullets <= this.item.bullet.maxLiveBullets - this.item.bullet.shotCount)
					&& !(this.cooldown > 0) && this.stackSize > 0;
		}
	}
}
