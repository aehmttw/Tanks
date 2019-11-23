package tanks.obstacle;

import tanks.*;

public class Obstacle implements IDrawableForInterface
{
	public boolean destructible = true;
	public boolean tankCollision = true;
	public boolean bulletCollision = true;
	//public boolean drawBelow = false;
	//public boolean drawAbove = false;
	
	public int drawLevel = 5;
	
	public boolean checkForObjects = false;
	public boolean update = false;
	public boolean draggable = true;
	public boolean bouncy = false;
	public boolean replaceTiles = false;

	public double posX;
	public double posY;
	public double colorR;
	public double colorG;
	public double colorB;
	public double colorA = 255;

	public static double draw_size = 0;
	public static double obstacle_size = Game.tank_size;

	public String name;
	public String description;

	//public int[] aposX = new int[]{-17, 5, 0};
	//public int[] aposY = new int[]{3, 30, 10};

	public Obstacle(String name, double posX, double posY)
	{
		this.name = name;
		this.posX = (int) ((posX + 0.5) * obstacle_size);
		this.posY = (int) ((posY + 0.5) * obstacle_size);
		double[] col = Obstacle.getRandomColor();
		this.colorR = col[0];
		this.colorG = col[1];
		this.colorB = col[2];

		this.description = "A solid block which can---be destroyed by mines";
	}
	
	@Override
	public void draw()
	{	
		Drawing drawing = Drawing.drawing;
		
		drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);

		if (Game.enable3d)
			drawing.fillBox(this.posX, this.posY, (obstacle_size - draw_size) / 2, draw_size, draw_size, draw_size);
		else
			drawing.fillRect(this.posX, this.posY, draw_size, draw_size);

		/*for (int i = 0; i < aposX.length; i++)
		{
			drawing.fillRect(aposX[i], aposY[i], 10, 10);
		}*/
	}
	
	@Override
	public void drawAt(double x, double y)
	{	
		double x1 = this.posX;
		double y1 = this.posY;
		this.posX = x;
		this.posY = y;
		this.draw();
		this.posX = x1;
		this.posY = y1;
	}

	@Override
	public void drawForInterface(double x, double y)
	{
		Drawing drawing = Drawing.drawing;
		
		drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
		drawing.fillInterfaceRect(x, y, draw_size, draw_size);
	}
	
	public void drawOutline()
	{
		Drawing drawing = Drawing.drawing;
		drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
		drawing.fillRect(this.posX - Obstacle.obstacle_size * 0.4, this.posY, Obstacle.obstacle_size * 0.2, Obstacle.obstacle_size);
		drawing.fillRect(this.posX + Obstacle.obstacle_size * 0.4, this.posY, Obstacle.obstacle_size * 0.2, Obstacle.obstacle_size);
		drawing.fillRect(this.posX, this.posY - Obstacle.obstacle_size * 0.4, Obstacle.obstacle_size, Obstacle.obstacle_size * 0.2);
		drawing.fillRect(this.posX, this.posY + Obstacle.obstacle_size * 0.4, Obstacle.obstacle_size, Obstacle.obstacle_size * 0.2);
	}
	
	public void onObjectEntry(Movable m)
	{
		
	}

	/** Only for visual effects which are to be handled by each client separately*/
	public void onObjectEntryLocal(Movable m)
	{

	}

	public void update()
	{
		
	}
	
	public boolean hasLeftNeighbor()
	{
		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			
			if (o.bulletCollision && o.posY == this.posY && this.posX - o.posX <= obstacle_size && this.posX - o.posX > 0)
				return true;
		}
		
		return false;
	}
	
	public boolean hasRightNeighbor()
	{
		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			
			if (o.bulletCollision && o.posY == this.posY && o.posX - this.posX <= obstacle_size && o.posX - this.posX > 0)
				return true;
		}
		
		return false;
	}
	
	public boolean hasUpperNeighbor()
	{
		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			
			if (o.bulletCollision && o.posX == this.posX && this.posY - o.posY <= obstacle_size && this.posY - o.posY > 0)
				return true;
		}
		
		return false;
	}
	
	public boolean hasLowerNeighbor()
	{
		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			
			if (o.bulletCollision && o.posX == this.posX && o.posY - this.posY <= obstacle_size && o.posY - this.posY > 0)
				return true;
		}
		
		return false;
	}
	
	public void drawTile(double r, double g, double b, double d) { }
	
	public void postOverride() 
	{
		Game.tileDrawables[(int) (this.posX / obstacle_size)][(int) (this.posY / obstacle_size)] = this;
	}
	
	public static double[] getRandomColor()
	{
		double colorMul = Math.random() * 0.5 + 0.5;
		double[] col = new double[3];
		
		if (Game.fancyGraphics)
		{
			col[0] = (colorMul * (176 - Math.random() * 70));
			col[1] = (colorMul * (111 - Math.random() * 34));
			col[2] = (colorMul * 14);

		}
		else
			col = new double[]{87, 46, 8};
		
		return col;
	}
}
