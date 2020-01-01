package tanks.tank;

import tanks.Game;

public class TankYellow extends TankAIControlled
{
	public TankYellow(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, 235, 200, 0, angle, ShootAI.reflect);

		this.liveBulletMax = 1;
		this.mineTimerBase = 200;
		this.mineTimerRandom = 400;
		this.mineTimer = this.mineTimerBase + this.mineTimerRandom * Math.random();
		
		this.coinValue = 2;
		this.description = "A tank which lays many mines";
	}
}
