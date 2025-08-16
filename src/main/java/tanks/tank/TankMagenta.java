package tanks.tank;

import tanks.Game;

/**
 * A medium-speed smart tank
 */
public class TankMagenta extends TankAIControlled
{
	public TankMagenta(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 230, 0, 200, angle, ShootAI.reflect);
		this.enableTargetEnemyReaction = true;
		this.maxSpeed = 1.0;
		this.enableMineLaying = false;
		this.getBullet().maxLiveBullets = 3;
		this.cooldownRandom = 20;
		this.cooldownBase = 40;
		this.mineAvoidSensitivity = 1.25;
		
		this.coinValue = 3;

		if (Game.tankTextures)
		{
			this.baseSkin = TankModels.cross;
		}

		this.description = "A medium-speed smart tank";
	}
}
