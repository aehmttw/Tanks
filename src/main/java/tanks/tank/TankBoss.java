package tanks.tank;

import tanks.Game;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.event.EventCreateTank;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;
import tanks.registry.RegistryTank;

import java.util.ArrayList;

public class TankBoss extends TankAIControlled
{
	public ArrayList<Tank> spawned = new ArrayList<Tank>();
	
	public TankBoss(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size * 3, 255, 0, 0, angle, ShootAI.alternate);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.liveBulletMax = 4;
		this.cooldownRandom = 200;
		this.cooldownBase = 100;
		this.aimAccuracyOffset = 0;
		this.bulletBounces = 3;
		this.bulletEffect = Bullet.BulletEffect.trail;
		this.bulletSpeed = 25.0 / 8;
		this.bulletSize = 25;
		this.bulletHeavy = true;
		this.health = 5;
		this.baseHealth = 5;
		this.turret.length *= 3;
		this.coinValue = 25;

		this.description = "A big boss tank which spawns---other tanks and takes 5 regular---bullets to destroy";
	}
	
	@Override
	public void update()
	{
		if (this.age <= 0 && !this.destroy && !ScreenGame.finishedQuick)
		{
			for (int i = 0; i < 4; i++)
			{
				this.spawnTank();
			}	
		}
		
		super.update();
		
		ArrayList<Tank> removeSpawned = new ArrayList<Tank>();
		
		for (int i = 0; i < this.spawned.size(); i++)
		{
			if (!Game.movables.contains(this.spawned.get(i)))
				removeSpawned.add(this.spawned.get(i));
		}
		
		for (int i = 0; i < removeSpawned.size(); i++)
		{
			this.spawned.remove(removeSpawned.get(i));
		}
		
		if (this.random.nextDouble() < 0.003 * Panel.frameFrequency && this.spawned.size() < 6 && !this.destroy && !ScreenGame.finishedQuick)
		{
			this.spawnTank();
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

			double pos = this.random.nextDouble() * 200 - 100;
			int side = (int) (this.random.nextDouble() * 4);

			x = pos;
			y = pos;

			if (side == 0)
				x = -100;
			else if (side == 1)
				x = 100;
			else if (side == 2)
				y = -100;
			else if (side == 3)
				y = 100;

			boolean retry = false;
			if (this.posX + x > Game.tile_size / 2 && this.posX + x < (Game.currentSizeX - 0.5) * Game.tile_size &&
					this.posY + y > Game.tile_size / 2 && this.posY + y < (Game.currentSizeY - 0.5) * Game.tile_size)
			{
				for (Obstacle o: Game.obstacles)
				{
					if (o.tankCollision && Math.abs(o.posX - (this.posX + x)) < Game.tile_size && Math.abs(o.posY - (this.posY + y)) < Game.tile_size)
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

		while (e.name.equals(this.name) || e.isBoss)
		{
			e = Game.registryTank.getRandomTank(this.random);
		}

		Tank t = e.getTank(this.posX + x, this.posY + y, this.angle);
		t.team = this.team;

		Game.eventsOut.add(new EventCreateTank(t));
		this.spawned.add(t);
		
		Game.movables.add(t);
	}
}
