package tanks;

import java.awt.Color;

public class EnemyTankPurple extends EnemyTankDynamic
{
	public EnemyTankPurple(double x, double y, double angle)
	{
		super(x, y, Game.tank_size, new Color(150, 0, 200), angle, ShootAI.alternate);
		this.enableDefensiveFiring = true;
		this.cooldownBase = 20;
		this.cooldownRandom = 40;
	}
}
