package tanks.tank;

import tanks.Game;

public class TankDummyLoadingScreen extends Tank
{
	public static final double size_multiplier = 1.5;
	
	public TankDummyLoadingScreen(double x, double y) 
	{
		super("loadingscreendummy", x, y, Game.tank_size * size_multiplier, 0, 150, 255);
		this.turret.length *= size_multiplier;
		this.turret.size *= size_multiplier;
	}

}
