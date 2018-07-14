package tanks;

import java.awt.Color;

public class EnemyTankGreen extends EnemyTank
{
	public EnemyTankGreen(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, new Color(100, 200, 0), angle, ShootAI.reflect);
		this.enableMovement = false;
		this.enableMineLaying = false;
		this.liveBulletMax = 2;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.aimTurretSpeed = 0.02;
		this.bulletBounces = 2;
		this.bulletColor = Color.red;
		this.bulletSpeed = 25.0 / 2;
		this.bulletEffect = Bullet.BulletEffect.fireTrail;
		this.turretIdleTimerBase = 25;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtPlayer = false;
		
		this.coinValue = 4;
	}
}
