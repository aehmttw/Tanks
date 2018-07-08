package tanks;

import java.awt.Color;

public class EnemyTankMint extends EnemyTank
{
	public EnemyTankMint(double x, double y, double angle)
	{
		super(x, y, Game.tank_size, new Color(0, 130, 130), angle, ShootAI.straight);

		this.enableMovement = true;
		this.speed = 1;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 240;
		this.idleTurretSpeed = 0.02;
		this.bulletBounces = 0;
		this.bulletColor = Color.red;
		this.bulletEffect = Bullet.BulletEffect.fire;
		this.bulletSpeed = 25.0 / 2;
		this.enableLookingAtPlayer = false;
		this.motionChangeChance = 0.0005;
		
		this.coinValue = 2;
	}
}
