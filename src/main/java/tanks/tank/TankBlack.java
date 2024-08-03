package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.DefaultBullets;

/**
 * A smart, very fast tank which fires rockets
 */
public class TankBlack extends TankAIControlled
{
	public TankBlack(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 0, 0, 0, angle, ShootAI.straight);
		this.cooldownBase = 75;
		this.cooldownRandom = 0;
		this.maxSpeed = 2.0;
		this.enableDefensiveFiring = true;

		this.setBullet(DefaultBullets.void_rocket);

		this.turretAimSpeed = 0.06;
		this.enablePathfinding = true;
		this.targetEnemySightBehavior = TargetEnemySightBehavior.strafe;
		this.avoidanceSeekOpenSpaces = true;
		this.bulletAvoidBehvavior = BulletAvoidBehavior.back_off;

		this.coinValue = 10;

		this.description = "A smart, very fast tank which fires rockets";
	}
}
