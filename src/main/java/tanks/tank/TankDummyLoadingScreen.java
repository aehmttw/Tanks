package tanks.tank;

import tanks.Game;

/**
 * A spinning tank shown on the loading screen
 */
public class TankDummyLoadingScreen extends Tank
{
	public static final double size_multiplier = 1.5;
	
	public TankDummyLoadingScreen(double x, double y) 
	{
		super("loadingscreendummy", x, y, Game.tile_size * size_multiplier, 0, 150, 255);
		this.colorR = Game.player.colorR;
		this.colorG = Game.player.colorG;
		this.colorB = Game.player.colorB;
		this.secondaryColorR = Game.player.turretColorR;
		this.secondaryColorG = Game.player.turretColorG;
		this.secondaryColorB = Game.player.turretColorB;

		this.description = "A spinning tank shown on the loading screen";
	}

}
