package tanks.tank;

import tanks.Game;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.event.EventCreateCustomTank;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;
import tanks.registry.RegistryTank;

/**
 * A tank which spawns mini tanks and shoots 2-bounce rockets
 */
public class TankPink extends TankAIControlled
{
	public int spawnedMinis = 0;
	
	public TankPink(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 255, 127, 127, angle, ShootAI.reflect);
		this.enableMovement = false;
		this.enableMineLaying = false;
		this.liveBulletMax = 2;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.aimTurretSpeed = 0.02;
		this.bulletBounces = 2;
		this.bulletSpeed = 25.0 / 4;
		this.bulletEffect = Bullet.BulletEffect.fireTrail;
		this.turretIdleTimerBase = 25;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;
		
		this.coinValue = 12;

		this.description = "A tank which spawns---mini tanks and shoots---2-bounce rockets";
	}
	
	@Override
	public void update()
	{
		if (this.age <= 0 && !this.destroy && !ScreenGame.finishedQuick)
		{
			for (int i = 0; i < 4; i++)
			{
				spawnTank();
			}	
		}
		
		super.update();
		
		if (this.random.nextDouble() < 0.003 * Panel.frameFrequency && this.spawnedMinis < 6 && !this.destroy && !ScreenGame.finishedQuick)
		{
			spawnTank();
		}
	}
	
	public void spawnTank()
	{
		double x;
		double y;

		int attempts = 0;

		while (true)
		{
			attempts++;

			double pos = this.random.nextDouble() * 100 - 50;
			int side = (int) (this.random.nextDouble() * 4);

			x = pos;
			y = pos;

			if (side == 0)
				x = -50;
			else if (side == 1)
				x = 50;
			else if (side == 2)
				y = -50;
			else if (side == 3)
				y = 50;

			boolean retry = false;
			if (this.posX + x > Game.tile_size / 4 && this.posX + x < (Game.currentSizeX - 0.25) * Game.tile_size &&
					this.posY + y > Game.tile_size / 4 && this.posY + y < (Game.currentSizeY - 0.25) * Game.tile_size)
			{
				for (Obstacle o : Game.obstacles)
				{
					if (o.tankCollision && Math.abs(o.posX - (this.posX + x)) < Game.tile_size * 0.75 && Math.abs(o.posY - (this.posY + y)) < Game.tile_size * 0.75)
					{
						retry = true;
						break;
					}
				}
			}
			else
				retry = true;

			if (!retry || attempts >= 10)
				break;
		}

		RegistryTank.TankEntry e = Game.registryTank.getEntry(this.name);

		while (e.name.equals(this.name))
		{
			e = Game.registryTank.getRandomTank(this.random);
		}

		TankMini t = new TankMini("mini", this.posX + x, this.posY + y, this.angle, this);
		Game.eventsOut.add(new EventCreateCustomTank(t));
		Game.movables.add(t);
	}
}
