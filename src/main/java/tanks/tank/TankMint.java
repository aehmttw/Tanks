package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultItems;

/**
 * A tank which shoots fast rocket bullets
 */
public class TankMint extends TankAIControlled
{
	public TankMint(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 60, 180, 140, angle, ShootAI.straight);

		this.enableMovement = true;
		this.maxSpeed = 0.75;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.enableDefensiveFiring = true;
		this.cooldownRandom = 60;
		this.cooldownBase = 240;
		this.turretAimSpeed = 0.02;

		this.setBullet(DefaultItems.rocket);
		this.getBullet().maxLiveBullets = 1;

		this.enableLookingAtTargetEnemy = false;
		this.turnChance = 0.001;
		this.mineAvoidSensitivity = 1;

		this.avoidanceSeekOpenSpaces = true;
		this.bulletAvoidBehvavior = BulletAvoidBehavior.dodge;

		if (Game.tankTextures)
		{
			this.baseModel = TankModels.diagonalStripes.base;
		}

		this.coinValue = 2;

		this.description = "A tank which shoots fast rocket bullets";
	}
}
