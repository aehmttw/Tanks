package tanks.gui;

import basewindow.BaseShapeBatchRenderer2;
import basewindow.IBatchRenderableObject;
import tanks.Drawing;
import tanks.Game;
import tanks.obstacle.Obstacle;

import java.util.HashMap;

public class TerrainRenderer
{
    public static final int section_size = 1000;
<<<<<<< HEAD
    protected final HashMap<Integer, RegionRenderer> renderers = new HashMap<>();
    protected final HashMap<IBatchRenderableObject, RegionRenderer> renderersByObj = new HashMap<>();
    public IBatchRenderableObject[][] tiles;
    public boolean staged = false;
=======
    protected static final HashMap<Integer, SectionRenderer> renderers = new HashMap<>();
    protected final HashMap<IBatchRenderableObject, SectionRenderer> renderersByObj = new HashMap<>();
    protected IBatchRenderableObject[][] tiles;
>>>>>>> 8b3cd24 (pre-rendering changes for obstacles)

    public static int f(int i)
    {
        return 1664525 * i + 1013904223;
    }

<<<<<<< HEAD
    public class RegionRenderer
    {
        public BaseShapeBatchRenderer2 renderer = Game.game.window.createShapeBatchRenderer2();
        public int posX;
        public int posY;

        public RegionRenderer(int x, int y)
        {
            System.out.println(x + " " + y);
            this.posX = x;
            this.posY = y;
        }
    }

    public RegionRenderer getRenderer(IBatchRenderableObject o, double x, double y)
    {
        RegionRenderer s = renderersByObj.get(o);
=======
    public static class SectionRenderer
    {
        public BaseShapeBatchRenderer2 renderer;

        public SectionRenderer()
        {
            this.renderer = Game.game.window.createShapeBatchRenderer2();
        }
    }

    public SectionRenderer getRenderer(IBatchRenderableObject o, double x, double y)
    {
        SectionRenderer s = renderersByObj.get(o);
>>>>>>> 8b3cd24 (pre-rendering changes for obstacles)

        if (s == null)
        {
            int key = f((int) (f((int) (x / section_size)) + y / section_size));
            if (renderers.get(key) == null)
<<<<<<< HEAD
                renderers.put(key, new RegionRenderer((int) (x / section_size), (int) (y / section_size)));
=======
                renderers.put(key, new SectionRenderer());
>>>>>>> 8b3cd24 (pre-rendering changes for obstacles)
            s = renderers.get(key);
            renderersByObj.put(o, s);
        }

        return s;
    }

    public void addRect(IBatchRenderableObject o, double x, double y, double sX, double sY)
    {
<<<<<<< HEAD
        BaseShapeBatchRenderer2 s = this.getRenderer(o, x, y).renderer;
        s.setColor(Drawing.drawing.currentColorR, Drawing.drawing.currentColorG, Drawing.drawing.currentColorB, Drawing.drawing.currentColorA, Drawing.drawing.currentGlow);
        s.fillRect(o, x, y, sX, sY);
=======
        SectionRenderer s = this.getRenderer(o, x, y);
        s.renderer.setColor(Drawing.drawing.currentColorR, Drawing.drawing.currentColorG, Drawing.drawing.currentColorB, Drawing.drawing.currentColorA, Drawing.drawing.currentGlow);
        s.renderer.fillRect(o, x, y, sX, sY);
>>>>>>> 8b3cd24 (pre-rendering changes for obstacles)
    }

    public void addBox(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options)
    {
<<<<<<< HEAD
        BaseShapeBatchRenderer2 s = this.getRenderer(o, x, y).renderer;
        s.setColor(Drawing.drawing.currentColorR, Drawing.drawing.currentColorG, Drawing.drawing.currentColorB, Drawing.drawing.currentColorA, Drawing.drawing.currentGlow);
        s.fillBox(o, x, y, z, sX, sY, sZ, options);
=======
        SectionRenderer s = this.getRenderer(o, x, y);
        s.renderer.setColor(Drawing.drawing.currentColorR, Drawing.drawing.currentColorG, Drawing.drawing.currentColorB, Drawing.drawing.currentColorA, Drawing.drawing.currentGlow);
        s.renderer.fillBox(o, x, y, z, sX, sY, sZ, options);
>>>>>>> 8b3cd24 (pre-rendering changes for obstacles)
    }

    public void remove(IBatchRenderableObject o)
    {
        this.getRenderer(o, 0, 0).renderer.delete(o);
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

<<<<<<< HEAD
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

    public void draw()
    {
        if (!staged)
        {
            this.stageBackground();
            this.stageObstacles();
            this.staged = true;
        }

        for (RegionRenderer s: this.renderers.values())
        {
            double x = 0;
            double y = 0;
            double z = 0;
            double sc = 1;

            boolean in = false;
            outer: for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    double x1 = Drawing.drawing.gameToAbsoluteX((s.posX + i / 2.0) * section_size, 0);
                    double y1 = Drawing.drawing.gameToAbsoluteY((s.posY + j / 2.0) * section_size, 0);

                    if (!Drawing.drawing.isOutOfBounds(x1, y1))
                    {
                        in = true;
                        break outer;
                    }
                }
            }

            if (in)
            {
                s.renderer.settings(true);
                s.renderer.setPosition(Drawing.drawing.gameToAbsoluteX(x, 0), Drawing.drawing.gameToAbsoluteY(y, 0), z * Drawing.drawing.scale);
                s.renderer.setScale(Drawing.drawing.scale * sc, Drawing.drawing.scale * sc, Drawing.drawing.scale * sc);
                s.renderer.draw();
            }
            else
            {
               // System.out.println("skipped");
            }
        }
    }

    public void stageBackground()
    {
        this.populateTiles();

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
                    Drawing.drawing.fillBox(this.tiles[i][j],
                            (i + 0.5) * Game.tile_size,
                            (j + 0.5) * Game.tile_size,
                            -Game.tile_size,
                            Game.tile_size,
                            Game.tile_size,
                            Game.tile_size + depth, (byte) 1);
                }
            }
        }
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
       
=======
    public void draw()
    {
        for (SectionRenderer s: this.renderersByObj.values())
        {
            s.renderer.draw();
        }
    }

    public static class Tile implements IBatchRenderableObject
    {
        @Override
        public boolean positionChanged()
        {
            return false;
        }

        @Override
        public boolean colorChanged()
        {
            return false;
        }

        @Override
        public boolean wasRedrawn()
        {
            return false;
        }

        @Override
        public void setRedrawn(boolean b)
        {

        }
>>>>>>> 8b3cd24 (pre-rendering changes for obstacles)
    }
}
