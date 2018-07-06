package tanks;

import java.awt.Color;

public class EnemyTankBlack extends EnemyTankDynamic
{
	public double strafeDirection = Math.PI / 2;

	public EnemyTankBlack(double x, double y, double angle)
	{
		super(x, y, Game.tank_size, new Color(0, 0, 0), angle, ShootAI.straight);
		this.aimRayBounces = 0;
		this.cooldownBase = 75;
		this.cooldownRandom = 0;
		this.speed = 3.5;
		this.enableDefensiveFiring = true;
		this.bulletSpeed = 25.0 / 2;
		this.aimTurretSpeed = 0.06;
	}
	
	@Override
	public void launchBullet(double offset)
	{
		Bullet b = new Bullet(this.posX, this.posY, Color.red, 0, this);
		b.setPolarMotion(angle + offset, 25.0/2);
		b.moveOut(4);
		b.effect = Bullet.BulletEffect.fire;
		Game.movables.add(b);
		this.cooldown = (int) (Math.random() * this.cooldownRandom + this.cooldownBase);

	}
	
	@Override
	public void reactToPlayerSight()
	{
		if (Math.random() < 0.01)
			strafeDirection = -strafeDirection;

		this.setMotionInDirectionWithOffset(Game.player.posX, Game.player.posY, 3.5, strafeDirection);
	}
}
