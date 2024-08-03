package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.DefaultBullets;

/**
 * A tank which shoots huge bullets which bounce 3 times and can't be stopped
 */
public class TankMaroon extends TankAIControlled
{
	public TankMaroon(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 100, 0, 25, angle, ShootAI.reflect);

		this.enableMovement = true;
		this.maxSpeed = 0.75;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.bullet.maxLiveBullets = 1;
		this.cooldownRandom = 120;
		this.cooldownBase = 480;
		this.turretAimSpeed = 0.02;

		this.setBullet(DefaultBullets.mega_bullet);
		this.bullet.maxLiveBullets = 1;

		this.enableLookingAtTargetEnemy = true;
		this.turnChance = 0.001;
		this.turretSize *= 1.5;
		this.enablePathfinding = true;
		this.stopSeekingOnSight = true;

		if (Game.tankTextures)
		{
			this.emblem = "emblems/circle.png";
			this.emblemR = this.colorR * 0.7;
			this.emblemG = this.colorG * 0.7;
			this.emblemB = this.colorB * 0.7;
		}

		this.coinValue = 4;

		this.description = "A tank which shoots huge bullets which bounce 3 times and can't be stopped";
	}

}
