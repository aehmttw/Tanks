package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.BulletFreeze2;
import tanks.bullet.legacy.BulletFreeze;

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
		this.bullet.maxLiveBullets = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 240;
		this.turretAimSpeed = 0.01;
		this.bullet.bounces = 0;
		this.bullet.effect = Bullet.BulletEffect.ice;
		this.bullet.speed = 25.0 / 8;
		this.bullet.damage = 0.25;
		this.bullet.bulletClass = BulletFreeze2.class;
		this.bullet.name = "Freezing bullet";
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
