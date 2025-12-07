package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultItems;

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
        this.friction += 0.15;

		this.setBullet(DefaultItems.sniper_rocket);
		this.getBullet().maxLiveBullets = 2;
		this.getBullet().recoil = 0;

		this.turretIdleTimerBase = 25;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;

		if (Game.tankTextures)
		{
			this.colorSkin = TankModels.fixed;
			this.baseSkin = TankModels.diagonalStripes;
			this.emblem = "emblems/squares.png";
			this.emblemColor.set(this.secondaryColor);
		}

		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankReference("mini"), 1));

		this.coinValue = 12;

		this.description = "A tank which spawns mini tanks and shoots 2-bounce rockets";
	}
}
