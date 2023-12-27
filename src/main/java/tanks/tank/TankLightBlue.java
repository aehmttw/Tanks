package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.BulletAir2;
import tanks.bullet.legacy.BulletAir;

/**
 * A tank which blows strong air currents
 */
public class TankLightBlue extends TankAIControlled
{
	public TankLightBlue(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 200, 220, 255, angle, ShootAI.straight);

		this.enableMovement = true;
		this.maxSpeed = 1.0;

		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.turretAimSpeed = 0.01;

		this.turnChance = 0.001;

		this.mineAvoidSensitivity = 0;
		this.enableBulletAvoidance = false;

		this.bullet.maxLiveBullets = 0;
		this.bullet.bulletClass = BulletAir.class;
		this.bullet.cooldownBase = 0;
		this.bullet.speed = 6.25;
		this.bullet.accuracy = 20;
		this.targetEnemySightBehavior = TargetEnemySightBehavior.strafe;
		this.cooldownBase = 0;
		this.cooldownRandom = 0;
		this.bullet.bounces = 0;
		this.bullet.effect = Bullet.BulletEffect.none;
		this.bullet.name = "Air";
		this.bullet.damage = 0;
		this.bullet.heavy = true;

		this.coinValue = 8;

		if (Game.tankTextures)
		{
			this.emblem = "emblems/pinwheel.png";
			this.emblemR = this.secondaryColorR;
			this.emblemG = this.secondaryColorG;
			this.emblemB = this.secondaryColorB;
		}

		this.description = "A tank which blows strong gusts of air";
	}
}