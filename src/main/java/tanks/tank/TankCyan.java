package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.DefaultBullets;
import tanks.bullet.legacy2.BulletFreeze2;

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

		this.setBullet(DefaultBullets.freezing_bullet);
		this.bullet.maxLiveBullets = 1;

		this.enableLookingAtTargetEnemy = false;
		this.turnChance = 0.001;
		this.resistFreeze = true;

		this.coinValue = 4;

		if (Game.tankTextures)
		{
			this.emblem = "emblems/snowflake.png";
			this.emblemR = 160;
			this.emblemG = 255;
			this.emblemB = 255;
		}

		this.description = "A support tank which shoots freezing bullets that deal low damage";
	}
}
