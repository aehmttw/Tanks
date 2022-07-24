package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

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
		this.bullet.maxLiveBullets = 4;
		this.cooldownRandom = 200;
		this.cooldownBase = 100;
		this.aimAccuracyOffset = 0;
		this.bullet.bounces = 3;
		this.bullet.effect = Bullet.BulletEffect.trail;
		this.bullet.speed = 25.0 / 8;
		this.bullet.size = 25;
		this.bullet.heavy = true;
		this.bullet.name = "Mega bullet";

		if (Game.tankTextures)
		{
			this.colorModel = TankModels.fixed.color;
			this.emblem = "emblems/star.png";
			this.emblemR = this.secondaryColorR;
			this.emblemG = this.secondaryColorG;
			this.emblemB = this.secondaryColorB;
		}

		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankBrown("brown", 0, 0, 0), 1));
		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankGray("gray", 0, 0, 0), 1));
		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankMint("mint", 0, 0, 0), 0.5));
		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankYellow("yellow", 0, 0, 0), 0.5));
		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankMagenta("magenta", 0, 0, 0), 0.3333));

		this.health = 5;
		this.baseHealth = 5;
		this.coinValue = 25;

		this.description = "A big boss tank which spawns other tanks and takes 5 regular bullets to destroy";
	}
}
