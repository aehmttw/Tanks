package tanks.gui;

import basewindow.BaseShapeBatchRenderer2;
import basewindow.IBatchRenderableObject;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;

import java.util.HashMap;

public class TerrainRenderer
{
    public static final int section_size = 400;
    protected final HashMap<Integer, RegionRenderer> renderers = new HashMap<>();
    protected final HashMap<IBatchRenderableObject, RegionRenderer> renderersByObj = new HashMap<>();
    protected final HashMap<Integer, RegionRenderer> outOfBoundsRenderers = new HashMap<>();
    public IBatchRenderableObject[][] tiles;
    public boolean staged = false;

    public static int f(int i)
    {
        return 1664525 * i + 1013904223;
    }

    public static class RegionRenderer
    {
        public BaseShapeBatchRenderer2 renderer = Game.game.window.createShapeBatchRenderer2();
        public int posX;
        public int posY;

        public RegionRenderer(int x, int y)
        {
            this.posX = x;
            this.posY = y;
        }
    }

    public RegionRenderer getRenderer(IBatchRenderableObject o, double x, double y, boolean outOfBounds)
    {
        RegionRenderer s = null;
        HashMap<Integer, RegionRenderer> renderers = this.outOfBoundsRenderers;

        if (!outOfBounds)
        {
            s = renderersByObj.get(o);
            renderers = this.renderers;
        }

        if (s == null)
        {
            int key = f((int) (f((int) (x / section_size)) + y / section_size));
            if (renderers.get(key) == null)
                renderers.put(key, new RegionRenderer((int) (x / section_size), (int) (y / section_size)));
            s = renderers.get(key);

            if (!outOfBounds)
                renderersByObj.put(o, s);
        }

        return s;
    }

    public void addRect(IBatchRenderableObject o, double x, double y, double sX, double sY, boolean out)
    {
        BaseShapeBatchRenderer2 s = this.getRenderer(o, x, y, out).renderer;
        s.beginAdd(o);

        float x0 = (float) x;
        float y0 = (float) y;

        float x1 = (float) (x + sX);
        float y1 = (float) (y + sY);

        float r1 = (float) Drawing.drawing.currentColorR;
        float g1 = (float) Drawing.drawing.currentColorG;
        float b1 = (float) Drawing.drawing.currentColorB;
        float a = (float) Drawing.drawing.currentColorA;
        float g = (float) Drawing.drawing.currentGlow;

        s.setColor(r1, g1, b1, a, g);
        s.addPoint(o, x1, y0, 0);
        s.addPoint(o, x0, y0, 0);
        s.addPoint(o, x0, y1, 0);

        s.addPoint(o, x1, y0, 0);
        s.addPoint(o, x1, y1, 0);
        s.addPoint(o, x0, y1, 0);

        s.endAdd();
    }

    public void addBox(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options, boolean out)
    {
        BaseShapeBatchRenderer2 s = this.getRenderer(o, x, y, out).renderer;
        s.beginAdd(o);

        float x0 = (float) x;
        float y0 = (float) y;
        float z0 = (float) z;

        float x1 = (float) (x + sX);
        float y1 = (float) (y + sY);
        float z1 = (float) (z + sZ);

        float r1 = (float) Drawing.drawing.currentColorR;
        float g1 = (float) Drawing.drawing.currentColorG;
        float b1 = (float) Drawing.drawing.currentColorB;
        float a = (float) Drawing.drawing.currentColorA;
        float g = (float) Drawing.drawing.currentGlow;

        float r2 = r1 * 0.8f;
        float g2 = g1 * 0.8f;
        float b2 = b1 * 0.8f;

        float r3 = r1 * 0.6f;
        float g3 = g1 * 0.6f;
        float b3 = b1 * 0.6f;

        if (options % 2 == 0)
        {
            s.setColor(r1, g1, b1, a, g);
            s.addPoint(o, x1, y0, z0);
            s.addPoint(o, x0, y0, z0);
            s.addPoint(o, x0, y1, z0);

            s.addPoint(o, x1, y0, z0);
            s.addPoint(o, x1, y1, z0);
            s.addPoint(o, x0, y1, z0);
        }

        if ((options >> 2) % 2 == 0)
        {
            s.setColor(r2, g2, b2, a, g);
            s.addPoint(o, x1, y1, z1);
            s.addPoint(o, x0, y1, z1);
            s.addPoint(o, x0, y1, z0);

            s.addPoint(o, x1, y1, z1);
            s.addPoint(o, x1, y1, z0);
            s.addPoint(o, x0, y1, z0);
        }

        if ((options >> 3) % 2 == 0)
        {
            s.setColor(r2, g2, b2, a, g);
            s.addPoint(o, x1, y0, z1);
            s.addPoint(o, x0, y0, z1);
            s.addPoint(o, x0, y0, z0);

            s.addPoint(o, x1, y0, z1);
            s.addPoint(o, x1, y0, z0);
            s.addPoint(o, x0, y0, z0);
        }

        if ((options >> 4) % 2 == 0)
        {
            s.setColor(r3, g3, b3, a, g);
            s.addPoint(o, x0, y1, z1);
            s.addPoint(o, x0, y1, z0);
            s.addPoint(o, x0, y0, z0);

            s.addPoint(o, x0, y1, z1);
            s.addPoint(o, x0, y0, z1);
            s.addPoint(o, x0, y0, z0);
        }

        if ((options >> 5) % 2 == 0)
        {
            s.setColor(r3, g3, b3, a, g);
            s.addPoint(o, x1, y1, z0);
            s.addPoint(o, x1, y1, z1);
            s.addPoint(o, x1, y0, z1);

            s.addPoint(o, x1, y1, z0);
            s.addPoint(o, x1, y0, z0);
            s.addPoint(o, x1, y0, z1);
        }

        if ((options >> 1) % 2 == 0)
        {
            s.setColor(r1, g1, b1, a, g);
            s.addPoint(o, x1, y1, z1);
            s.addPoint(o, x0, y1, z1);
            s.addPoint(o, x0, y0, z1);

            s.addPoint(o, x1, y1, z1);
            s.addPoint(o, x1, y0, z1);
            s.addPoint(o, x0, y0, z1);
        }

        s.endAdd();
    }

    public void remove(IBatchRenderableObject o)
    {
        this.getRenderer(o, 0, 0, false).renderer.delete(o);
    }

    public void populateTiles()
    {
        this.tiles = new IBatchRenderableObject[Game.currentSizeX][Game.currentSizeY];
        for (int i = 0; i < Game.currentSizeX; i++)
        {
            for (int j = 0; j < Game.currentSizeY; j++)
            {
                this.tiles[i][j] = new Tile();
            }
        }
    }

    public void reset()
    {
        for (RegionRenderer r: this.renderers.values())
        {
            r.renderer.free();
        }

        this.tiles = null;
        this.renderers.clear();
        this.renderersByObj.clear();
        this.staged = false;
    }

    public void drawMap(HashMap<Integer, RegionRenderer> renderers, int xOffset, int yOffset)
    {
        for (RegionRenderer s: renderers.values())
        {
            double x = xOffset * Game.tile_size * Game.currentSizeX;
            double y = yOffset * Game.tile_size * Game.currentSizeY;
            double z = 0;
            double sc = 1;

            boolean in = false;
            outer: for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    double x1 = Drawing.drawing.gameToAbsoluteX(x + (s.posX + i / 2.0) * section_size, 0);
                    double y1 = Drawing.drawing.gameToAbsoluteY(y + (s.posY + j / 2.0) * section_size, 0);

                    if (!Drawing.drawing.isOutOfBounds(x1, y1))
                    {
                        in = true;
                        break outer;
                    }
                }
            }

            if (in)
            {
                s.renderer.settings(true, false, true);
                s.renderer.setPosition(Drawing.drawing.gameToAbsoluteX(x, 0), Drawing.drawing.gameToAbsoluteY(y, 0), z * Drawing.drawing.scale);
                s.renderer.setScale(Drawing.drawing.scale * sc, Drawing.drawing.scale * sc, Drawing.drawing.scale * sc);
                s.renderer.draw();
            }
        }
    }

    public void draw()
    {
        if (!staged)
        {
            this.stageBackground();
            this.stageObstacles();
            this.staged = true;
        }

        this.drawMap(this.renderers, 0, 0);

        double width = (Game.game.window.absoluteWidth / Drawing.drawing.unzoomedScale / Game.tile_size);
        double height = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.unzoomedScale / Game.tile_size);

		double iStart = ((Game.currentSizeX - width) / 2.0) / Game.currentSizeX;
        double iEnd = width / Game.currentSizeX + iStart;
        double jStart = ((Game.currentSizeY - height) / 2.0) / Game.currentSizeY;
        double jEnd = height / Game.currentSizeY + jStart;

        int xStart = (int) Math.floor(iStart);
        int yStart = (int) Math.floor(jStart);
        int xEnd = (int) Math.floor(iEnd - 0.00001);
        int yEnd = (int) Math.floor(jEnd - 0.00001);

        if (!(Game.screen instanceof ScreenGame))
            for (int x = xStart; x <= xEnd; x++)
            {
                for (int y = yStart; y <= yEnd; y++)
                {
                    if (x != 0 || y != 0)
                    {
                        this.drawMap(this.outOfBoundsRenderers, x, y);
                    }
                }
            }
    }

    public void stageBackground()
    {
        double s = Obstacle.draw_size;
        Obstacle.draw_size = Game.tile_size;
        this.populateTiles();

        for (int i = 0; i < Game.obstacles.size(); i++)
        {
            Obstacle o = Game.obstacles.get(i);

            if (o.replaceTiles)
                o.postOverride();

            int x = (int) (o.posX / Game.tile_size);
            int y = (int) (o.posY / Game.tile_size);

            if (!(!Game.fancyTerrain || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY))
                Game.game.heightGrid[x][y] = Math.max(o.getTileHeight(), Game.game.heightGrid[x][y]);
        }

        for (int i = 0; i < this.tiles.length; i++)
        {
            for (int j = 0; j < this.tiles[i].length; j++)
            {
                double r = Game.tilesR[i][j];
                double g = Game.tilesG[i][j];
                double b = Game.tilesB[i][j];
                double depth = Game.tilesDepth[i][j];

                if (Game.tileDrawables[i][j] != null)
                    Game.tileDrawables[i][j].drawTile(this.tiles[i][j], r, g, b, depth, Game.tile_size);
                else
                {
                    Drawing.drawing.setColor(r, g, b);
                    this.addBox(this.tiles[i][j],
                            i * Game.tile_size,
                            j * Game.tile_size,
                            -Game.tile_size, Game.tile_size, Game.tile_size,
                            Game.tile_size + depth, (byte) 1, false);
                }

                this.addBox(this.tiles[i][j],
                        i * Game.tile_size,
                        j * Game.tile_size,
                        -Game.tile_size, Game.tile_size, Game.tile_size,
                        Game.tile_size + depth, (byte) 1, true);
            }
        }
        Obstacle.draw_size = s;
    }

    public void stageObstacles()
    {
        double d = Obstacle.draw_size;
        Obstacle.draw_size = Game.tile_size;
        for (Obstacle o: Game.obstacles)
        {
            if (o.batchDraw)
                o.draw();
        }
        Obstacle.draw_size = d;
    }

    public static class Tile implements IBatchRenderableObject
    {
       
    }
}
