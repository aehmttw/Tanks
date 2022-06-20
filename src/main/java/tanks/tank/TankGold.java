package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletBoost;

/**
 * A tank which speeds up its allies and becomes explosive as a last stand
 */
public class TankGold extends TankAIControlled
{
	public TankGold(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 255, 180, 0, angle, ShootAI.straight);

		this.enableMineLaying = false;
		this.enablePredictiveFiring = true;
		this.enableDefensiveFiring = true;
		this.cooldownBase = 40;
		this.cooldownRandom = 0;
		this.bullet.maxLiveBullets = 5;
		this.aimTurretSpeed = 0.04;
		this.bullet.bounces = 0;
		this.bullet.effect = Bullet.BulletEffect.ember;
		this.bullet.damage = 0;
		this.enablePathfinding = true;
		this.seekChance = 0.01;
		this.bullet.speed = 25 / 4.0;
		this.bullet.bulletClass = BulletBoost.class;
		this.bullet.name = "Booster bullet";
		this.dealsDamage = false;
		this.commitsSuicide = true;
		this.targetEnemySightBehavior = TargetEnemySightBehavior.keep_distance;
		this.mine.radius *= 1.5;

		this.coinValue = 4;

		this.description = "A tank which speeds up---its allies and becomes---explosive as a last stand";
	}
}