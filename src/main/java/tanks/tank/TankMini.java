package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

public class TankMini extends TankAIControlled
{
	public TankPink tank;
	public boolean previousDestroy = false;
	
	public TankMini(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size / 2, 255, 127, 127, angle, ShootAI.straight);

		this.enableMovement = true;
		this.speed = 3.0;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.aimTurretSpeed = 0.02;
		this.bulletBounces = 0;
		this.bulletEffect = Bullet.BulletEffect.none;
		this.bulletSpeed = 25.0 / 4;
		this.bulletDamage = 0.25;
		this.bulletSize /= 2;
		this.enableLookingAtTargetEnemy = true;
		this.motionChangeChance = 0.001;
		this.enableBulletAvoidance = false;
		this.lives = 0.25;
		this.baseLives = 0.25;

		this.description = "A small, primitive tank which---shoots tiny, low damage bullets";
	}
	
	public TankMini(String name, double x, double y, double angle, TankPink t)
	{
		this(name, x, y, angle);
		this.tank = t;
		t.spawnedMinis++;
		
		this.turret.size /= 2;
		this.turret.length /= 2;
		
		this.team = t.team;
	}
	
	@Override
	public void update()
	{
		if (this.destroy && !previousDestroy)
		{
			this.previousDestroy = true;
			
			if (tank != null)
				tank.spawnedMinis--;
		}
		
		super.update();	
	}
	
	@Override
	public void postUpdate()
	{
		if (this.tank != null && !seesTargetEnemy)
		{
			if (!this.tank.destroy && Math.sqrt(Math.pow(this.posX - this.tank.posX, 2) + Math.pow(this.posY - this.tank.posY, 2)) > 300)
			{
				this.overrideDirection = true;
				this.setMotionInDirection(this.tank.posX, this.tank.posY, this.speed);
			}
		}
		
	}
	
}
