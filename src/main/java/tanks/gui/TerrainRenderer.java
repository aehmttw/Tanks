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
    protected static final HashMap<Integer, SectionRenderer> renderers = new HashMap<>();
    protected final HashMap<IBatchRenderableObject, SectionRenderer> renderersByObj = new HashMap<>();
    protected IBatchRenderableObject[][] tiles;

    public static int f(int i)
    {
        return 1664525 * i + 1013904223;
    }

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

        if (s == null)
        {
            int key = f((int) (f((int) (x / section_size)) + y / section_size));
            if (renderers.get(key) == null)
                renderers.put(key, new SectionRenderer());
            s = renderers.get(key);
            renderersByObj.put(o, s);
        }

        return s;
    }

    public void addRect(IBatchRenderableObject o, double x, double y, double sX, double sY)
    {
        SectionRenderer s = this.getRenderer(o, x, y);
        s.renderer.setColor(Drawing.drawing.currentColorR, Drawing.drawing.currentColorG, Drawing.drawing.currentColorB, Drawing.drawing.currentColorA, Drawing.drawing.currentGlow);
        s.renderer.fillRect(o, x, y, sX, sY);
    }

    public void addBox(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options)
    {
        SectionRenderer s = this.getRenderer(o, x, y);
        s.renderer.setColor(Drawing.drawing.currentColorR, Drawing.drawing.currentColorG, Drawing.drawing.currentColorB, Drawing.drawing.currentColorA, Drawing.drawing.currentGlow);
        s.renderer.fillBox(o, x, y, z, sX, sY, sZ, options);
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
    }
}
