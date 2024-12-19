package tanks.obstacle;

import basewindow.IBatchRenderableObject;
import basewindow.ShaderGroup;
import tanks.*;
import tanks.rendering.ShaderGroundObstacle;
import tanks.rendering.ShaderObstacle;

public abstract class Obstacle extends GameObject implements IDrawableForInterface, ISolidObject, IDrawableWithGlow, IBatchRenderableObject
{
	public Effect.EffectType destroyEffect = Effect.EffectType.obstaclePiece;
	public double destroyEffectAmount = 1;

	public boolean destructible = true;
	public boolean tankCollision = true;
	public boolean bulletCollision = true;

	/**
	 * If set to true, AI tanks will treat this block as breakable and shoot at it if there are tanks behind it
	 */
	public boolean shouldShootThrough = false;

	/**
	 * If set to a nonnegative value, will override how much AI controlled tanks will avoid pathfinding over this
	 */
	public int unfavorability = -1;

	/**
	 * Full = a full block, nothing can be placed underneath
	 * Ground = replaces the ground tile, can have blocks/tanks on top
	 * Top = can be placed on top of a ground tile, can have tanks inside
	 * Extra = can be placed anywhere without a full tile, can have tanks inside
	 * */
	public enum ObstacleType { full, ground, top, extra }
	public ObstacleType type = ObstacleType.full;

	public int drawLevel = 5;

	public boolean checkForObjects = false;
	public boolean update = false;
	public boolean bouncy = false;
	public boolean allowBounce = true;
	public boolean replaceTiles = true;

	/**
	 * If set to true, will draw as a VBO. Set to false for simpler rendering of more dynamic obstacles.
	 */
	public boolean batchDraw = true;
	public Class<? extends ShaderGroup> renderer = ShaderObstacle.class;
	public Class<? extends ShaderGroup> tileRenderer = ShaderGroundObstacle.class;

	/** Obstacles with different render numbers can have different values for their uniforms */
	public int rendererNumber = 0;
	public int tileRendererNumber = 0;

	public double colorR;
	public double colorG;
	public double colorB;
	public double colorA = 255;
	public double glow = 0;

	public boolean enableRotation = false;
	public double rotation;

	public static double draw_size = 0;

	public boolean removed = false;

	public String name;
	public String description;

	public Face[] horizontalFaces;
	public Face[] verticalFaces;

	protected boolean[] validFaces = new boolean[2];

	public double baseGroundHeight;

	public boolean shouldClip = false;
	public boolean collisionWhenClipped = true;
	public int clipFrames = 0;

	public Obstacle(String name, double posX, double posY)
	{
		this.name = name;
		this.posX = (int) ((posX + 0.5) * Game.tile_size);
		this.posY = (int) ((posY + 0.5) * Game.tile_size);
		this.draggable = true;

		this.baseGroundHeight = Game.sampleGroundHeight(this.posX, this.posY);

		this.description = "A solid block which can be destroyed by mines";
	}

	@Override
	public void drawGlow()
	{

	}

	@Override
	public boolean isGlowEnabled()
	{
		return false;
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

	public void drawOutlineAt(double x, double y)
	{
		double x1 = this.posX;
		double y1 = this.posY;
		this.posX = x;
		this.posY = y;
		this.drawOutline();
		this.posX = x1;
		this.posY = y1;
	}

	public void drawOutline()
	{
		drawOutline(this.colorR, this.colorG, this.colorB, this.colorA);
	}

	public void drawOutline(double r, double g, double b, double a)
	{
		Drawing.drawing.setColor(r, g, b, a);
		Drawing.drawing.drawRect(this.posX, this.posY, draw_size, draw_size, Game.tile_size * 0.2);
	}

	public void draw3dOutline()
	{
		draw3dOutline(this.colorR, this.colorG, this.colorB, 128);
	}

	public void draw3dOutline(double r, double g, double b)
	{
		draw3dOutline(r, g, b, 128);
	}

	public abstract void draw3dOutline(double r, double g, double b, double a);

	public void onObjectEntry(Movable m)
	{

	}

	/** Only for visual effects which are to be handled by each client separately*/
	public void onObjectEntryLocal(Movable m)
	{

	}

	public void update()
	{
		this.clipFrames--;
		if (this.clipFrames <= 0)
		{
			this.update = false;
			this.shouldClip = false;
		}
	}

	public void reactToHit(double bx, double by)
	{

	}

	public boolean hasNeighbor(int ox, int oy, boolean unbreakable)
	{
		int x = (int) (this.posX / Game.tile_size) + ox;
		int y = (int) (this.posY / Game.tile_size) + oy;

		if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
		{
			if (unbreakable)
				return Game.game.unbreakableGrid[x][y];
			else
				return Game.game.solidGrid[x][y];
		}

		return false;
	}

	public boolean hasLeftNeighbor()
	{
		return hasNeighbor(-1, 0, false);
	}

	public boolean hasRightNeighbor()
	{
		return hasNeighbor(1, 0, false);
	}

	public boolean hasUpperNeighbor()
	{
		return hasNeighbor(0, -1, false);
	}

	public boolean hasLowerNeighbor()
	{
		return hasNeighbor(0, 1, false);
	}

	/**
	 * Draws the tile under the obstacle if it needs to be drawn differently than when not covered by an obstacle
	 *
	 * @param r Red
	 * @param g Green
	 * @param b Blue
	 * @param d Tile height
	 * @param extra The deepest tile next to the current tile, used to render sides underground
	 */
	public void drawTile(IBatchRenderableObject tile, double r, double g, double b, double d, double extra)
	{
		Drawing.drawing.setColor(r, g, b);
		Drawing.drawing.fillBox(tile, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, extra + d);
	}

	public void postOverride()
	{
		int x = (int)(this.posX / Game.tile_size);
		int y = (int)(this.posY / Game.tile_size);

		if (x >= 0 && x < Game.tileDrawables.length && y >= 0 && y < Game.tileDrawables[0].length)
		{
			if (Game.tileDrawables[x][y] == null || Game.tileDrawables[x][y].type != ObstacleType.ground)
				Game.tileDrawables[x][y] = this;
		}
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

	public boolean[] getValidHorizontalFaces(boolean unbreakable)
	{
		this.validFaces[0] = false;
		this.validFaces[1] = false;
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

	public boolean[] getValidVerticalFaces(boolean unbreakable)
	{
		this.validFaces[0] = false;
		this.validFaces[1] = false;
		return this.validFaces;
	}

	/**
	 * @return height of tile in terms of drawing, for things like block particle collision
	 * */
	public abstract double getTileHeight();

	/**
	 * @return how deep the edges of the tile span - for example, ice tiles go down to -15, but most tiles only go down to 0
	 */
	public double getEdgeDrawDepth()
	{
		return 0;
	}

	/**
	 * Returns height of tile in terms of where objects like mines or treads should be drawn on top of it
	 * */
	public double getGroundHeight()
	{
		return -1000;
	}

	public byte getOptionsByte(double h)
	{
		/* TODO: maybe re-implement pruning hidden obstacle faces, especially if adding obstacle grid

		byte o = 0;

		if (Obstacle.draw_size < Game.tile_size)
			return 0;

		if (Game.sampleObstacleHeight(this.posX, this.posY + Game.tile_size) >= h)
			o += 4;

		if (Game.sampleObstacleHeight(this.posX, this.posY - Game.tile_size) >= h)
			o += 8;

		if (Game.sampleObstacleHeight(this.posX - Game.tile_size, this.posY) >= h)
			o += 16;

		if (Game.sampleObstacleHeight(this.posX + Game.tile_size, this.posY) >= h)
			o += 32;

		return o;*/

		return 0;
	}

	public void onDestroy(Movable source)
	{
		Game.removeObstacles.add(this);
	}

	public void playDestroyAnimation(double posX, double posY, double radius)
	{

	}

	public Effect getCompanionEffect()
	{
		return null;
	}

	public static boolean canPlaceOn(ObstacleType t1, ObstacleType t2)
	{
		if (t1 == ObstacleType.full || t2 == ObstacleType.full)
			return false;
		else if (t1 == ObstacleType.extra || t2 == ObstacleType.extra)
			return true;
		else
			return t1 != t2;
	}
}
