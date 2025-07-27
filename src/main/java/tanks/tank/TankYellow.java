package tanks.tank;

import tanks.Game;

/**
 * A tank which lays many {@link Mine}s.
 */
public class TankYellow extends TankAIControlled
{
	public TankYellow(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 235, 200, 0, angle, ShootAI.reflect);

		this.getBullet().maxLiveBullets = 1;
		this.getMine().maxLiveMines = 4;
		this.mineTimerBase = 100;
		this.mineTimerRandom = 300;
		this.cooldownBase = 120;
		this.cooldownRandom = 100;

		this.mineTimer = this.mineTimerBase + this.mineTimerRandom * this.random.nextDouble();

		if (Game.tankTextures)
		{
			this.emblem = "emblems/circle_double.png";
			this.emblemColor.set(this.secondaryColor);
		}

		this.coinValue = 2;
		this.description = "A tank which lays many mines";
	}
}
