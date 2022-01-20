package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

/**
 * A fast tank which rapidly fires many small, low-damage bullets
 */
public class TankDarkGreen extends TankAIControlled
{
	public TankDarkGreen(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 85, 107, 47, angle, ShootAI.straight);
		this.cooldownBase = 5;
		this.cooldownRandom = 0;
		this.maxSpeed = 1.5;
		this.bulletSpeed = 25.0 / 4;
		this.aimAccuracyOffset = 0.1;
		this.liveBulletMax = 8;
		this.bulletBounces = 0;
		this.bulletDamage /= 8;
		this.bulletSize /= 2;
		this.bulletEffect = Bullet.BulletEffect.trail;
		
		this.coinValue = 10;

		this.description = "A fast tank which rapidly fires---many small, low-damage bullets";
	}
	
	@Override
	public void reactToTargetEnemySight()
	{
		this.setAccelerationAwayFromDirection(Game.playerTank.posX, Game.playerTank.posY, acceleration);
	}
}
