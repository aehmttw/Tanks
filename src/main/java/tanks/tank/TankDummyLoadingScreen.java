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
		this.secondaryColorR = Game.player.colorR2;
		this.secondaryColorG = Game.player.colorG2;
		this.secondaryColorB = Game.player.colorB2;

		this.description = "A spinning tank shown on the loading screen";
	}

}
