package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.DefaultBullets;

/**
 * A fast tank which rapidly fires many small, low-damage bullets
 */
public class TankDarkGreen extends TankAIControlled
{
	public TankDarkGreen(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 85, 107, 47, angle, ShootAI.straight);
		this.cooldownBase = 5;
		this.cooldownRandom = 0;
		this.maxSpeed = 1.5;
		this.aimAccuracyOffset = 0.1;
		this.targetEnemySightBehavior = TargetEnemySightBehavior.flee;
		this.bulletAvoidBehvavior = BulletAvoidBehavior.dodge;

		this.setBullet(DefaultBullets.mini_bullet);

		if (Game.tankTextures)
		{
			this.baseModel = TankModels.camo.base;
			this.colorModel = TankModels.camo.color;
			this.turretBaseModel = TankModels.camo.turretBase;
			this.turretModel = TankModels.camo.turret;
		}

		this.coinValue = 10;

		this.description = "A fast tank which rapidly fires many small, low-damage bullets";
	}
}
