package tanks.tank;

import java.awt.Color;

import tanks.Game;

public class TankPurple extends EnemyTank
{
	public TankPurple(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, new Color(150, 0, 200), angle, ShootAI.alternate);
		this.enableDefensiveFiring = true;
		this.cooldownBase = 20;
		this.cooldownRandom = 40;
		
		this.coinValue = 4;
	}
}
