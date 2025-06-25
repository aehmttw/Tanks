package tanks.rendering;

import basewindow.BaseShapeBatchRenderer;
import basewindow.IBatchRenderableObject;
import basewindow.ShaderGroup;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.screen.ILevelPreviewScreen;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;

import java.util.HashMap;

public class StaticTerrainRenderer extends TerrainRenderer
{
    protected final HashMap<Class<? extends ShaderGroup>, RegionRenderer> renderers = new HashMap<>();
    public RegionRenderer outOfBoundsRenderer;

    public boolean staged = false;
    public boolean freed = false;

    protected float[] currentColor = new float[3];
    protected double currentDepth;

    public double offX;
    public double offY;

    public boolean asPreview = false;
    public int previewWidth = 0;

    protected ShaderGroundOutOfBounds outsideShader;

    public static int f(int i)
    {
        return 1664525 * i + 1013904223;
    }

    public StaticTerrainRenderer()
    {
        try
        {
            ShaderGroup ds = Game.game.window.shaderDefault;
            Game.game.shaderInstances.put(ds.getClass(), ds);

            this.outsideShader = Game.game.shaderOutOfBounds;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Game.exitToCrash(e);
        }
    }

    public RegionRenderer getRenderer(Class<? extends ShaderGroup> s)
    {
        return renderers.get(s);
    }

    public static class RegionRenderer
    {
        public BaseShapeBatchRenderer renderer;
        public ShaderGroup shader;

        public RegionRenderer(ShaderGroup s)
        {
            this.shader = s;
            int verts = Game.obstacles.size() * 300 + Game.currentSizeX * Game.currentSizeY * 30;
            this.renderer = Game.game.window.createStaticBatchRenderer(shader, true, null, false, verts);
        }
    }

    public RegionRenderer getRenderer(IBatchRenderableObject o, boolean outOfBounds)
    {
        RegionRenderer s = this.outOfBoundsRenderer;

        Class<? extends ShaderGroup> sg = ShaderGroup.class;

        if (o instanceof Obstacle)
            sg = ((Obstacle) o).renderer;
        else if (o instanceof RendererTile && ((RendererTile) o).obstacleAbove != null)
            sg = ((RendererTile) o).obstacleAbove.tileRenderer;

        if (!outOfBounds)
        {
            s = this.getRenderer(sg);
        }

        if (s == null)
        {
            this.renderers.put(sg, new RegionRenderer(getShader(sg)));
            s = renderers.get(sg);
        }

        return s;
    }

    public void addBox(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options, boolean out)
    {
        if (this.freed)
            Game.exitToCrash(new RuntimeException("Renderer was freed"));

        RegionRenderer r = this.getRenderer(o, out);
        BaseShapeBatchRenderer s = r.renderer;
        ShaderGroup shader = r.shader;

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

        float r2 = r1 * 0.8f;
        float g2 = g1 * 0.8f;
        float b2 = b1 * 0.8f;

        float r3 = r1 * 0.6f;
        float g3 = g1 * 0.6f;
        float b3 = b1 * 0.6f;

        int h = (int) (z / Game.tile_size) * 8;

        if (shader instanceof IGroundHeightShader)
            s.setAttribute(((IGroundHeightShader) shader).getGroundHeight(), (float) currentDepth);

        if (shader instanceof IGroundColorShader)
            s.setAttribute(((IGroundColorShader) shader).getGroundColor(), currentColor);

        if (options % 2 == 0)
        {
            s.setColor(r1, g1, b1, a);
            addVertexCoord(s, shader, h + 1f);
            s.addPoint(x1, y0, z0);
            addVertexCoord(s, shader, h + 0f);
            s.addPoint(x0, y0, z0);
            addVertexCoord(s, shader, h + 2f);
            s.addPoint(x0, y1, z0);

            addVertexCoord(s, shader, h + 1f);
            s.addPoint(x1, y0, z0);
            addVertexCoord(s, shader, h + 3f);
            s.addPoint(x1, y1, z0);
            addVertexCoord(s, shader, h + 2f);
            s.addPoint(x0, y1, z0);
        }

        if ((options >> 2) % 2 == 0)
        {
            s.setColor(r2, g2, b2, a);
            addVertexCoord(s, shader, h + 7f);
            s.addPoint(x1, y1, z1);
            addVertexCoord(s, shader, h + 6f);
            s.addPoint(x0, y1, z1);
            addVertexCoord(s, shader, h + 2f);
            s.addPoint(x0, y1, z0);

            addVertexCoord(s, shader, h + 7f);
            s.addPoint(x1, y1, z1);
            addVertexCoord(s, shader, h + 3f);
            s.addPoint(x1, y1, z0);
            addVertexCoord(s, shader, h + 2f);
            s.addPoint(x0, y1, z0);
        }

        if ((options >> 3) % 2 == 0)
        {
            s.setColor(r2, g2, b2, a);
            addVertexCoord(s, shader, h + 5f);
            s.addPoint(x1, y0, z1);
            addVertexCoord(s, shader, h + 4f);
            s.addPoint(x0, y0, z1);
            addVertexCoord(s, shader, h + 0f);
            s.addPoint(x0, y0, z0);

            addVertexCoord(s, shader, h + 5f);
            s.addPoint(x1, y0, z1);
            addVertexCoord(s, shader, h + 1f);
            s.addPoint(x1, y0, z0);
            addVertexCoord(s, shader, h + 0f);
            s.addPoint(x0, y0, z0);
        }

        if ((options >> 4) % 2 == 0)
        {
            s.setColor(r3, g3, b3, a);
            addVertexCoord(s, shader, h + 6f);
            s.addPoint(x0, y1, z1);
            addVertexCoord(s, shader, h + 2f);
            s.addPoint(x0, y1, z0);
            addVertexCoord(s, shader, h + 0f);
            s.addPoint(x0, y0, z0);

            addVertexCoord(s, shader, h + 6f);
            s.addPoint(x0, y1, z1);
            addVertexCoord(s, shader, h + 4f);
            s.addPoint(x0, y0, z1);
            addVertexCoord(s, shader, h + 0f);
            s.addPoint(x0, y0, z0);
        }

        if ((options >> 5) % 2 == 0)
        {
            s.setColor(r3, g3, b3, a);
            addVertexCoord(s, shader, h + 3f);
            s.addPoint(x1, y1, z0);
            addVertexCoord(s, shader, h + 7f);
            s.addPoint(x1, y1, z1);
            addVertexCoord(s, shader, h + 5f);
            s.addPoint(x1, y0, z1);

            addVertexCoord(s, shader, h + 3f);
            s.addPoint(x1, y1, z0);
            addVertexCoord(s, shader, h + 1f);
            s.addPoint(x1, y0, z0);
            addVertexCoord(s, shader, h + 5f);
            s.addPoint(x1, y0, z1);
        }

        if ((options >> 1) % 2 == 0)
        {
            s.setColor(r1, g1, b1, a);
            addVertexCoord(s, shader, h + 7f);
            s.addPoint(x1, y1, z1);
            addVertexCoord(s, shader, h + 6f);
            s.addPoint(x0, y1, z1);
            addVertexCoord(s, shader, h + 4f);
            s.addPoint(x0, y0, z1);

            addVertexCoord(s, shader, h + 7f);
            s.addPoint(x1, y1, z1);
            addVertexCoord(s, shader, h + 5f);
            s.addPoint(x1, y0, z1);
            addVertexCoord(s, shader, h + 4f);
            s.addPoint(x0, y0, z1);
        }
    }

    public void populateTiles()
    {
        this.rendererTiles = new RendererTile[Game.currentSizeX][Game.currentSizeY];
        for (int i = 0; i < Game.currentSizeX; i++)
        {
            for (int j = 0; j < Game.currentSizeY; j++)
            {
                this.rendererTiles[i][j] = new RendererTile();
            }
        }
    }

    public void reset()
    {
        for (RegionRenderer r : this.renderers.values())
        {
            r.renderer.free();
        }

        this.outOfBoundsRenderer.renderer.free();

        this.rendererTiles = null;
        this.renderers.clear();
        this.staged = false;

        this.freed = true;
    }

    public static int count = 0;
    public void drawMap(RegionRenderer s, int xOffset, int yOffset)
    {
        double sX = asPreview ? previewWidth : Game.currentSizeX;
        double x = xOffset * Game.tile_size * sX + offX;
        double y = yOffset * Game.tile_size * Game.currentSizeY + offY;
        double z = 0;
        double sc = 1;

        if (s.shader instanceof RendererShader)
            s.renderer.settings(((RendererShader) s.shader).depthTest, ((RendererShader) s.shader).glow, ((RendererShader) s.shader).depthMask);
        else
            s.renderer.settings(true, false, true);

        s.renderer.setPosition(Drawing.drawing.gameToAbsoluteX(x, 0), Drawing.drawing.gameToAbsoluteY(y, 0), z * Drawing.drawing.scale);
        s.renderer.setScale(Drawing.drawing.scale * sc, Drawing.drawing.scale * sc, Drawing.drawing.scale * sc);

        if (s.shader instanceof IGlowShader)
            s.renderer.setGlow(((IGlowShader) s.shader).getGlow());

        s.renderer.draw();
    }

    public void stage()
    {
        if (this.freed)
            Game.exitToCrash(new RuntimeException("Renderer was freed"));

        if (!staged)
        {
            this.stageBackground();
            this.stageObstacles();
            this.staged = true;
        }
    }

    public void draw()
    {
        if (this.freed)
            Game.exitToCrash(new RuntimeException("Renderer was freed"));

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

        if (asPreview)
        {
            xStart = -(100 / previewWidth) - 1;
            yStart = 0;
            xEnd = 100 / previewWidth + 1;
            yEnd = 0;
        }

        if (!(Game.screen instanceof ILevelPreviewScreen))
        {
            this.outsideShader.set();

            float size = (float) (Obstacle.draw_size / Game.tile_size);
            if (!(Game.screen instanceof ScreenGame))
                size = 0;

            this.outsideShader.setSize(size);

            if (size >= 0)
            {
                for (int x = xStart; x <= xEnd; x++)
                {
                    for (int y = yStart; y <= yEnd; y++)
                    {
                        if (x != 0 || y != 0)
                        {
                            this.drawMap(this.outOfBoundsRenderer, x, y);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 10; i++)
        {
            for (Class<? extends ShaderGroup> s : this.renderers.keySet())
            {
                try
                {
                    RendererDrawLayer drawLayer = s.getAnnotation(RendererDrawLayer.class);
                    if ((drawLayer == null && i == 5) || (drawLayer != null && drawLayer.value() == i))
                    {
                        ShaderGroup so = getShader(s);
                        so.set();

                        if (so instanceof IObstacleSizeShader)
                            ((IObstacleSizeShader) so).setSize((float) (Obstacle.draw_size / Game.tile_size));

                        if (so instanceof IObstacleTimeShader)
                            ((IObstacleTimeShader) so).setTime(((int) System.currentTimeMillis()) % 30000);

                        if (so instanceof IShrubHeightShader)
                            ((IShrubHeightShader) so).setShrubHeight(getShrubHeight());

                        this.drawMap(this.renderers.get(s), 0, 0);
                    }
                }
                catch (Exception e)
                {
                    Game.exitToCrash(e);
                }
            }
        }

        Game.game.window.shaderDefault.set();
    }

    public void stageBackground()
    {
        double s = Obstacle.draw_size;
        Obstacle.draw_size = Game.tile_size;

        this.populateTiles();

        for (int i = 0; i < this.rendererTiles.length; i++)
        {
            for (int j = 0; j < this.rendererTiles[i].length; j++)
            {
                this.drawTile(i, j);
            }
        }

        Obstacle.draw_size = s;
    }

    public void stageObstacles()
    {
        if (!Game.enable3d)
            return;

        double d = Obstacle.draw_size;
        Obstacle.draw_size = Game.tile_size;
        for (Obstacle o: Game.obstacles)
            drawObstacle(o);

        Obstacle.draw_size = d;
    }
}
