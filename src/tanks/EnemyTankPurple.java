package tanks;

import java.awt.Color;

public class EnemyTankPurple extends EnemyTank
{
	public EnemyTankPurple(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, new Color(150, 0, 200), angle, ShootAI.alternate);
		this.enableDefensiveFiring = true;
		this.cooldownBase = 20;
		this.cooldownRandom = 40;
		
		this.coinValue = 4;
	}
}
