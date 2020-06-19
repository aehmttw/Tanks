package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

public class TankDarkGreen extends TankAIControlled
{
	public TankDarkGreen(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 85, 107, 47, angle, ShootAI.straight);
		this.cooldownBase = 5;
		this.cooldownRandom = 0;
		this.speed = 3;
		this.bulletSpeed = 25.0 / 2;
		this.aimAccuracyOffset = 0.1;
		this.liveBulletMax = 8;
		this.bulletBounces = 0;
		this.bulletDamage /= 8;
		this.bulletSize /= 2;
		this.bulletEffect = Bullet.BulletEffect.trail;
		
		this.coinValue = 9;

		this.description = "A fast tank which rapidly fires---many small, low-damage bullets";
	}
	
	@Override
	public void reactToTargetEnemySight()
	{
		this.setMotionAwayFromDirection(Game.playerTank.posX, Game.playerTank.posY, speed);
	}
}
