package tanks.gui;

import basewindow.BaseShapeBatchRenderer2;
import basewindow.IBatchRenderableObject;
import tanks.Drawing;
import tanks.Game;

import java.util.HashMap;

public class TrackRenderer
{
    public static final int section_size = 400;
    protected final HashMap<Integer, RegionRenderer> renderers = new HashMap<>();
    protected final HashMap<IBatchRenderableObject, RegionRenderer> renderersByObj = new HashMap<>();
    public IBatchRenderableObject[][] tiles;

    public static int f(int i)
    {
        return 1664525 * i + 1013904223;
    }

    public class RegionRenderer
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

    public RegionRenderer getRenderer(IBatchRenderableObject o, double x, double y)
    {
        RegionRenderer s = renderersByObj.get(o);

        if (s == null)
        {
            int key = f((int) (f((int) (x / section_size)) + y / section_size));
            if (renderers.get(key) == null)
                renderers.put(key, new RegionRenderer((int) (x / section_size), (int) (y / section_size)));
            s = renderers.get(key);
            renderersByObj.put(o, s);
        }

        return s;
    }

    public void addRect(IBatchRenderableObject o, double x, double y, double z, double width, double height, double rotation)
    {
        BaseShapeBatchRenderer2 s = this.getRenderer(o, x, y).renderer;
        s.beginAdd(o);

        float r1 = (float) Drawing.drawing.currentColorR;
        float g1 = (float) Drawing.drawing.currentColorG;
        float b1 = (float) Drawing.drawing.currentColorB;
        float a = (float) Drawing.drawing.currentColorA;
        float g = (float) Drawing.drawing.currentGlow;

        s.setColor(r1, g1, b1, a, g);
        s.addPoint(o, (float) s.rotateX(-width / 2, -height / 2, x, rotation), (float) s.rotateY(-width / 2, -height / 2, y, rotation), (float) z);
        s.addPoint(o, (float) s.rotateX(width / 2, -height / 2, x, rotation), (float) s.rotateY(width / 2, -height / 2, y, rotation), (float) z);
        s.addPoint(o, (float) s.rotateX(width / 2, height / 2, x, rotation), (float) s.rotateY(width / 2, height / 2, y, rotation), (float) z);

        s.addPoint(o, (float) s.rotateX(-width / 2, -height / 2, x, rotation), (float) s.rotateY(-width / 2, -height / 2, y, rotation), (float) z);
        s.addPoint(o, (float) s.rotateX(-width / 2, height / 2, x, rotation), (float) s.rotateY(-width / 2, height / 2, y, rotation), (float) z);
        s.addPoint(o, (float) s.rotateX(width / 2, height / 2, x, rotation), (float) s.rotateY(width / 2, height / 2, y, rotation), (float) z);

        s.endAdd();
    }

    public void reset()
    {
        for (RegionRenderer r: this.renderers.values())
        {
            r.renderer.free();
        }

        this.tiles = null;
        this.renderers.clear();
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
                s.renderer.settings(true, false, false);
                s.renderer.setPosition(Drawing.drawing.gameToAbsoluteX(x, 0), Drawing.drawing.gameToAbsoluteY(y, 0), z * Drawing.drawing.scale);
                s.renderer.setScale(Drawing.drawing.scale * sc, Drawing.drawing.scale * sc, Drawing.drawing.scale * sc);
                s.renderer.draw();
            }
        }
    }

    public void remove(IBatchRenderableObject o)
    {
        this.getRenderer(o, 0, 0).renderer.delete(o);
    }

    public void draw()
    {
        this.drawMap(this.renderers, 0, 0);
    }
}
