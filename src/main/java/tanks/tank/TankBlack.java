package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

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
		this.bullet.speed = 25.0 / 4;
		this.bullet.bounces = 0;
		this.bullet.effect = Bullet.BulletEffect.darkFire;
		this.bullet.name = "Dark fire bullet";
		this.turretAimSpeed = 0.06;
		this.enablePathfinding = true;
		this.targetEnemySightBehavior = TargetEnemySightBehavior.strafe;
		this.avoidanceSeekOpenSpaces = true;
		this.bulletAvoidBehvavior = BulletAvoidBehavior.back_off;

		this.coinValue = 10;

		this.description = "A smart, very fast tank which fires rockets";
	}
}
