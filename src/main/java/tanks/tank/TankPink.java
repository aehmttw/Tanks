package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultBullets;

/**
 * A tank which spawns mini tanks and shoots 2-bounce rockets
 */
public class TankPink extends TankAIControlled
{
	public TankPink(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 255, 127, 127, angle, ShootAI.reflect);
		this.enableMovement = false;
		this.enableMineLaying = false;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.turretAimSpeed = 0.02;

		this.setBullet(DefaultBullets.sniper_rocket);
		this.bullet.maxLiveBullets = 2;

		this.turretIdleTimerBase = 25;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;

		if (Game.tankTextures)
		{
			this.colorModel = TankModels.fixed.color;
			this.baseModel = TankModels.diagonalStripes.base;
			this.emblem = "emblems/squares.png";
			this.emblemR = this.secondaryColorR;
			this.emblemG = this.secondaryColorG;
			this.emblemB = this.secondaryColorB;
		}

		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankReference("mini"), 1));

		this.coinValue = 12;

		this.description = "A tank which spawns mini tanks and shoots 2-bounce rockets";
	}
}
