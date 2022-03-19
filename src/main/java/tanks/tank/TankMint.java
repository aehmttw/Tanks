package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

public class TankMint extends TankAIControlled
{
	public TankMint(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 60, 180, 140, angle, ShootAI.straight);

		this.enableMovement = true;
		this.maxSpeed = 0.75;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.enableDefensiveFiring = true;
		this.liveBulletMax = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 240;
		this.aimTurretSpeed = 0.02;
		this.bulletBounces = 0;
		this.bulletEffect = Bullet.BulletEffect.fire;
		this.bulletSpeed = 25.0 / 4;
		this.enableLookingAtTargetEnemy = false;
		this.motionChangeChance = 0.001;
		this.avoidSensitivity = 1;
		
		this.coinValue = 2;

		this.description = "A tank which shoots fast rocket bullets";
	}
}
