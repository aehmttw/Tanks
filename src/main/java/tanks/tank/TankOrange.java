package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.legacy.BulletFlame;

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

		this.bullet.maxLiveBullets = 0;
		this.bullet.bulletClass = BulletFlame.class;
		this.bullet.cooldownBase = 0;
		this.cooldownBase = 0;
		this.cooldownRandom = 0;
		this.bullet.effect = Bullet.BulletEffect.none;
		this.bullet.bounces = 0;
		this.bullet.damage = 0.1;
		this.bullet.name = "Flamethrower";

		this.coinValue = 4;

		if (Game.tankTextures)
		{
			this.baseModel = TankModels.flames.base;
			this.colorModel = TankModels.flames.color;
			this.turretBaseModel = TankModels.flames.turretBase;
			this.turretModel = TankModels.flames.turret;
		}

		this.description = "A short-range tank which shoots fire";
	}
}