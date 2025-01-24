package tanks.item;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.effect.AttributeModifier;
import tanks.minigames.Minigame;
import tanks.tank.Tank;
import tanks.tankson.Property;

public class ItemBullet extends Item
{
	public static final String item_class_name = "bullet";

	@Property(id="bullet", category = "none")
	public Bullet bullet = new Bullet();

	public ItemBullet()
	{
		this.rightClick = false;
		this.supportsHits = true;
		this.icon = "bullet_normal.png";
	}

	public ItemBullet(Bullet b)
	{
		this();
		this.bullet = b;
	}

	@Override
	public ItemStack<?> getStack(Player p)
	{
		return new ItemStackBullet(p, this, 0);
	}

	public static class ItemStackBullet extends ItemStack<ItemBullet>
	{
		public double fractionUsed = 0;
		public int liveBullets;

		public ItemStackBullet(Player p, ItemBullet item, int max)
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
				boolean unlimited = false;

				if (this.stackSize <= 0)
				{
					remainingQty = Double.MAX_VALUE;
					unlimited = true;
				}

				if (this.item.cooldownBase <= 0)
					useAmt = Panel.frameFrequency;

				int q = (int) Math.min(this.item.bullet.shotCount, Math.ceil(remainingQty / useAmt));

				double speedmul = m.em().getAttributeValue(AttributeModifier.bullet_speed, 1);

				if (this.item.bullet.shotSound != null && this.item.bullet.soundVolume > 0)
					Drawing.drawing.playGlobalSound(this.item.bullet.shotSound,
							(float) (this.item.bullet.pitch * (1 - (Math.random() * 0.5) * this.item.bullet.pitchVariation)),
							(float) this.item.bullet.soundVolume);

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

					Bullet b = this.item.bullet.getClass().getConstructor(double.class, double.class, Tank.class, boolean.class, ItemStackBullet.class)
							.newInstance(m.posX, m.posY, m, true, this);
					this.item.bullet.clonePropertiesTo(b);
					b.setColorFromTank();

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

					while (this.fractionUsed >= 1 && this.stackSize > 0)
					{
						this.stackSize--;
						this.fractionUsed--;
					}

					// TODO: make this work with fraction used, involves fixing the hits too
					if (Crusade.crusadeMode && Crusade.currentCrusade != null && this.player != null)
						Crusade.currentCrusade.getCrusadePlayer(this.player).addItemUse(this);

					if (this.stackSize <= 0 && !unlimited)
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
					&& !(this.cooldown > 0);
		}
	}
}
