package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

/**
 * A deadly stationary tank which shoots rockets that bounce twice
 */
public class TankGreen extends TankAIControlled
{
	public TankGreen(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 100, 200, 0, angle, ShootAI.reflect);
		this.enableMovement = false;
		this.enableMineLaying = false;
		this.bullet.maxLiveBullets = 4;
		this.cooldownRandom = 20;
		this.cooldownBase = 80;
		this.turretAimSpeed = 0.03;
		this.bullet.bounces = 2;
		this.bullet.speed = 25.0 / 4;
		this.bullet.effect = Bullet.BulletEffect.fireTrail;
		this.bullet.name = "Bouncy fire bullet";
		this.turretIdleTimerBase = 25;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;
		this.enableDefensiveFiring = true;

		this.coinValue = 10;

		this.description = "A deadly stationary tank which shoots rockets that bounce twice";
	}
}
