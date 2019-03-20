package tanks;

public class ObstacleShrubbery extends Obstacle
{
	
	public double opacity = 255;
	
	public ObstacleShrubbery(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		this.destructible = true;
		this.drawBelow = false;
		this.tankCollision = false;
		this.bulletCollision = false;
		this.checkForObjects = true;
		this.colorR = (Math.random() * 20);
		this.colorG = (Math.random() * 50) + 150;
		this.colorB = (Math.random() * 20);
		if (!Game.fancyGraphics)
		{
			this.colorR = 10;
			this.colorG = 175;
			this.colorB = 10;
		}
	}
	
	@Override
	public void draw()
	{	
		this.opacity = Math.min(this.opacity + Panel.frameFrequency, 255);
		if (Game.screen instanceof ScreenLevelBuilder || Game.screen instanceof ScreenGame && (!((ScreenGame) Game.screen).playing))
		{
			this.opacity = 127;
		}
		
		if (Game.player.destroy)
			this.opacity = Math.max(127, this.opacity - Panel.frameFrequency * 2);
		
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.opacity);
		Drawing.drawing.fillRect(this.posX, this.posY, draw_size, draw_size);
	}
	
	@Override
	public void drawForInterface(double x, double y)
	{
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 127);
		Drawing.drawing.fillInterfaceRect(x, y, draw_size, draw_size);
	}
	
	@Override
	public void onObjectEntry(Movable m)
	{
		this.opacity = Math.max(this.opacity - Panel.frameFrequency * Math.pow(Math.abs(m.vX) + Math.abs(m.vY), 2), 127);
		m.hiddenTimer = Math.min(100, m.hiddenTimer + (this.opacity - 127) / 255);
		m.canHide = true;
		
		if (m instanceof BulletFlame)
			Game.removeObstacles.add(this);
	}

}
