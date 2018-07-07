package tanks;

import java.awt.Color;

public class EnemyTankMini extends EnemyTankDynamic
{
	public EnemyTankPink tank;
	public boolean previousDestroy = false;
	
	public EnemyTankMini(double x, double y, double angle)
	{
		super(x, y, Game.tank_size / 2, new Color(255, 127, 127), angle, ShootAI.straight);

		this.enableMovement = true;
		this.speed = 2.5;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.idleTurretSpeed = 0.02;
		this.bulletBounces = 0;
		this.bulletColor = Color.blue;
		this.bulletEffect = Bullet.BulletEffect.none;
		this.bulletSpeed = 25.0 / 4;
		this.bulletDamage = 0.25;
		this.bulletSize /= 2;
		this.enableLookingAtPlayer = false;
		this.motionChangeChance = 0.001;
		this.enableBulletAvoidance = false;
		
		this.coinValue = 2;
	}
	
	public EnemyTankMini(double x, double y, double angle, EnemyTankPink t)
	{
		this(x, y, angle);
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
	}
}
