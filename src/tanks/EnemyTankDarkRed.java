package tanks;

import java.awt.Color;

public class EnemyTankDarkRed extends EnemyTankDynamic
{
	public EnemyTankDarkRed(double x, double y, double angle)
	{
		super(x, y, Game.tank_size, new Color(100, 0, 0), angle, ShootAI.straight);
		this.cooldownBase = 5;
		this.cooldownRandom = 0;
		this.speed = 2.5;
		this.bulletSpeed = 25.0 / 2;
		this.aimAccuracyOffset = 0.1;
		this.liveBulletMax = 8;
		this.bulletBounces = 0;
		this.bulletDamage /= 4;
		this.bulletSpeed = 25.0 / 2;
		this.bulletColor = Color.black;
		this.bulletSize /= 2;
		this.bulletEffect = Bullet.BulletEffect.none;
	}
	
	@Override
	public void reactToPlayerSight()
	{
		this.setMotionAwayFromDirection(Game.player.posX, Game.player.posY, speed);
	}
}
