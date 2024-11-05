package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.DefaultBullets;

/**
 * A big boss tank which spawns other tanks and takes 5 regular bullets to destroy
 */
public class TankBoss extends TankAIControlled
{
	public TankBoss(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size * 3, 255, 0, 0, angle, ShootAI.alternate);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.cooldownRandom = 200;
		this.cooldownBase = 100;
		this.aimAccuracyOffset = 0;

		this.setBullet(DefaultBullets.mega_bullet);
		this.bullet.maxLiveBullets = 4;

		if (Game.tankTextures)
		{
			this.colorModel = TankModels.fixed.color;
			this.emblem = "emblems/star.png";
			this.emblemR = this.secondaryColorR;
			this.emblemG = this.secondaryColorG;
			this.emblemB = this.secondaryColorB;
		}

		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankReference("brown"), 1));
		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankReference("gray"), 1));
		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankReference("mint"), 0.5));
		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankReference("yellow"), 0.5));
		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankReference("magenta"), 0.3333));

		this.health = 5;
		this.baseHealth = 5;
		this.coinValue = 25;

		this.description = "A big boss tank which spawns other tanks and takes 5 regular bullets to destroy";
	}
}
