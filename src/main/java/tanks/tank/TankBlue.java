package tanks.tank;

import tanks.Game;
import tanks.bullet.BulletElectric;

public class TankBlue extends TankAIControlled
{
	public TankBlue(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 0, 0, 200, angle, ShootAI.straight);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.bulletBounces = 3;
		this.aimTurretSpeed = 0.02;
		this.enableLookingAtTargetEnemy = false;
		this.cooldown = 100;
		this.cooldownBase = 200;
		this.cooldownRandom = 0;
		this.bulletSpeed = 25.0 / 8;
		this.bulletSound = "laser.ogg";
		this.bulletDamage = 0.2;
		this.setFrameDamageMultiplier = false;
		this.bulletClass = BulletElectric.class;

		this.coinValue = 4;

		this.description = "A stationary tank---which shoots---stunning electricity---that arcs between---targets";
	}

	@Override
	public void update()
	{
		super.update();
	}

	@Override
	public void shoot()
	{
		if (this.cooldown > 0 || this.disabled || this.destroy)
			return;

		super.launchBullet(0);
	}
}
