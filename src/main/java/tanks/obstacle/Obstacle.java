package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.IDrawableForInterface;
import tanks.Movable;

public class Obstacle implements IDrawableForInterface, ISolidObject
{
	public static final int default_max_height = 4;

	public boolean destructible = true;
	public boolean tankCollision = true;
	public boolean bulletCollision = true;

	public boolean enableStacking = true;
	public double stackHeight = 1;

	public boolean enableGroupID = false;
	public int groupID = 0;
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

	public double[] stackColorR = new double[default_max_height];
	public double[] stackColorG = new double[default_max_height];
	public double[] stackColorB = new double[default_max_height];

	public static double draw_size = 0;

	public String name;
	public String description;

	public Face[] horizontalFaces;
	public Face[] verticalFaces;

	protected boolean[] validFaces = new boolean[2];

	public Obstacle(String name, double posX, double posY)
	{
		this.name = name;
		this.posX = (int) ((posX + 0.5) * Game.tile_size);
		this.posY = (int) ((posY + 0.5) * Game.tile_size);
		double[] col = Obstacle.getRandomColor();
		this.colorR = col[0];
		this.colorG = col[1];
		this.colorB = col[2];

		for (int i = 0; i < default_max_height; i++)
		{
			double[] col2;

			if (i != 0)
				col2 = Obstacle.getRandomColor();
			else
				col2 = col;

			this.stackColorR[i] = col2[0];
			this.stackColorG[i] = col2[1];
			this.stackColorB[i] = col2[2];
		}

		this.description = "A solid block which can---be destroyed by mines";
	}
	
	@Override
	public void draw()
	{
		if (this.stackHeight <= 0)
			return;

		Drawing drawing = Drawing.drawing;
		
		drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);

		if (Game.enable3d)
		{
			for (int i = 0; i < Math.min(this.stackHeight, 4); i++)
			{
				int in = default_max_height - 1 - i;
				drawing.setColor(this.stackColorR[in], this.stackColorG[in], this.stackColorB[in], this.colorA);

				if (stackHeight % 1 == 0)
					drawing.fillBox(this.posX, this.posY, i * Game.tile_size, draw_size, draw_size, draw_size);
				else
					drawing.fillBox(this.posX, this.posY, (i - 1 + stackHeight % 1.0) * Game.tile_size, draw_size, draw_size, draw_size);
			}
		}
		else
			drawing.fillRect(this.posX, this.posY, draw_size, draw_size);
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
		drawing.fillRect(this.posX - Game.tile_size * 0.4, this.posY, Game.tile_size * 0.2, Game.tile_size);
		drawing.fillRect(this.posX + Game.tile_size * 0.4, this.posY, Game.tile_size * 0.2, Game.tile_size);
		drawing.fillRect(this.posX, this.posY - Game.tile_size * 0.4, Game.tile_size, Game.tile_size * 0.2);
		drawing.fillRect(this.posX, this.posY + Game.tile_size * 0.4, Game.tile_size, Game.tile_size * 0.2);
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
		int x = (int) (this.posX / Game.tile_size) - 1;
		int y = (int) (this.posY / Game.tile_size);

		if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
		{
			return Game.game.solidGrid[x][y];
		}

		return false;

		/*for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			
			if (o.bulletCollision && o.posY == this.posY && this.posX - o.posX <= Game.tile_size && this.posX - o.posX > 0)
				return true;
		}
		
		return false;*/
	}
	
	public boolean hasRightNeighbor()
	{
		int x = (int) (this.posX / Game.tile_size) + 1;
		int y = (int) (this.posY / Game.tile_size);

		if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
		{
			return Game.game.solidGrid[x][y];
		}

		return false;

		/*for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			
			if (o.bulletCollision && o.posY == this.posY && o.posX - this.posX <= Game.tile_size && o.posX - this.posX > 0)
				return true;
		}
		
		return false;*/
	}
	
	public boolean hasUpperNeighbor()
	{
		int x = (int) (this.posX / Game.tile_size);
		int y = (int) (this.posY / Game.tile_size) - 1;

		if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
		{
			return Game.game.solidGrid[x][y];
		}

		return false;

		/*for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			
			if (o.bulletCollision && o.posX == this.posX && this.posY - o.posY <= Game.tile_size && this.posY - o.posY > 0)
				return true;
		}
		
		return false;*/
	}
	
	public boolean hasLowerNeighbor()
	{
		int x = (int) (this.posX / Game.tile_size);
		int y = (int) (this.posY / Game.tile_size) + 1;

		if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
		{
			return Game.game.solidGrid[x][y];
		}

		return false;

		/*for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			
			if (o.bulletCollision && o.posX == this.posX && o.posY - this.posY <= Game.tile_size && o.posY - this.posY > 0)
				return true;
		}
		
		return false;*/
	}
	
	public void drawTile(double r, double g, double b, double d) { }
	
	public void postOverride() 
	{
		int x = (int)(this.posX / Game.tile_size);
		int y = (int)(this.posY / Game.tile_size);

		if (x >= 0 && x < Game.tileDrawables.length && y >= 0 && y < Game.tileDrawables[0].length)
			Game.tileDrawables[x][y] = this;
	}

	public void setMetadata(String data)
	{
		this.stackHeight = Double.parseDouble(data);
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

	@Override
	public Face[] getHorizontalFaces()
	{
		if (this.horizontalFaces == null)
		{
			this.horizontalFaces = new Face[2];
			double s = Game.tile_size / 2;
			this.horizontalFaces[0] = new Face(this, this.posX - s, this.posY - s, this.posX + s, this.posY - s, true, true, this.tankCollision, this.bulletCollision);
			this.horizontalFaces[1] = new Face(this, this.posX - s, this.posY + s, this.posX + s, this.posY + s, true, false, this.tankCollision, this.bulletCollision);
		}

		return this.horizontalFaces;
	}

	public boolean[] getValidHorizontalFaces()
	{
		this.validFaces[0] = !this.hasUpperNeighbor();
		this.validFaces[1] = !this.hasLowerNeighbor();
		return this.validFaces;
	}

	@Override
	public Face[] getVerticalFaces()
	{
		if (this.verticalFaces == null)
		{
			this.verticalFaces = new Face[2];
			double s = Game.tile_size / 2;
			this.verticalFaces[0] = new Face(this, this.posX - s, this.posY - s, this.posX - s, this.posY + s, false, true, this.tankCollision, this.bulletCollision);
			this.verticalFaces[1] = new Face(this, this.posX + s, this.posY - s, this.posX + s, this.posY + s, false, false, this.tankCollision, this.bulletCollision);
		}

		return this.verticalFaces;
	}

	public boolean[] getValidVerticalFaces()
	{
		this.validFaces[0] = !this.hasLeftNeighbor();
		this.validFaces[1] = !this.hasRightNeighbor();
		return this.validFaces;
	}
}
