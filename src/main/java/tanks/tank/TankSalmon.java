package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.BulletHoming;

/**
 * A smart, very fast tank which fires rockets
 */
public class TankSalmon extends TankAIControlled
{
	public TankSalmon(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 255, 180, 140, angle, ShootAI.homing);
		this.cooldownBase = 150;
		this.cooldownRandom = 50;
		this.maxSpeed = 1.25;
		this.bullet.speed = 25.0 / 4;
		this.bullet.bounces = 0;
		this.bullet.effect = Bullet.BulletEffect.fire;
		this.bullet.bulletClass = BulletHoming.class;
		this.bullet.name = "Homing bullet";
		this.turretAimSpeed = 0.06;
		this.enablePathfinding = true;
		this.enablePredictiveFiring = false;
		this.targetEnemySightBehavior = TargetEnemySightBehavior.backwind;

		this.coinValue = 10;

		if (Game.tankTextures)
		{
			this.emblem = "emblems/curve.png";
			this.emblemR = this.secondaryColorR;
			this.emblemG = this.secondaryColorG;
			this.emblemB = this.secondaryColorB;
			this.baseModel = TankModels.diagonalStripes.base;
		}

		this.description = "A tank which shoots homing rockets";
	}
}
