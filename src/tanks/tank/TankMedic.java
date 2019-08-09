package tanks.tank;

import tanks.Bullet;
import tanks.BulletHealing;
import tanks.Game;
import tanks.Mine;
import tanks.Movable;
import tanks.Panel;
import tanks.Ray;
import tanks.Team;

public class TankMedic extends TankAIControlled
{
	boolean lineOfSight = false;

	boolean suicidal = false;
	double timeUntilDeath = 500 + Math.random() * 250;

	public TankMedic(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, 255, 255, 255, angle, ShootAI.straight);

		this.texture = "/tanks/resources/medic.png";
		this.enableMovement = true;
		this.speed = 1;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.cooldownRandom = 0;
		this.cooldownBase = 0;
		this.aimTurretSpeed = 0.02;
		this.bulletBounces = 0;
		this.bulletEffect = Bullet.BulletEffect.none;
		this.bulletDamage = 0;
		this.motionChangeChance = 0.001;

		this.coinValue = 8;
	}

	@Override
	public void postUpdate()
	{
		if (!this.suicidal)
		{
			boolean die = true;
			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				if (m != this && m.team == this.team && !(m instanceof TankMedic))
				{
					die = false;
					break;
				}
			}

			if (die)
				this.suicidal = true;
		}

		if (this.suicidal)
		{
			this.timeUntilDeath -= Panel.frameFrequency;
			this.speed = 3 - 2 * Math.min(this.timeUntilDeath, 500) / 500;
			this.enableBulletAvoidance = false;
			this.enableMineAvoidance = false;
		}
			
		if (this.timeUntilDeath < 500)
		{
			this.colorG = this.timeUntilDeath / 500 * 255;
			this.colorB = this.timeUntilDeath / 500 * 255;

			if (this.timeUntilDeath < 150 && ((int) this.timeUntilDeath % 16) / 8 == 1)
			{
				this.colorR = 255;
				this.colorG = 255;
				this.colorB = 0;
			}
		}

		if (this.timeUntilDeath <= 0)
		{
			Game.movables.add(new Mine(this.posX, this.posY, 0, this));
			this.destroy = true;
			this.lives = 0;
		}
	}

	@Override
	public void shoot() 
	{
		if (this.cooldown > 0 || this.suicidal)
			return;

		Ray r = new Ray(this.posX, this.posY, this.angle, this.bulletBounces, this);
		r.moveOut(5);
		if (!this.hasTarget || r.getTarget() != this.targetEnemy)
			return;

		BulletHealing b = new BulletHealing(this.posX, this.posY, this.bulletBounces, this);
		b.team = this.team;
		b.setPolarMotion(this.angle, 25.0/4);
		b.moveOut(8);
		Game.movables.add(b);
		this.cooldown = this.cooldownBase;
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

			if (m instanceof Tank && m != this && Team.isAllied(this, m) && m.hiddenTimer <= 0 && !((Tank) m).invulnerable && ((Tank) m).lives - ((Tank) m).baseLives < 1)
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
		if (this.suicidal)
		{
			this.overrideDirection = true;
			this.setMotionInDirection(targetEnemy.posX, targetEnemy.posY, speed);
		}
	}

}
