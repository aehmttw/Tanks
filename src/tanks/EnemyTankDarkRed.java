package tanks;

import java.awt.Color;

public class EnemyTankDarkRed extends EnemyTankDynamic
{
	public EnemyTankDarkRed(double x, double y, double angle)
	{
		super(x, y, Game.tank_size, new Color(100, 0, 0), angle, ShootAI.straight);
		this.aimRayBounces = 0;
		this.cooldownBase = 5;
		this.cooldownRandom = 0;
		this.speed = 2.5;
		this.bulletSpeed = 25.0 / 2;
		this.aimAccuracyOffset = 0.1;
		this.liveBulletMax = 8;
	}
	
	@Override
	public void launchBullet(double offset)
	{
		Bullet b = new Bullet(this.posX, this.posY, Color.black, 0, this);
		b.setPolarMotion(angle + offset, 25.0/2);
		b.moveOut(4);
		Game.movables.add(b);
		this.cooldown = this.cooldownBase;
		b.size /= 2;
		b.damage = 0.25;
	}
	
	@Override
	public void reactToPlayerSight()
	{
		this.setMotionAwayFromDirection(Game.player.posX, Game.player.posY, speed);
	}
}
