package tanks.tank;

import tanks.Game;

/**
 * A primitive mobile tank
 * @see TankBrown
 */
public class TankGray extends TankAIControlled
{
	public TankGray(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 100, 100, 100, angle, ShootAI.wander);

		this.enableMovement = true;
		this.maxSpeed = 0.75;
		this.enableMineLaying = false;
		this.bullet.maxLiveBullets = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.turretIdleSpeed = 0.01;
		this.turretIdleTimerBase = 500;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;
		this.turnChance = 0.001;
		this.aimAccuracyOffset = 0;
		this.mineAvoidSensitivity = 0.75;

		this.coinValue = 1;

		if (Game.tankTextures)
		{
			this.colorModel = TankModels.horizontalStripes.color;
		}

		this.description = "A primitive mobile tank";
	}
}
