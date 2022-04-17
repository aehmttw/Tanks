package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

/**
 * A fast tank which rapidly fires many small, low-damage bullets
 */
public class TankDarkGreen extends TankAIControlled
{
	public TankDarkGreen(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 85, 107, 47, angle, ShootAI.straight);
		this.cooldownBase = 5;
		this.cooldownRandom = 0;
		this.maxSpeed = 1.5;
		this.bullet.speed = 25.0 / 4;
		this.aimAccuracyOffset = 0.1;
		this.bullet.maxLiveBullets = 8;
		this.bullet.bounces = 0;
		this.bullet.damage /= 8;
		this.bullet.size /= 2;
		this.bullet.effect = Bullet.BulletEffect.trail;
		this.bullet.name = "Mini bullet";
		this.targetEnemySightBehavior = TargetEnemySightBehavior.flee;
		
		this.coinValue = 10;

		this.description = "A fast tank which rapidly fires---many small, low-damage bullets";
	}
}
