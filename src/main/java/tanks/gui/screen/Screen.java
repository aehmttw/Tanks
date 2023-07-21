package tanks.gui.screen;

import basewindow.IBatchRenderableObject;
import tanks.*;
import tanks.obstacle.Obstacle;

import java.util.Arrays;

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

	public double centerX = Drawing.drawing.interfaceSizeX / 2;
	public double centerY = Drawing.drawing.interfaceSizeY / 2;

	public boolean selfBatch = true;
	public boolean forceInBounds = false;
	public int minBgWidth = 0;
	public int minBgHeight = 0;

	public boolean drawBgRect = true;

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
		if (!drawn)
		{
			Drawing.drawing.trackRenderer.reset();
			Drawing.drawing.terrainRenderer2.reset();
			this.drawn = true;
		}

		if (!(Game.screen instanceof IDarkScreen))
			Panel.darkness = Math.max(Panel.darkness - Panel.frameFrequency * 3, 0);

		if (Game.screen != Game.prevScreen || Game.game.window.hasResized)
			Drawing.drawing.forceRedrawTerrain();

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

		Drawing.drawing.terrainRendering = true;
		Drawing.drawing.terrainRenderer2.draw();
		Drawing.drawing.trackRenderer.draw();
		Drawing.drawing.terrainRendering = false;

//		if (!selfBatch || (Obstacle.draw_size > 0 && Obstacle.draw_size < Game.tile_size))
//		{
//			if (Game.game.window.shapeRenderer.supportsBatching)
//				Game.game.window.shapeRenderer.setBatchMode(true, true, true, false, true);
//		}
//		else if (Game.game.window.shapeRenderer.supportsBatching)
//			Drawing.drawing.beginTerrainRenderers();
//
//		if (Game.currentSizeX != Game.tilesR.length || Game.currentSizeY != Game.tilesR[0].length)
//			Game.resetTiles();
//
//		int width = (int) (Game.game.window.absoluteWidth / Drawing.drawing.unzoomedScale / Game.tile_size);
//		int height = (int) ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.unzoomedScale / Game.tile_size);
//
//		int iStart = (Game.currentSizeX - width) / 2 - 1;
//		int iEnd = width + 2 + iStart;
//
//		if (this.forceInBounds)
//		{
//			iStart = 0;
//			iEnd = Game.currentSizeX;
//		}
//
//		if (this.minBgWidth > (iEnd - iStart))
//		{
//			int m = (this.minBgWidth - (iEnd - iStart)) / 2;
//			iStart -= m;
//			iEnd += m;
//		}
//
//		for (int i1 = iStart; i1 < iEnd; i1++)
//		{
//			int i = i1;
//			while (i < 0)
//				i += Game.currentSizeX;
//
//			i = i % Game.currentSizeX;
//
//			int jStart = (Game.currentSizeY - height) / 2 - 1;
//			int jEnd = height + 2 + jStart;
//
//			if (this.forceInBounds)
//			{
//				jStart = 0;
//				jEnd = Game.currentSizeY;
//			}
//
//			if (this.minBgHeight > (jEnd - jStart))
//			{
//				jStart -= (this.minBgHeight - (jEnd - jStart)) / 2;
//				jEnd += (this.minBgHeight - (jEnd - jStart)) / 2;
//			}
//
//			for (int j1 = jStart; j1 < jEnd; j1++)
//			{
//				boolean inBounds = true;
//
//				int j = j1;
//				while (j < 0)
//					j += Game.currentSizeY;
//
//				j = j % Game.currentSizeY;
//
//				IBatchRenderableObject bo = this;
//
//				double frac2 = 0;
//				if (i1 >= 0 && i1 < Game.currentSizeX && j1 >= 0 && j1 < Game.currentSizeY)
//				{
//					if (this.splitTiles)
//						bo = this.tiles[i][j];
//
//					if (Game.fancyTerrain)
//						Drawing.drawing.setColor(getFlashCol(Game.tilesR[i][j], i, j), getFlashCol(Game.tilesG[i][j], i, j), getFlashCol(Game.tilesB[i][j], i, j));
//					else
//						Drawing.drawing.setColor(Level.currentColorR, Level.currentColorG, Level.currentColorB);
//				}
//				else
//				{
//					inBounds = false;
//					frac2 = frac;
//
//					if (frac >= 1)
//						continue;
//
//					if (this.splitTiles)
//						bo = this.tiles[i][j];
//
//					Drawing.drawing.setColor(174 * frac + (1 - frac) * getFlashCol(Game.tilesR[i][j], i, j), 92 * frac + (1 - frac) * getFlashCol(Game.tilesG[i][j], i, j), 16 * frac + (1 - frac) * getFlashCol(Game.tilesB[i][j], i, j));
//				}
//
//				if (Game.enable3d)
//				{
//					double z1 = 0;
//
//					byte o = 61;
//
//					if (Game.enable3dBg && Game.fancyTerrain && !(Drawing.drawing.scale <= 0.25 * Drawing.drawing.interfaceScale && !Game.game.window.shapeRenderer.supportsBatching))
//					{
//						z1 = Game.tilesDepth[i][j];
//						o = 1;
//					}
//
//					double extra = 0;
//
//					if (i > 0)
//						extra = Math.max(extra, -Game.game.heightGrid[i - 1][j]);
//
//					if (j > 0)
//						extra = Math.max(extra, -Game.game.heightGrid[i][j - 1]);
//
//					if (i < Game.currentSizeX - 1)
//						extra = Math.max(extra, -Game.game.heightGrid[i + 1][j]);
//
//					if (j < Game.currentSizeY - 1)
//						extra = Math.max(extra, -Game.game.heightGrid[i][j + 1]);
//
//
//					if (Game.tileDrawables[i][j] != null && inBounds)
//					{
//						Game.tileDrawables[i][j].drawTile(this, getFlashCol(Game.tilesR[i][j], i, j), getFlashCol(Game.tilesG[i][j], i, j), getFlashCol(Game.tilesB[i][j], i, j), z1, extra);
//
//						if (!Game.game.window.drawingShadow)
//							Game.tileDrawables[i][j] = null;
//					}
//					else
//					{
//						if (extra != 0)
//							o = 1;
//
//						if (size < 1)
//							Drawing.drawing.fillBox(bo,
//									(i1 + 0.5) / Game.bgResMultiplier * Game.tile_size,
//									(j1 + 0.5) / Game.bgResMultiplier * Game.tile_size,
//									Math.max(0, 2000 - size * 2000 * (1 + Game.tilesDepth[i][j] / 10)) - Game.tile_size + z1,
//									Game.tile_size / Game.bgResMultiplier,
//									Game.tile_size / Game.bgResMultiplier,
//									Game.tile_size);
//						else
//						{
//							Drawing.drawing.fillBox(bo,
//									(i1 + 0.5) / Game.bgResMultiplier * Game.tile_size,
//									(j1 + 0.5) / Game.bgResMultiplier * Game.tile_size,
//									-extra,
//									Game.tile_size / Game.bgResMultiplier,
//									Game.tile_size / Game.bgResMultiplier,
//									extra + z1 * (1 - frac2), o);
//						}
//					}
//				}
//				else
//					Drawing.drawing.fillRect(bo,
//							(i1 + 0.5) / Game.bgResMultiplier * Game.tile_size,
//							(j1 + 0.5) / Game.bgResMultiplier * Game.tile_size,
//							Game.tile_size * size / Game.bgResMultiplier,
//							Game.tile_size * size / Game.bgResMultiplier);
//			}
//		}
//
//		if (!selfBatch || (Obstacle.draw_size > 0 && Obstacle.draw_size < Game.tile_size))
//		{
//			if (Game.game.window.shapeRenderer.supportsBatching)
//				Game.game.window.shapeRenderer.setBatchMode(false, true, true, false, true);
//		}
//		else if (Game.game.window.shapeRenderer.supportsBatching && drawBgRect)
//			Drawing.drawing.drawTerrainRenderers();


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
