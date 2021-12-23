package tanks.tank;

import tanks.Game;

public class TankMagenta extends TankAIControlled
{
	public TankMagenta(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 230, 0, 200, angle, ShootAI.reflect);
		this.enableTargetEnemyReaction = true;
		this.maxSpeed = 1.0;
		this.enableMineLaying = false;
		this.liveBulletMax = 3;
		this.cooldownRandom = 20;
		this.cooldownBase = 40;
		this.avoidSensitivity = 1.25;
		
		this.coinValue = 3;

		this.description = "A medium-speed smart tank";
	}
}
