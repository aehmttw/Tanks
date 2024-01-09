package tanks.tank;

import tanks.Game;

/**
 * A smart, fast tank which can lay mines
 */
public class TankPurple extends TankAIControlled
{
	public TankPurple(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 150, 0, 200, angle, ShootAI.alternate);
		this.enableDefensiveFiring = true;
		this.cooldownBase = 20;
		this.cooldownRandom = 40;
		this.enablePathfinding = true;

		this.turretAimSpeed = 0.045;
		this.turretIdleSpeed = 0.001;
		this.avoidanceSeekOpenSpaces = true;
		this.bulletAvoidBehvavior = BulletAvoidBehavior.dodge;

		this.coinValue = 10;

		this.description = "A smart, fast tank which can lay mines";
	}
}
