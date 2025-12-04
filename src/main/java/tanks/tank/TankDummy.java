package tanks.tank;

import tanks.Game;

/**
 * A dummy tank used to practice your aim
 */
public class TankDummy extends TankAIControlled
{
	public TankDummy(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 75, 40, 0, angle, ShootAI.none);
		this.enableMovement = false;
		this.enableMineLaying = false;
		this.getBullet().maxLiveBullets = 0;
		this.turretIdleSpeed = 0;
		this.enableLookingAtTargetEnemy = false;
        this.friction += 0.15;

		if (Game.tankTextures)
		{
			this.colorSkin = TankModels.fixed;
			this.emblem = "emblems/x.png";
			this.emblemColor.red = 50;
			this.emblemColor.green = 25;
			this.coinValue = 0;
		}

		this.description = "A dummy tank used to practice your aim";
	}
}
