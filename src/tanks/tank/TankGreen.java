package tanks.tank;

import java.awt.Color;

import tanks.Bullet;
import tanks.Game;

public class TankGreen extends EnemyTank
{
	public TankGreen(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, new Color(100, 200, 0), angle, ShootAI.reflect);
		this.enableMovement = false;
		this.enableMineLaying = false;
		this.liveBulletMax = 4;
		this.cooldownRandom = 20;
		this.cooldownBase = 80;
		this.aimTurretSpeed = 0.03;
		this.bulletBounces = 2;
		this.bulletColor = Color.red;
		this.bulletSpeed = 25.0 / 2;
		this.bulletEffect = Bullet.BulletEffect.fireTrail;
		this.turretIdleTimerBase = 25;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;
		
		this.coinValue = 4;
	}
}
