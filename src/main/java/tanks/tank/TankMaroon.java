package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

/**
 * A tank which shoots huge bullets which bounce 3 times and can't be stopped
 */
public class TankMaroon extends TankAIControlled
{
	public TankMaroon(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 100, 0, 25, angle, ShootAI.reflect);

		this.enableMovement = true;
		this.maxSpeed = 0.75;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.bullet.maxLiveBullets = 1;
		this.cooldownRandom = 120;
		this.cooldownBase = 480;
		this.aimTurretSpeed = 0.02;
		this.bullet.bounces = 3;
		this.bullet.effect = Bullet.BulletEffect.trail;
		this.bullet.size = 25;
		this.bullet.heavy = true;
		this.bullet.name = "Mega bullet";
		this.enableLookingAtTargetEnemy = true;
		this.motionChangeChance = 0.001;
		this.turretSize *= 1.5;
		this.enablePathfinding = true;
		this.stopSeekingOnSight = true;
		
		this.coinValue = 4;

		this.description = "A tank which shoots huge bullets which---bounce 3 times and can't be stopped";
	}

}
