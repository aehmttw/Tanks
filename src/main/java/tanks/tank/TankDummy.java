package tanks.tank;

import tanks.Game;

/**
 * A dummy tank used to practice your aim
 * @see TankDummyLoadingScreen
 */
public class TankDummy extends TankAIControlled
{
	public TankDummy(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 75, 40, 0, angle, ShootAI.none);
		this.enableMovement = false;
		this.enableMineLaying = false;
		this.bullet.maxLiveBullets = 0;
		this.turretIdleSpeed = 0;
		this.enableLookingAtTargetEnemy = false;

		if (Game.tankTextures)
		{
			this.colorModel = TankModels.fixed.color;
			this.emblem = "emblems/x.png";
			this.emblemR = 50;
			this.emblemG = 25;
			this.coinValue = 0;
		}

		this.description = "A dummy tank used to practice your aim";
	}
}
