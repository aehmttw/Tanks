package tanks.tank;

import tanks.Game;
import tanks.Movable;
import tanks.bullet.BulletFlame;

public class TankOrange extends TankAIControlled
{
	public TankOrange(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 230, 120, 0, angle, ShootAI.straight);

		this.enableMovement = true;
		this.maxSpeed = 1.0;

		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.aimTurretSpeed = 0.01;
		this.enablePathfinding = true;

		this.motionChangeChance = 0.001;
		this.avoidSensitivity = 1;

		this.bulletClass = BulletFlame.class;
		this.bulletSound = "flame.ogg";
		this.bulletSoundVolume = 0.7f;
		this.cooldown = 200;
		this.cooldownBase = 1;
		this.cooldownRandom = 0;
		this.bulletBounces = 0;

		this.coinValue = 4;

		this.description = "A short-range tank which shoots fire";
	}

	@Override
	public void shoot()
	{
		if (this.targetEnemy != null && Movable.distanceBetween(this, this.targetEnemy) < 400 && this.cooldown <= 0 && !this.disabled && !this.destroy)
		{
			Ray a = new Ray(this.posX, this.posY, this.angle, 0, this);

			if (a.getTarget() != null)
				super.shoot();
		}
	}
}