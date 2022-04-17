package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

/**
 * A small, primitive tank which shoots tiny, low damage bullets
 */
public class TankMini extends TankAIControlled
{
	public TankMini(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size / 2, 255, 127, 127, angle, ShootAI.straight);

		this.enableMovement = true;
		this.maxSpeed = 1.5;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.bullet.maxLiveBullets = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.aimTurretSpeed = 0.02;
		this.bullet.bounces = 0;
		this.bullet.effect = Bullet.BulletEffect.trail;
		this.bullet.damage = 0.25;
		this.bullet.size /= 2;
		this.bullet.name = "Mini bullet";
		this.enableLookingAtTargetEnemy = true;
		this.motionChangeChance = 0.001;
		this.enableBulletAvoidance = false;
		this.health = 0.25;
		this.baseHealth = 0.25;
		this.avoidSensitivity = 0.5;
		this.turret.length /= 2;
		this.stayNearParent = true;

		this.description = "A small, primitive tank which---shoots tiny, low damage bullets";
	}
}
