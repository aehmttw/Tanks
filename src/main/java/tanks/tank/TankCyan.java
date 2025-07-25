package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultItems;

/**
 * A support tank which shoots freezing bullets that deal low damage
 */
public class TankCyan extends TankAIControlled
{
	public TankCyan(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 128, 255, 255, angle, ShootAI.straight);

		this.enableMovement = true;
		this.maxSpeed = 0.75;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.cooldownRandom = 60;
		this.cooldownBase = 240;
		this.turretAimSpeed = 0.01;

		this.setBullet(DefaultItems.freezing_bullet);
		this.getBullet().maxLiveBullets = 1;

		this.enableLookingAtTargetEnemy = false;
		this.turnChance = 0.001;
		this.resistFreeze = true;

		this.coinValue = 4;

		if (Game.tankTextures)
		{
			this.emblem = "emblems/snowflake.png";
			this.emblemColor.set(160, 255, 255);
		}

		this.description = "A support tank which shoots freezing bullets that deal low damage";
	}
}
