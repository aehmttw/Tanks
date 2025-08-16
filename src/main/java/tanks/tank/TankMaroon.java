package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultItems;

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
		this.cooldownRandom = 120;
		this.cooldownBase = 480;
		this.turretAimSpeed = 0.02;

		this.setBullet(DefaultItems.mega_bullet);
		this.getBullet().maxLiveBullets = 1;

		this.enableLookingAtTargetEnemy = true;
		this.turnChance = 0.001;
		this.turretSize *= 1.5;
		this.enablePathfinding = true;
		this.stopSeekingOnSight = true;

		if (Game.tankTextures)
		{
			this.emblem = "emblems/circle.png";
			this.emblemColor.set(this.color.red * 0.7, this.color.green * 0.7, this.color.blue * 0.7);
		}

		this.coinValue = 4;

		this.description = "A tank which shoots huge bullets which bounce 3 times and can't be stopped";
	}

}
