package tanks;

import java.awt.Color;

public class TankDarkGreen extends EnemyTank
{
	public TankDarkGreen(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, new Color(85, 107, 47), angle, ShootAI.straight);
		this.cooldownBase = 5;
		this.cooldownRandom = 0;
		this.speed = 2.5;
		this.bulletSpeed = 25.0 / 2;
		this.aimAccuracyOffset = 0.1;
		this.liveBulletMax = 8;
		this.bulletBounces = 0;
		this.bulletDamage /= 8;
		this.bulletSpeed = 25.0 / 2;
		this.bulletColor = Color.black;
		this.bulletSize /= 2;
		this.bulletEffect = Bullet.BulletEffect.none;
		
		this.coinValue = 9;
	}
	
	@Override
	public void reactToTargetEnemySight()
	{
		this.setMotionAwayFromDirection(Game.player.posX, Game.player.posY, speed);
	}
}
