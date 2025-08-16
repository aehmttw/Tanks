package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultItems;

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
		this.bulletAvoidBehavior = BulletAvoidBehavior.dodge;

		this.setBullet(DefaultItems.mini_bullet);

		if (Game.tankTextures)
		{
			this.baseSkin = TankModels.camo;
			this.colorSkin = TankModels.camo;
			this.turretBaseSkin = TankModels.camo;
			this.turretSkin = TankModels.camo;
		}

		this.coinValue = 10;

		this.description = "A fast tank which rapidly fires many small, low-damage bullets";
	}
}
