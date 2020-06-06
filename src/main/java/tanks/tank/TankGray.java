package tanks.tank;

import tanks.Game;

public class TankGray extends TankAIControlled
{
	public TankGray(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 100, 100, 100, angle, ShootAI.wander);

		this.enableMovement = true;
		this.speed = 1.5;
		this.enableMineLaying = false;
		this.liveBulletMax = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.idleTurretSpeed = 0.01;
		this.bulletBounces = 1;
		this.turretIdleTimerBase = 500;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;
		this.motionChangeChance = 0.001;
		this.aimAccuracyOffset = 0;

		this.coinValue = 1;

		this.description = "A primitive mobile tank";
	}
}
