package tanks.tank;

import tanks.Game;

public class TankDummy extends Tank
{
	public TankDummy(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 75, 40, 0);
		this.angle = angle;
		
		this.coinValue = 0;

		this.description = "A dummy tank used to practice your aim";
	}

    @Override
	public void update()
	{
		this.vX *= 0.8;
		this.vY *= 0.8;

		super.update();
	}
}
