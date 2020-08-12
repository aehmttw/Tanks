package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

public class TankMaroon extends TankAIControlled
{
	public TankMaroon(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 100, 0, 25, angle, ShootAI.reflect);

		this.enableMovement = true;
		this.maxSpeed = 0.75;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.cooldownRandom = 120;
		this.cooldownBase = 480;
		this.aimTurretSpeed = 0.02;
		this.bulletBounces = 3;
		this.bulletEffect = Bullet.BulletEffect.trail;
		this.bulletSize = 25;
		this.bulletHeavy = true;
		this.enableLookingAtTargetEnemy = true;
		this.motionChangeChance = 0.001;
		this.turret.size *= 1.5;
		this.enablePathfinding = true;
		
		this.coinValue = 7;

		this.description = "A tank which shoots huge bullets which---bounce 3 times and can't be stopped";
	}

	public void reactToTargetEnemySight()
	{
		this.currentlySeeking = false;
	}

}
