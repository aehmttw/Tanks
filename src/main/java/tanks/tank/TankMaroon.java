package tanks.tank;

import tanks.Bullet;
import tanks.Game;

public class TankMaroon extends TankAIControlled
{
	public TankMaroon(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, 100, 0, 25, angle, ShootAI.reflect);

		this.enableMovement = true;
		this.speed = 1;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.cooldownRandom = 120;
		this.cooldownBase = 480;
		this.aimTurretSpeed = 0.02;
		this.bulletBounces = 3;
		this.bulletEffect = Bullet.BulletEffect.none;
		this.bulletSpeed = 25.0 / 4;
		this.bulletSize = 25;
		this.bulletHeavy = true;
		this.enableLookingAtTargetEnemy = false;
		this.motionChangeChance = 0.001;
		
		this.coinValue = 7;
	}
}
