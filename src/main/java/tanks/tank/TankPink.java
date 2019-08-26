package tanks.tank;

import tanks.Game;
import tanks.bullets.Bullet;
import tanks.event.EventCreateCustomTank;

public class TankPink extends TankAIControlled
{
	public int spawnedMinis = 0;
	
	public TankPink(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, 255, 127, 127, angle, ShootAI.reflect);
		this.enableMovement = false;
		this.enableMineLaying = false;
		this.liveBulletMax = 2;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.aimTurretSpeed = 0.02;
		this.bulletBounces = 2;
		this.bulletSpeed = 25.0 / 2;
		this.bulletEffect = Bullet.BulletEffect.fireTrail;
		this.turretIdleTimerBase = 25;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;
		
		this.coinValue = 15;
	}
	
	@Override
	public void update()
	{
		if (this.age <= 0)
		{
			for (int i = 0; i < 4; i++)
			{
				spawnTank();
			}	
		}
		
		super.update();
		
		if (Math.random() < 0.003 && this.spawnedMinis < 6)
		{
			spawnTank();
		}
	}
	
	public void spawnTank()
	{
		TankMini t = new TankMini("mini", this.posX, this.posY, this.angle, this);
		Game.events.add(new EventCreateCustomTank(t));
		Game.movables.add(t);
	}
}
