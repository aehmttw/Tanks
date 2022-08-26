package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;

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
		this.bullet.maxLiveBullets = 2;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.turretAimSpeed = 0.02;
		this.bullet.bounces = 2;
		this.bullet.speed = 25.0 / 4;
		this.bullet.effect = Bullet.BulletEffect.fireTrail;
		this.bullet.name = "Bouncy fire bullet";
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

		this.spawnedTankEntries.add(new SpawnedTankEntry(new TankMini("mini", 0, 0, 0), 1));

		this.coinValue = 12;

		this.description = "A tank which spawns mini tanks and shoots 2-bounce rockets";
	}
}
