package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultBullets;

/**
 * A tank which speeds up its allies and becomes explosive as a last stand
 */
public class TankGold extends TankAIControlled
{
	public TankGold(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 255, 180, 0, angle, ShootAI.straight);

		this.enableMineLaying = false;
		this.enablePredictiveFiring = true;
		this.enableDefensiveFiring = true;
		this.cooldownBase = 40;
		this.cooldownRandom = 0;
		this.turretAimSpeed = 0.04;
		this.enablePathfinding = true;
		this.seekChance = 0.01;
		this.setBullet(DefaultBullets.booster_bullet);
		this.enableSuicide = true;
		this.targetEnemySightBehavior = TargetEnemySightBehavior.keep_distance;
		this.mine.explosion.radius *= 1.5;

		if (Game.tankTextures)
		{
			this.colorModel = TankModels.arrow.color;
		}

		this.coinValue = 4;

		this.description = "A tank which speeds up its allies and becomes explosive as a last stand";
	}
}