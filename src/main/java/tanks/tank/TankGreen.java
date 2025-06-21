package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultItems;

/**
 * A deadly stationary tank which shoots rockets that bounce twice
 */
public class TankGreen extends TankAIControlled
{
	public TankGreen(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 100, 200, 0, angle, ShootAI.reflect);
		this.enableMovement = false;
		this.enableMineLaying = false;
		this.cooldownRandom = 20;
		this.cooldownBase = 80;
		this.turretAimSpeed = 0.03;
		this.turretIdleSpeed = 0.01;
		this.turretIdleTimerRandom = 250;

		this.setBullet(DefaultItems.sniper_rocket);
		this.getBullet().recoil = 0;

		this.turretIdleTimerBase = 25;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;
		this.enableDefensiveFiring = true;

		if (Game.tankTextures)
		{
			this.colorSkin = TankModels.fixed;
			this.baseSkin = TankModels.diagonalStripes;
		}

		this.coinValue = 10;

		this.description = "A deadly stationary tank which shoots rockets that bounce twice";
	}
}
