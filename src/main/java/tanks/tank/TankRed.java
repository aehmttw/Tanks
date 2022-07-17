package tanks.tank;

import tanks.Game;
import tanks.bullet.BulletLaser;

/**
 * A stationary tank which shoots {@link BulletLaser}s.
 */
public class TankRed extends TankAIControlled
{
	public TankRed(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 200, 0, 0, angle, ShootAI.straight);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.bullet.maxLiveBullets = 1;
		this.turretAimSpeed = 0.02;
		this.enableLookingAtTargetEnemy = false;
		this.bullet.bulletClass = BulletLaser.class;
		this.cooldownBase = 100;
		this.aimAccuracyOffset = 0;
		this.bullet.name = "Laser";
		this.cooldownSpeedup = 0.25;
		this.bullet.bounces = 0;
		this.chargeUp = true;
		this.coinValue = 6;

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