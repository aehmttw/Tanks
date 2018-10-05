package tanks.tank;

import java.awt.Color;

import tanks.Bullet;
import tanks.Game;

public class TankPink extends TankAIControlled
{
	public int spawnedMinis = 0;
	
	public TankPink(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, new Color(255, 127, 127), angle, ShootAI.reflect);
		this.enableMovement = false;
		this.enableMineLaying = false;
		this.liveBulletMax = 2;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.aimTurretSpeed = 0.02;
		this.bulletBounces = 2;
		this.bulletColor = Color.red;
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
				Game.movables.add(new TankMini("mini", this.posX, this.posY, this.angle, this));
			}	
		}
		
		super.update();
		
		if (Math.random() < 0.003 && this.spawnedMinis < 6)
		{
			Game.movables.add(new TankMini("mini", this.posX, this.posY, this.angle, this));
		}
	}
	
}
