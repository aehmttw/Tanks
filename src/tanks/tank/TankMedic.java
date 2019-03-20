package tanks.tank;

import tanks.Bullet;
import tanks.BulletHealing;
import tanks.Game;
import tanks.Movable;
import tanks.Ray;
import tanks.Team;

public class TankMedic extends TankAIControlled
{
	boolean lineOfSight = false;
	
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
	public void shoot() 
	{
		if (this.cooldown > 0)
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
	
}
