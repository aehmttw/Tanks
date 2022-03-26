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
		this.liveBulletMax = 4;
		this.cooldownRandom = 20;
		this.cooldownBase = 80;
		this.aimTurretSpeed = 0.03;
		this.bulletBounces = 2;
		this.bulletSpeed = 25.0 / 4;
		this.bulletEffect = Bullet.BulletEffect.fireTrail;
		this.turretIdleTimerBase = 25;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;
		this.enableDefensiveFiring = true;

		this.coinValue = 10;

		this.description = "A deadly stationary tank which---shoots rockets that bounce twice";
	}
}
