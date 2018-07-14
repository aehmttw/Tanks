package tanks;

import java.awt.Color;

public class EnemyTankMini extends EnemyTank
{
	public EnemyTankPink tank;
	public boolean previousDestroy = false;
	
	public EnemyTankMini(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size / 2, new Color(255, 127, 127), angle, ShootAI.straight);

		this.enableMovement = true;
		this.speed = 2.5;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.aimTurretSpeed = 0.02;
		this.bulletBounces = 0;
		this.bulletColor = Color.blue;
		this.bulletEffect = Bullet.BulletEffect.none;
		this.bulletSpeed = 25.0 / 4;
		this.bulletDamage = 0.25;
		this.bulletSize /= 2;
		this.enableLookingAtPlayer = true;
		this.motionChangeChance = 0.001;
		this.enableBulletAvoidance = false;
	}
	
	public EnemyTankMini(String name, double x, double y, double angle, EnemyTankPink t)
	{
		this(name, x, y, angle);
		this.tank = t;
		t.spawnedMinis++;
		
		this.turret.size /= 2;
		this.turret.length /= 2;
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

		
		if (this.tank != null)
		{
			if (!this.tank.destroy && Math.sqrt(Math.pow(this.posX - this.tank.posX, 2) + Math.pow(this.posY - this.tank.posY, 2)) > 300)
			{
				this.setMotionInDirection(this.tank.posX, this.tank.posY, this.speed);
			}
		}
		
	}
}
