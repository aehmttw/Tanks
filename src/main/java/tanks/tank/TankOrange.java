package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultItems;

/**
 * A short-range tank which shoots fire
 */
public class TankOrange extends TankAIControlled
{
	public TankOrange(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 230, 120, 0, angle, ShootAI.straight);

		this.enableMovement = true;
		this.maxSpeed = 1.0;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.turretAimSpeed = 0.01;
		this.enablePathfinding = true;
		this.aimAccuracyOffset = 0;
		this.turnChance = 0.001;
		this.mineAvoidSensitivity = 1;
		this.bulletAvoidBehvavior = BulletAvoidBehavior.aggressive_dodge;
		this.avoidanceSeekOpenSpaces = true;
		this.cooldownBase = 0;
		this.cooldownRandom = 0;

		this.setBullet(DefaultItems.flamethrower);
		this.bulletItem.item.cooldownBase = 0;

		this.coinValue = 4;

		if (Game.tankTextures)
		{
			this.baseSkin = TankModels.flames;
			this.colorSkin = TankModels.flames;
			this.turretBaseSkin = TankModels.flames;
			this.turretSkin = TankModels.flames;
		}

		this.description = "A short-range tank which shoots fire";
	}
}