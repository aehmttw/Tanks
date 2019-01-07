package tanks;

import java.awt.Color;
import java.awt.Graphics;

import tanks.tank.Tank;


public class Bullet extends Movable
{
	public static enum BulletEffect {none, fire, darkFire, fireTrail, ice, trail};

	public static int bullet_size = 10;

	public boolean playPopSound = true;
	public double age = 0;
	public double size;
	public int bounces;
	public Color baseColor;
	public Color outlineColor;
	public double destroyTimer = 0;
	public Tank tank;
	public double damage = 1;
	public BulletEffect effect = BulletEffect.none;
	public boolean useCustomWallCollision = false;
	public double wallCollisionSize = 10;
	public boolean heavy = false;
	public ItemBullet item;
	public double recoil = 1.0;

	public boolean affectsMaxLiveBullets = true;

	public Bullet(double x, double y, int bounces, Tank t)
	{
		super(x, y);
		this.vX = 0;
		this.vY = 0;
		this.size = bullet_size;
		this.baseColor = t.color;
		this.outlineColor = Team.getObjectColor(t.turret.color, t);
		this.bounces = bounces;
		this.tank = t;
		this.team = t.team;

		t.liveBullets++;
	}

	/** Do not use, instead use the constructor with primitive data types. Intended for Item use only!*/
	@Deprecated
	public Bullet(Double x, Double y, Integer bounces, Tank t, ItemBullet ib) 
	{
		this(x.doubleValue(), y.doubleValue(), bounces.intValue(), t, false);
		this.item = ib;
	}

	public Bullet(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets)
	{
		this(x, y, bounces, t);

		this.affectsMaxLiveBullets = affectsMaxLiveBullets;

		if (!this.affectsMaxLiveBullets)
			t.liveBullets--;
	}

	public void moveOut(int amount)
	{
		this.moveInDirection(vX, vY, amount);
	}

	public void collidedWithTank(Tank t)
	{
		if (!heavy)
			this.destroy = true;

		if (!(Team.isAllied(this, t) && !this.team.friendlyFire))
		{
			t.flashAnimation = 1;
			if (!this.heavy)
			{
				this.vX = 0;
				this.vY = 0;
			}
			
			t.lives -= this.damage;

			if (t.lives <= 0)
			{
				t.flashAnimation = 0;
				t.destroy = true;

				if (this.tank.equals(Game.player))
					Panel.panel.hotbar.currentCoins.coins += t.coinValue;
			}
		}
	}

	public void collidedWithObject(Movable o)
	{
		if (this.playPopSound)
			Drawing.playSound("resources/bullet_explode.wav");

		if (!heavy)
		{
			this.destroy = true;
			this.vX = 0;
			this.vY = 0;
		}

		o.destroy = true;

		o.vX = 0;
		o.vY = 0;
	}

	@Override
	public void checkCollision() 
	{
		if (this.destroy)
			return;

		boolean collided = false;

		double prevX = this.posX;
		double prevY = this.posY;

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);

			if (!o.bulletCollision && !o.checkForObjects)
				continue;

			double horizontalDist = Math.abs(this.posX - o.posX);
			double verticalDist = Math.abs(this.posY - o.posY);

			double dx = this.posX - o.posX;
			double dy = this.posY - o.posY;

			double s = this.size;
			if (useCustomWallCollision)
				s = this.wallCollisionSize;

			double bound = s / 2 + Obstacle.obstacle_size / 2;

			if (horizontalDist < bound && verticalDist < bound)
			{
				if (o.checkForObjects)
					o.onObjectEntry(this);
				
				if (!o.bulletCollision)
					continue;
					
				if (dx <= 0 && dx > 0 - bound && horizontalDist > verticalDist)
				{
					this.posX += horizontalDist - bound;
					this.vX = -Math.abs(this.vX);
					collided = true;
				}
				else if (dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
				{
					this.posY += verticalDist - bound;
					this.vY = -Math.abs(this.vY);
					collided = true;
				}
				else if (dx >= 0 && dx < bound && horizontalDist > verticalDist)
				{
					this.posX -= horizontalDist - bound;
					this.vX = Math.abs(this.vX);
					collided = true;
				}
				else if (dy >= 0 && dy < bound && horizontalDist < verticalDist)
				{
					this.posY -= verticalDist - bound;
					this.vY = Math.abs(this.vY);
					collided = true;
				}
			}

		}

		if (this.posX + this.size/2 > Drawing.sizeX)
		{
			collided = true;
			this.posX = Drawing.sizeX - this.size/2 - (this.posX + this.size/2 - Drawing.sizeX);
			this.vX = -Math.abs(this.vX);
		}
		if (this.posX - this.size/2 < 0)
		{
			collided = true;
			this.posX = this.size/2 - (this.posX - this.size / 2);
			this.vX = Math.abs(this.vX);
		}
		if (this.posY + this.size/2 > Drawing.sizeY)
		{
			collided = true;
			this.posY = Drawing.sizeY - this.size/2 - (this.posY + this.size/2 - Drawing.sizeY);
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
				if (!o.destroy)
				{
					double horizontalDist = Math.abs(this.posX - o.posX);
					double verticalDist = Math.abs(this.posY - o.posY);

					int s = Bullet.bullet_size;
					if (o instanceof Mine)
						s = Mine.mine_size;

					double bound = this.size / 2 + s / 2;

					if (horizontalDist < bound && verticalDist < bound)
					{
						collidedWithObject(o);
					}
				}
			}

		}


		if (collided)
		{

			if (this.bounces <= 0)
			{
				if (this.playPopSound)
					Drawing.playSound("resources/bullet_explode.wav");

				this.destroy = true;
				this.vX = 0;
				this.vY = 0;
			}
			else
				Drawing.playSound("resources/bounce.wav");

			this.bounces--;
		}
	}

	public Ray getRay()
	{
		Ray r = new Ray(posX, posY, this.getAngleInDirection(this.posX + this.vX, this.posY + this.vY), this.bounces, tank);
		r.skipSelfCheck = true;
		return r;
	}

	@Override
	public void update()
	{
		if (destroy)
		{
			if (this.destroyTimer <= 0 && Game.fancyGraphics && !(this instanceof BulletFlame))
			{
				for (int i = 0; i < this.size * 4; i++)
				{
					Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
					int var = 50;
					e.maxAge /= 2;
					e.col = new Color((int) Math.min(255, Math.max(0, this.baseColor.getRed() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.baseColor.getGreen() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.baseColor.getBlue() + Math.random() * var - var / 2)));
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
			/*if (this.age > 80)
			{
				this.destroy = true;

				Bullet b = new Bullet(this.posX, this.posY, 1, this.tank);
				b.vX = this.vX;
				b.vY = this.vY;
				b.addPolarMotion(this.getPolarDirection() + Math.PI / 2, 5);
				b.moveOut(2);

				Bullet b2 = new Bullet(this.posX, this.posY, 1, this.tank);
				b2.vX = this.vX;
				b2.vY = this.vY;
				b2.addPolarMotion(this.getPolarDirection() - Math.PI / 2, 5);
				b2.moveOut(2);

				Game.movables.add(b);
				Game.movables.add(b2);
			}*/

			if (Game.fancyGraphics)
			{
				if (this.effect.equals(BulletEffect.trail) || this.effect.equals(BulletEffect.fire) || this.effect.equals(BulletEffect.darkFire))
					Game.effects.add(Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.trail));

				if (this.effect.equals(BulletEffect.fireTrail))
				{	
					Game.effects.add(Effect.createNewEffect(this.posX - this.vX * Panel.frameFrequency / 8, this.posY - this.vY * Panel.frameFrequency / 8, Effect.EffectType.smokeTrail, 0.25, 0.75));
					Game.effects.add(Effect.createNewEffect(this.posX - this.vX * Panel.frameFrequency / 4, this.posY - this.vY * Panel.frameFrequency / 4, Effect.EffectType.smokeTrail, 0.25, 0.50));
					Game.effects.add(Effect.createNewEffect(this.posX - this.vX * Panel.frameFrequency / 8 * 3, this.posY - this.vY * Panel.frameFrequency / 8 * 3, Effect.EffectType.smokeTrail, 0.25, 0.25));
					Game.effects.add(Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.smokeTrail, 0.25, 0));
				}

				if (this.effect.equals(BulletEffect.fire) || this.effect.equals(BulletEffect.fireTrail))
				{
					Game.effects.add(Effect.createNewEffect(this.posX - this.vX * Panel.frameFrequency / 8, this.posY - this.vY * Panel.frameFrequency / 8, Effect.EffectType.fire, 0.25, 0.75));
					Game.effects.add(Effect.createNewEffect(this.posX - this.vX * Panel.frameFrequency / 4, this.posY - this.vY * Panel.frameFrequency / 4, Effect.EffectType.fire, 0.25, 0.50));
					Game.effects.add(Effect.createNewEffect(this.posX - this.vX * Panel.frameFrequency / 8 * 3, this.posY - this.vY * Panel.frameFrequency / 8 * 3, Effect.EffectType.fire, 0.25, 0.25));
					Game.effects.add(Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.fire, 0.25, 0));
				}

				if (this.effect.equals(BulletEffect.darkFire))
				{
					Game.effects.add(Effect.createNewEffect(this.posX - this.vX * Panel.frameFrequency / 8, this.posY - this.vY * Panel.frameFrequency / 8, Effect.EffectType.darkFire, 0.25, 0.75));
					Game.effects.add(Effect.createNewEffect(this.posX - this.vX * Panel.frameFrequency / 4, this.posY - this.vY * Panel.frameFrequency / 4, Effect.EffectType.darkFire, 0.25, 0.50));
					Game.effects.add(Effect.createNewEffect(this.posX - this.vX * Panel.frameFrequency / 8 * 3, this.posY - this.vY * Panel.frameFrequency / 8 * 3, Effect.EffectType.darkFire, 0.25, 0.25));
					Game.effects.add(Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.darkFire, 0.25, 0));
				}

				if (this.effect.equals(BulletEffect.ice))
				{
					Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
					int var = 50;
					e.maxAge /= 2;
					e.col = new Color((int) Math.min(255, Math.max(0, 128 + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2)));
					e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);
					Game.effects.add(e);
				}
			}
		}

		if (this.destroyTimer >= 60)
		{
			if (this.affectsMaxLiveBullets)
				this.tank.liveBullets--;

			if (this.item != null)
				this.item.liveBullets--;

			Game.removeMovables.add(this);
		}

		super.update();

		this.age += Panel.frameFrequency;
	}

	@Override
	public void draw(Graphics p) 
	{
		double opacity = ((60 - destroyTimer) / 60.0);
		double sizeModifier = destroyTimer * (size / Bullet.bullet_size);
		p.setColor(new Color(this.outlineColor.getRed(), this.outlineColor.getGreen(), this.outlineColor.getBlue(), (int)(opacity * opacity * opacity * 255.0)));
		Drawing.window.fillOval(p, posX, posY, size + sizeModifier, size + sizeModifier);
		p.setColor(new Color(this.baseColor.getRed(), this.baseColor.getGreen(), this.baseColor.getBlue(), (int)(opacity * opacity * opacity * 255.0)));
		Drawing.window.fillOval(p, posX, posY, (size + sizeModifier) * 0.6, (size + sizeModifier) * 0.6);

	}

}
