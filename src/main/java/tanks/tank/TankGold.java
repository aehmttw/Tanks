package tanks.tank;

import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.Team;
import tanks.bullet.Bullet;
import tanks.bullet.BulletBoost;
import tanks.event.EventLayMine;
import tanks.event.EventTankUpdateColor;

public class TankGold extends TankAIControlled
{
	boolean suicidal = false;
	double timeUntilDeath = 500 + this.random.nextDouble() * 250;

	public TankGold(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 255, 180, 0, angle, ShootAI.straight);

		this.enableMineLaying = false;
		this.enablePredictiveFiring = true;
		this.enableDefensiveFiring = true;
		this.cooldownBase = 50;
		this.cooldownRandom = 0;
		this.liveBulletMax = 5;
		this.aimTurretSpeed = 0.03;
		this.bulletBounces = 0;
		this.bulletEffect = Bullet.BulletEffect.ember;
		this.bulletClass = BulletBoost.class;
		this.bulletDamage = 0;
		this.enablePathfinding = true;
		this.seekChance = 0.01;
		this.bulletSpeed = 25 / 4.0;
		this.dealsDamage = false;
		this.aimAccuracyOffset = 0;

		this.coinValue = 4;

		this.description = "A tank which speeds up---its allies and becomes---explosive as a last stand";
	}

	@Override
	public void postUpdate()
	{
		if (!this.suicidal)
		{
			boolean shouldSuicide = true;
			for (Tank t : Tank.idMap.values())
			{
				if (!(t instanceof TankMedic || t instanceof TankGold) && Team.isAllied(this, t))
				{
					shouldSuicide = false;
					break;
				}
			}

			this.suicidal = shouldSuicide;
		}

		if (this.suicidal)
		{
			this.timeUntilDeath -= Panel.frameFrequency;
			this.maxSpeed = 4.5 - 3 * Math.min(this.timeUntilDeath, 500) / 500;
			this.enableBulletAvoidance = false;
			this.enableMineAvoidance = false;
		}

		if (this.timeUntilDeath < 500)
		{
			this.colorG = this.timeUntilDeath / 500 * 180;

			if (this.timeUntilDeath < 150 && ((int) this.timeUntilDeath % 16) / 8 == 1)
			{
				this.colorR = 255;
				this.colorG = 255;
				this.colorB = 0;
			}

			if (shouldSendEvent)
				Game.eventsOut.add(new EventTankUpdateColor(this));
		}

		if (this.timeUntilDeath <= 0 && !this.disabled)
		{
			Mine m = new Mine(this.posX, this.posY, 0, this);
			m.radius *= 1.5;
			Game.eventsOut.add(new EventLayMine(m));
			Game.movables.add(m);
			this.destroy = true;
			this.health = 0;
		}
	}

	@Override
	public void shoot()
	{
		if (this.cooldown > 0 || this.suicidal || this.disabled || this.destroy || this.liveBullets >= this.liveBulletMax)
			return;

		Ray r = new Ray(this.posX, this.posY, this.angle, this.bulletBounces, this);
		r.moveOut(5);

		if (this.avoidTimer <= 0 && (!this.hasTarget || r.getTarget() != this.targetEnemy))
			return;

		super.shoot();
	}

	@Override
	public void updateTarget()
	{
		if (this.suicidal)
		{
			super.updateTarget();
			return;
		}

		double nearestDist = Double.MAX_VALUE;
		Movable nearest = null;
		this.hasTarget = false;

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			if (m instanceof Tank && m != this && Team.isAllied(this, m) && ((Tank) m).targetable && !((Tank) m).hidden && !((Tank) m).invulnerable && ((Tank) m).health - ((Tank) m).baseHealth < 1)
			{
				Ray r = new Ray(this.posX, this.posY, this.getAngleInDirection(m.posX, m.posY), 0, this);
				r.moveOut(5);
				if (r.getTarget() != m)
					continue;

				double distance = Movable.distanceBetween(this, m);

				if (distance < nearestDist)
				{
					this.hasTarget = true;
					nearestDist = distance;
					nearest = m;
				}
			}
		}

		this.targetEnemy = nearest;
	}

	public void reactToTargetEnemySight()
	{
		if (this.suicidal && this.targetEnemy != null)
		{
			this.overrideDirection = true;
			this.setAccelerationInDirection(targetEnemy.posX, targetEnemy.posY, acceleration);
		}
		else
		{
			if (this.targetEnemy == null)
				return;

			this.overrideDirection = true;
			if (Movable.distanceBetween(this, this.targetEnemy) < Game.tile_size * 6)
				this.setAccelerationAwayFromDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);
			else
				this.setAccelerationInDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);

		}
	}

	public boolean isInterestingPathTarget(Movable m)
	{
		return m instanceof Tank && Team.isAllied(m, this) && m != this && ((Tank) m).health - ((Tank) m).baseHealth < 1 && !(m instanceof TankGold);
	}

}