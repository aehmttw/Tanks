package tanks.tank;

import tanks.Game;

/**
 * An invisible smart tank.
 */
public class TankWhite extends TankAIControlled
{
	public TankWhite(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 255, 255, 255, angle, ShootAI.alternate);
		this.maxSpeed = 1.0;
		this.enableDefensiveFiring = true;
		this.enablePathfinding = true;
		this.invisible = true;

		this.coinValue = 10;

		this.turretAimSpeed = 0.045;
		this.turretIdleSpeed = 0.001;
		this.avoidanceSeekOpenSpaces = true;
		this.bulletAvoidBehvavior = BulletAvoidBehavior.dodge;

		this.description = "An invisible smart tank";
	}
}
