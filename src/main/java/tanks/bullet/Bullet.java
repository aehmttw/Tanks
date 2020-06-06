package tanks.bullet;

import tanks.*;
import tanks.event.*;
import tanks.hotbar.ItemBullet;
import tanks.obstacle.Obstacle;
import tanks.tank.Mine;
import tanks.tank.Ray;
import tanks.tank.Tank;
import tanks.tank.TankPlayerRemote;

import java.util.ArrayList;
import java.util.HashMap;

public class Bullet extends Movable implements IDrawable
{
	public static int currentID = 0;
	public static ArrayList<Integer> freeIDs = new ArrayList<Integer>();
	public static HashMap<Integer, Bullet> idMap = new HashMap<Integer, Bullet>();

	public int networkID;

	public enum BulletEffect {none, fire, darkFire, fireTrail, ice, trail}

	public static double bullet_size = 10;

	public String name;

	public boolean enableExternalCollisions = true;
	public boolean playPopSound = true;
	public boolean playBounceSound = true;
	public double age = 0;
	public double size;
	public int bounces;
	public int bouncyBounces = 100;

	public double ageFrac = 0;
	public double quarterAgeFrac = 0;
	public double halfAgeFrac = 0;

	public double sinceLastEffect = 0;

	public double baseColorR;
	public double baseColorG;
	public double baseColorB;

	public double outlineColorR;
	public double outlineColorG;
	public double outlineColorB;

	public double iPosZ;

	public double destroyTimer = 0;
	public Tank tank;
	public double damage = 1;
	public BulletEffect effect = BulletEffect.none;
	public boolean useCustomWallCollision = false;
	public double wallCollisionSize = 10;
	public boolean heavy = false;
	public ItemBullet item;
	public double recoil = 1.0;

	public boolean affectsMaxLiveBullets;

	public String itemSound = "shoot.ogg";

	public Bullet(double x, double y, int bounces, Tank t)
	{
		this(x, y, bounces, t, true, null);
	}

	/** Do not use, instead use the constructor with primitive data types. Intended for Item use only!*/
	@Deprecated
	public Bullet(Double x, Double y, Integer bounces, Tank t, ItemBullet ib)
	{
		this(x.doubleValue(), y.doubleValue(), bounces.intValue(), t, false, ib);
	}

	public Bullet(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets, ItemBullet item)
	{
		super(x, y);

		this.item = item;
		this.vX = 0;
		this.vY = 0;
		this.size = bullet_size;
		this.baseColorR = t.colorR;
		this.baseColorG = t.colorG;
		this.baseColorB = t.colorB;

		double[] oc = Team.getObjectColor(t.turret.colorR, t.turret.colorG, t.turret.colorB, t);
		this.outlineColorR = oc[0];
		this.outlineColorG = oc[1];
		this.outlineColorB = oc[2];

		this.bounces = bounces;
		this.tank = t;
		this.team = t.team;
		this.name = "normal";

		this.iPosZ = this.tank.size / 2 + this.tank.turret.size / 2;

		this.isRemote = t.isRemote;

		//if (!t.isRemote && fireEvent)
		//	Game.eventsOut.add(new EventShootBullet(this));

		this.affectsMaxLiveBullets = affectsMaxLiveBullets;

		if (!this.tank.isRemote && this.affectsMaxLiveBullets)
		{
			if (this.item == null)
				t.liveBullets++;
			else
				this.item.liveBullets++;
		}

		if (freeIDs.size() > 0)
			this.networkID = freeIDs.remove(0);
		else
		{
			this.networkID = currentID;
			currentID++;
		}

		idMap.put(this.networkID, this);

		this.drawLevel = 8;
	}

	public void moveOut(double amount)
	{
		this.moveInDirection(vX, vY, amount);
	}

	public void collidedWithTank(Tank t)
	{
		if (!heavy)
			this.destroy = true;

		if (!(Team.isAllied(this, t) && !this.team.friendlyFire) && !t.invulnerable)
		{
			t.flashAnimation = 1;
			if (!this.heavy)
			{
				this.vX = 0;
				this.vY = 0;
			}

			t.lives -= this.damage;

			Game.eventsOut.add(new EventTankUpdateHealth(t));

			if (t.lives <= 0)
			{
				t.flashAnimation = 0;
				t.destroy = true;

				if (this.tank.equals(Game.playerTank))
					Panel.panel.hotbar.currentCoins.coins += t.coinValue;
				else if (this.tank instanceof TankPlayerRemote && Crusade.crusadeMode)
				{
					((TankPlayerRemote) this.tank).player.coins.coins += t.coinValue;
					Game.eventsOut.add(new EventUpdateCoins(((TankPlayerRemote) this.tank).player));
				}
			}
			else if (this.playPopSound)
			{
				Drawing.drawing.playGlobalSound("bullet_explode.ogg", (float) (bullet_size / size));
			}
		}
		else if (this.playPopSound)
		{
			Drawing.drawing.playGlobalSound("bullet_explode.ogg", (float) (bullet_size / size));
		}
	}

	public void collidedWithObject(Movable o)
	{
		if (o instanceof Bullet && !((Bullet) o).enableExternalCollisions)
			return;

		if (o instanceof Bullet)
		{
			if (this.playPopSound)
				Drawing.drawing.playGlobalSound("bullet_explode.ogg", (float) (bullet_size / ((Bullet) o).size));
		}

		if (!heavy)
		{
			if (this.playPopSound)
				Drawing.drawing.playGlobalSound("bullet_explode.ogg", (float) (bullet_size / size));

			this.destroy = true;
			this.vX = 0;
			this.vY = 0;
		}

		if (heavy && o instanceof Bullet && ((Bullet)o).heavy)
		{
			if (this.playPopSound)
				Drawing.drawing.playGlobalSound("bullet_explode.ogg", (float) (bullet_size / size));

			this.destroy = true;
			this.vX = 0;
			this.vY = 0;
		}

		o.destroy = true;

		o.vX = 0;
		o.vY = 0;
	}

	public void checkCollisionLocal()
	{
		if (this.destroy)
			return;

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);

			if (!o.checkForObjects)
				continue;

			double dx = this.posX - o.posX;
			double dy = this.posY - o.posY;

			double horizontalDist = Math.abs(dx);
			double verticalDist = Math.abs(dy);

			double s = this.size;
			if (useCustomWallCollision)
				s = this.wallCollisionSize;

			double bound = s / 2 + Game.tile_size / 2;

			if (horizontalDist < bound && verticalDist < bound)
				o.onObjectEntryLocal(this);
		}
	}

	public void checkCollision()
	{
		if (this.destroy)
			return;

		boolean bouncy = false;

		boolean collided = false;

		double prevX = this.posX;
		double prevY = this.posY;

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);

			if (!o.bulletCollision && !o.checkForObjects)
				continue;

			double dx = this.posX - o.posX;
			double dy = this.posY - o.posY;

			double horizontalDist = Math.abs(dx);
			double verticalDist = Math.abs(dy);

			double s = this.size;

			if (useCustomWallCollision)
				s = this.wallCollisionSize;

			double bound = s / 2 + Game.tile_size / 2;

			if (horizontalDist < bound && verticalDist < bound)
			{
				if (o.checkForObjects)
					o.onObjectEntry(this);

				if (!o.bulletCollision)
					continue;

				boolean left = o.hasLeftNeighbor();
				boolean right = o.hasRightNeighbor();
				boolean up = o.hasUpperNeighbor();
				boolean down = o.hasLowerNeighbor();

				if (left && dx <= 0)
					horizontalDist = 0;

				if (right && dx >= 0)
					horizontalDist = 0;

				if (up && dy <= 0)
					verticalDist = 0;

				if (down && dy >= 0)
					verticalDist = 0;

				bouncy = o.bouncy;
				collided = true;

				if (!left && dx <= 0 && dx > 0 - bound && horizontalDist > verticalDist)
				{
					this.posX += 2 * (horizontalDist - bound);
					this.vX = -Math.abs(this.vX);
				}
				else if (!up && dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
				{
					this.posY += 2 * (verticalDist - bound);
					this.vY = -Math.abs(this.vY);
				}
				else if (!right && dx >= 0 && dx < bound && horizontalDist > verticalDist)
				{
					this.posX -= 2 * (horizontalDist - bound);
					this.vX = Math.abs(this.vX);
				}
				else if (!down && dy >= 0 && dy < bound && horizontalDist < verticalDist)
				{
					this.posY -= 2 * (verticalDist - bound);
					this.vY = Math.abs(this.vY);
				}
			}

		}

		if (this.posX + this.size/2 > Drawing.drawing.sizeX)
		{
			collided = true;
			this.posX = Drawing.drawing.sizeX - this.size/2 - (this.posX + this.size/2 - Drawing.drawing.sizeX);
			this.vX = -Math.abs(this.vX);
		}
		if (this.posX - this.size/2 < 0)
		{
			collided = true;
			this.posX = this.size/2 - (this.posX - this.size / 2);
			this.vX = Math.abs(this.vX);
		}
		if (this.posY + this.size/2 > Drawing.drawing.sizeY)
		{
			collided = true;
			this.posY = Drawing.drawing.sizeY - this.size/2 - (this.posY + this.size/2 - Drawing.drawing.sizeY);
			this.vY = -Math.abs(this.vY);
		}
		if (this.posY - this.size/2 < 0)
		{
			collided = true;
			this.posY = this.size/2 - (this.posY - this.size / 2);
			this.vY = Math.abs(this.vY);
		}

		if (collided && this.age == 0)
		{
			this.destroy = true;
			this.posX = prevX;
			this.posY = prevY;
			return;
		}

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable o = Game.movables.get(i);

			if (o instanceof Tank && !o.destroy)
			{
				double horizontalDist = Math.abs(this.posX - o.posX);
				double verticalDist = Math.abs(this.posY - o.posY);

				Tank t = ((Tank) o);

				double bound = this.size / 2 + t.size / 2;

				if (horizontalDist < bound && verticalDist < bound)
				{
					this.collidedWithTank(t);
				}
			}
			else if ((o instanceof Bullet || o instanceof Mine) && o != this && !o.destroy && !(o instanceof BulletFlame || this instanceof BulletFlame))
			{
				double horizontalDist = Math.abs(this.posX - o.posX);
				double verticalDist = Math.abs(this.posY - o.posY);

				double s = Bullet.bullet_size;
				if (o instanceof Mine)
					s = Mine.mine_size;

				double bound = this.size / 2 + s / 2;

				if (horizontalDist < bound && verticalDist < bound)
				{
					this.collidedWithObject(o);
				}
			}

		}


		if (collided)
		{
			if (!bouncy)
				this.bounces--;
			else
				this.bouncyBounces--;

			if (this.bounces < 0 || this.bouncyBounces < 0)
			{
				if (this.playPopSound)
					Drawing.drawing.playGlobalSound("bullet_explode.ogg", (float) (bullet_size / size));

				this.destroy = true;
				this.vX = 0;
				this.vY = 0;
			}
			else if (this.playBounceSound)
				Drawing.drawing.playGlobalSound("bounce.ogg", (float) (bullet_size / size));

			Game.eventsOut.add(new EventBulletUpdate(this));
		}
	}

	public Ray getRay()
	{
		Ray r = new Ray(posX, posY, this.getAngleInDirection(this.posX + this.vX, this.posY + this.vY), this.bounces, tank);
		r.size = this.size;
		return r;
	}

	@Override
	public void update()
	{
		super.update();

		if (destroy)
		{
			if (this.destroyTimer <= 0 && !freeIDs.contains(this.networkID))
			{
				if (!this.tank.isRemote)
					Game.eventsOut.add(new EventBulletDestroyed(this));

				freeIDs.add(this.networkID);
				idMap.remove(this.networkID);

				if (this.affectsMaxLiveBullets)
				{
					if (this.item == null)
						this.tank.liveBullets--;
					else
						this.item.liveBullets--;
				}

				this.onDestroy();
			}

			if (this.destroyTimer <= 0 && Game.fancyGraphics && !(this instanceof BulletFlame))
			{
				for (int i = 0; i < this.size * 4; i++)
				{
					Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
					double var = 50;
					e.maxAge /= 2;
					e.colR = Math.min(255, Math.max(0, this.baseColorR + Math.random() * var - var / 2));
					e.colG = Math.min(255, Math.max(0, this.baseColorG + Math.random() * var - var / 2));
					e.colB = Math.min(255, Math.max(0, this.baseColorB + Math.random() * var - var / 2));

					if (Game.enable3d)
						e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * this.size / 50.0 * 4);
					else
						e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);

					Game.effects.add(e);
				}
			}

			this.destroyTimer += Panel.frameFrequency;
			this.vX = 0;
			this.vY = 0;
		}
		else
		{
			double frac = 1 / (1 + this.age / 100);
			this.posZ = this.iPosZ * frac + (Game.tile_size / 4) * (1 - frac);

			this.ageFrac += Panel.frameFrequency;
			this.halfAgeFrac += Panel.frameFrequency;
			this.quarterAgeFrac += Panel.frameFrequency;

			if (Game.fancyGraphics)
			{
				while (this.ageFrac >= 1)
				{
					this.ageFrac -= 1;

					if (this.effect.equals(BulletEffect.trail) || this.effect.equals(BulletEffect.fire) || this.effect.equals(BulletEffect.darkFire))
						Game.effects.add(Effect.createNewEffect(this.posX - lastFinalVX * ageFrac, this.posY - lastFinalVY * ageFrac, this.posZ - lastFinalVZ, Effect.EffectType.trail, ageFrac));

					if (this.effect.equals(BulletEffect.ice))
					{
						Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
						double var = 50;
						e.maxAge /= 2;
						e.colR = Math.min(255, Math.max(0, 128 + Math.random() * var - var / 2));
						e.colG = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
						e.colB = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));

						if (Game.enable3d)
							e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);
						else
							e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);


						Game.effects.add(e);
					}
				}

				while (this.quarterAgeFrac >= 0.25)
				{
					this.quarterAgeFrac -= 0.25;

					//if (this.effect.equals(BulletEffect.fireTrail))
					//	Game.effects.add(Effect.createNewEffect(this.posX - lastFinalVX * quarterAgeFrac, this.posY - lastFinalVY * quarterAgeFrac, this.posZ - lastFinalVZ, Effect.EffectType.smokeTrail, quarterAgeFrac));

					if (this.effect.equals(BulletEffect.fire) || this.effect.equals(BulletEffect.fireTrail))
						Game.effects.add(Effect.createNewEffect(this.posX - lastFinalVX * quarterAgeFrac, this.posY - lastFinalVY * quarterAgeFrac, this.posZ - lastFinalVZ, Effect.EffectType.fire, quarterAgeFrac));

					if (this.effect.equals(BulletEffect.darkFire))
						Game.effects.add(Effect.createNewEffect(this.posX - lastFinalVX * quarterAgeFrac, this.posY - lastFinalVY * quarterAgeFrac, this.posZ - lastFinalVZ, Effect.EffectType.darkFire, quarterAgeFrac));
				}

				while (this.halfAgeFrac >= 0.5)
				{
					this.halfAgeFrac -= 0.5;

					if (this.effect.equals(BulletEffect.fireTrail))
						Game.effects.add(Effect.createNewEffect(this.posX - lastFinalVX * halfAgeFrac, this.posY - lastFinalVY * halfAgeFrac, this.posZ - lastFinalVZ, Effect.EffectType.smokeTrail, halfAgeFrac));
				}
			}
		}

		if (this.destroyTimer >= 60)
		{
			Game.removeMovables.add(this);
		}

		if (!this.tank.isRemote)
			this.checkCollision();
		else
			this.checkCollisionLocal();

		this.age += Panel.frameFrequency;
	}

	@Override
	public void draw()
	{
		double opacity = ((60 - destroyTimer) / 60.0);
		double sizeModifier = destroyTimer * (size / Bullet.bullet_size);
		Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, (int)(opacity * opacity * opacity * 255.0));

		if (Game.enable3d)
			Drawing.drawing.fillOval(posX, posY, posZ, size + sizeModifier, size + sizeModifier);
		else
			Drawing.drawing.fillOval(posX, posY, size + sizeModifier, size + sizeModifier);

		Drawing.drawing.setColor(this.baseColorR, this.baseColorG, this.baseColorB, (int)(opacity * opacity * opacity * 255.0));

		if (Game.enable3d)
			Drawing.drawing.fillOval(posX, posY, posZ + 1, (size + sizeModifier) * 0.6, (size + sizeModifier) * 0.6);
		else
			Drawing.drawing.fillOval(posX, posY, (size + sizeModifier) * 0.6, (size + sizeModifier) * 0.6);

	}

	@Override
	public void addAttribute(AttributeModifier m)
	{
		super.addAttribute(m);

		if (!this.isRemote)
			Game.eventsOut.add(new EventBulletAddAttributeModifier(this, m, false));
	}

	@Override
	public void addUnduplicateAttribute(AttributeModifier m)
	{
		super.addUnduplicateAttribute(m);

		if (!this.isRemote)
			Game.eventsOut.add(new EventBulletAddAttributeModifier(this, m, true));
	}

	public void onDestroy()
	{

	}
}
