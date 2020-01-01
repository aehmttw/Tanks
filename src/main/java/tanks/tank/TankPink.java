package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.event.EventCreateCustomTank;
import tanks.event.EventCreateTank;
import tanks.registry.RegistryTank;

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

		this.description = "A tank which spawns mini tanks and---shoots rockets that bounce twice";
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
		double pos = Math.random() * 100 - 50;
		int side = (int) (Math.random() * 4);

		double x = pos;
		double y = pos;

		if (side == 0)
			x = -50;
		else if (side == 1)
			x = 50;
		else if (side == 2)
			y = -50;
		else if (side == 3)
			y = 50;

		RegistryTank.TankEntry e = Game.registryTank.getEntry(this.name);

		while (e.name.equals(this.name))
		{
			e = Game.registryTank.getRandomTank();
		}

		TankMini t = new TankMini("mini", this.posX + x, this.posY + y, this.angle, this);
		Game.eventsOut.add(new EventCreateCustomTank(t));
		Game.movables.add(t);
	}
}
