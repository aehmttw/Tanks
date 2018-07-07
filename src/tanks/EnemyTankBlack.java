package tanks;

import java.awt.Color;

public class EnemyTankBlack extends EnemyTankDynamic
{
	public double strafeDirection = Math.PI / 2;

	public EnemyTankBlack(double x, double y, double angle)
	{
		super(x, y, Game.tank_size, new Color(0, 0, 0), angle, ShootAI.straight);
		this.cooldownBase = 75;
		this.cooldownRandom = 0;
		this.speed = 3.5;
		this.enableDefensiveFiring = true;
		this.bulletSpeed = 25.0 / 2;
		this.bulletBounces = 0;
		this.bulletColor = Color.red;
		this.bulletEffect = Bullet.BulletEffect.fire;
		this.aimTurretSpeed = 0.06;
		
		this.coinValue = 10;
	}
	
	@Override
	public void reactToPlayerSight()
	{
		if (Math.random() < 0.01)
			strafeDirection = -strafeDirection;

		this.setMotionInDirectionWithOffset(Game.player.posX, Game.player.posY, 3.5, strafeDirection);
	}
}
