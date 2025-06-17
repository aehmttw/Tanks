package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultItems;

/**
 * A stationary tank which shoots stunning electricity that arcs between targets
 */
public class TankBlue extends TankAIControlled
{
	public TankBlue(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 0, 0, 200, angle, ShootAI.straight);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;

		this.setBullet(DefaultItems.zap);
		this.getBullet().recoil = 0;

		this.turretAimSpeed = 0.02;
		this.enableLookingAtTargetEnemy = false;
		this.cooldownBase = 200;
		this.cooldownRandom = 0;

		if (Game.tankTextures)
		{
			this.colorModel = TankModels.fixed.color;
			this.emblem = "emblems/electric.png";
			this.emblemG = 160;
			this.emblemB = 255;
		}

		this.coinValue = 4;

		this.description = "A stationary tank which shoots stunning electricity that arcs between targets";
	}
}
