package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultBullets;

/**
 * A stationary tank which shoots lasers.
 */
public class TankRed extends TankAIControlled
{
	public TankRed(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 200, 0, 0, angle, ShootAI.straight);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.turretAimSpeed = 0.02;
		this.enableLookingAtTargetEnemy = false;

		this.cooldownBase = 100;
		this.aimAccuracyOffset = 0;

		this.cooldownSpeedup = 0.25;
		this.chargeUp = true;
		this.coinValue = 6;

		this.setBullet(DefaultBullets.laser);

		if (Game.tankTextures)
		{
			this.colorModel = TankModels.fixed.color;
			this.emblem = "emblems/laser.png";
			this.emblemR = 110;
			this.turretModel = TankModels.cross.turret;
		}

		this.description = "A stationary tank which shoots deadly lasers";
	}
}