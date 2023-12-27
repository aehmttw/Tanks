package tanks.gui.screen;

import basewindow.IBatchRenderableObject;
import tanks.*;
import tanks.gui.ScreenIntro;
import tanks.obstacle.Obstacle;
import tanks.rendering.StaticTerrainRenderer;

public abstract class Screen implements IBatchRenderableObject
{
	public String music = null;
	public String musicID = null;

	public String screenHint = "";
	public boolean showDefaultMouse = true;

	public boolean allowClose = true;

	public boolean enableMargins = true;
	public boolean drawDarkness = true;

	public double textSize = Drawing.drawing.textSize;
	public double titleSize = Drawing.drawing.titleSize;
	public double objWidth = Drawing.drawing.objWidth;
	public double objHeight = Drawing.drawing.objHeight;
	public double objXSpace = Drawing.drawing.objXSpace;
	public double objYSpace = Drawing.drawing.objYSpace;

	public double screenAge = 0;

	public double centerX = Drawing.drawing.interfaceSizeX / 2;
	public double centerY = Drawing.drawing.interfaceSizeY / 2;

	public boolean selfBatch = true;
	public boolean forceInBounds = false;
	public int minBgWidth = 0;
	public int minBgHeight = 0;

	public boolean drawBgRect = true;
	public boolean stageOnly = false;

	public double interfaceScaleZoomOverride = -1;

	protected boolean redrawn = false;
	public boolean splitTiles = false;
	public boolean drawn = false;

	public IBatchRenderableObject[][] tiles;

	public double lastObsSize;

	public Screen()
	{
		this.setupLayoutParameters();
	}

	public Screen(double objWidth, double objHeight, double objXSpace, double objYSpace)
	{
		//Game.game.window.setCursorLocked(false);

		this.objWidth = objWidth;
		this.objHeight = objHeight;
		this.objXSpace = objXSpace;
		this.objYSpace = objYSpace;

		this.textSize = this.objHeight * 0.6;
		this.titleSize = this.textSize * 1.25;

		this.setupLayoutParameters();
	}

	public abstract void update();

	public abstract void draw();

	public void drawPostMouse()
	{

	}

	public void drawDefaultBackground()
	{
		this.drawDefaultBackground(1);
	}

	public void drawDefaultBackground(double size)
	{
		if (Game.screen instanceof ScreenIntro && !(this instanceof ScreenIntro))
			return;

		if (!drawn)
			this.drawn = true;

		if (!(Game.screen instanceof IDarkScreen))
			Panel.darkness = Math.max(Panel.darkness - Panel.frameFrequency * 3, 0);

		for (int i = 0; i < Game.currentSizeX; i++)
		{
			for (int j = 0; j < Game.currentSizeY; j++)
			{
				if (Game.game.heightGrid[i][j] <= -1000)
					Game.game.heightGrid[i][j] = 0;
			}
		}

		double frac = 0;

		if (this instanceof ScreenGame || this instanceof ILevelPreviewScreen || (this instanceof IOverlayScreen
				&& !(this instanceof IConditionalOverlayScreen && !((IConditionalOverlayScreen) this).isOverlayEnabled())))
			frac = Obstacle.draw_size / Game.tile_size;

		if (this.forceInBounds)
			frac = 0;

		if (drawBgRect && (!(this instanceof ScreenExit) && size >= 1 && (selfBatch || (!Game.fancyTerrain && !Game.enable3d))))
		{
			Drawing.drawing.setColor(174 * frac + (1 - frac) * Level.currentColorR, 92 * frac + (1 - frac) * Level.currentColorG, 16 * frac + (1 - frac) * Level.currentColorB);

			double mul = 1;
			if (Game.angledView)
				mul = 2;

			Drawing.drawing.fillShadedInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2,
					mul * Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, mul * Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);

			Drawing.drawing.setColor(Level.currentColorR, Level.currentColorG, Level.currentColorB, 255.0 * size);
			Drawing.drawing.fillBackgroundRect(this, Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2, Drawing.drawing.sizeX, Drawing.drawing.sizeY);
		}

		Drawing.drawing.setColor(Level.currentColorR, Level.currentColorG, Level.currentColorB);

		if (stageOnly && Drawing.drawing.terrainRenderer instanceof StaticTerrainRenderer)
			((StaticTerrainRenderer) Drawing.drawing.terrainRenderer).stage();
		else
			Drawing.drawing.terrainRenderer.draw();

		Drawing.drawing.trackRenderer.draw();

		if (this.drawDarkness && drawBgRect)
		{
			Drawing.drawing.setColor(0, 0, 0, Math.max(0, Panel.darkness));
			Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);
		}

		this.lastObsSize = Obstacle.draw_size;
	}

	public double getFlashCol(double col, int i, int j)
	{
		return col * (1 - Game.tilesFlash[i][j]) + Game.tilesFlash[i][j] * 255;
	}

	public double getOffsetX()
	{
		return 0;
	}

	public double getOffsetY()
	{
		return 0;
	}

	public double getScale()
	{
		return Drawing.drawing.unzoomedScale;
	}

	public void setupLayoutParameters()
	{

	}

	public void onAttemptClose()
	{

	}

	public static class FlashingTile implements IBatchRenderableObject
	{
		public boolean redrawn = false;
		public int posX;
		public int posY;

		public double flash;

		public FlashingTile(int x, int y)
		{
			this.posX = x;
			this.posY = y;
		}
	}

	/** Setup all light info in Panel.lights to be sent to the shader */
	public void setupLights()
	{

	}
}
