package tanks.tank;

import tanks.Game;

public class TankPurple extends TankAIControlled
{
	public TankPurple(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 150, 0, 200, angle, ShootAI.alternate);
		this.enableDefensiveFiring = true;
		this.cooldownBase = 20;
		this.cooldownRandom = 40;
		this.enablePathfinding = true;

		this.coinValue = 10;

		this.description = "A smart, fast tank---which can lay mines";
	}
}
