package tanks;

import java.awt.Color;

public class EnemyTankYellow extends EnemyTank
{
	public EnemyTankYellow(double x, double y, double angle)
	{
		super(x, y, Game.tank_size, new Color(235, 200, 0), angle, ShootAI.reflect);

		this.liveBulletMax = 1;
		this.mineTimerBase = 200;
		this.mineTimerRandom = 600;
		this.mineTimer = this.mineTimerBase + this.mineTimerRandom * Math.random();
		
		this.coinValue = 2;
	}
}
