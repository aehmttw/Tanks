package tanks.tank;

import java.awt.Color;

import tanks.Game;

public class TankYellow extends EnemyTank
{
	public TankYellow(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, new Color(235, 200, 0), angle, ShootAI.reflect);

		this.liveBulletMax = 1;
		this.mineTimerBase = 200;
		this.mineTimerRandom = 600;
		this.mineTimer = this.mineTimerBase + this.mineTimerRandom * Math.random();
		
		this.coinValue = 2;
	}
}
