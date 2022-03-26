package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

/**
 * A primitive stationary tank
 */
public class TankTest extends TankAIControlled
{

	public TankTest(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 200, 140, 60, angle, ShootAI.straight);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.liveBulletMax = 5;
		this.cooldownRandom = 0;
		this.cooldownBase = 20;
		this.idleTurretSpeed = 0.01;
		this.bulletBounces = 1;
		this.turretIdleTimerBase = 500;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;
		this.enablePredictiveFiring = true;
		this.enableDefensiveFiring = true;

		this.bulletBounces = 0;
		this.bulletEffect = Bullet.BulletEffect.fire;
		this.bulletSpeed = 25.0 / 4;

		this.coinValue = 5;

		this.description = "A primitive stationary tank";
	}
}
