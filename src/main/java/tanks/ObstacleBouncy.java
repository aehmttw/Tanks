package tanks;

public class ObstacleBouncy extends Obstacle
{
	public ObstacleBouncy(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.bouncy = true;
		this.colorR = Math.random() * 127 + 128;
		this.colorG = 0;
		this.colorB = 255;
		
		if (!Game.fancyGraphics)
			this.colorR = 191;
	}
}
