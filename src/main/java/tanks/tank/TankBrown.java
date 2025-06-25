package tanks.tank;

import tanks.Game;

/**
 * A primitive stationary tank
 */
public class TankBrown extends TankAIControlled
{

	public TankBrown(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 150, 80, 0, angle, ShootAI.wander);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.getBullet().maxLiveBullets = 1;
		this.getBullet().recoil = 0;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.turretIdleSpeed = 0.01;
		this.turretIdleTimerBase = 500;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;
		this.aimAccuracyOffset = 0;

		if (Game.tankTextures)
		{
			this.colorSkin = TankModels.fixed;
		}

		this.coinValue = 1;

		this.description = "A primitive stationary tank";
	}
}
