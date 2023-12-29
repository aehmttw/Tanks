package tanks.rendering;

import basewindow.*;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.ScreenIntro;
import tanks.gui.screen.*;
import tanks.obstacle.Obstacle;

import java.util.HashMap;

public class TerrainRenderer
{
    public static final int section_size = 2000;

    protected final HashMap<Class<? extends ShaderGroup>, HashMap<Integer, RegionRenderer>> renderers = new HashMap<>();
    protected final HashMap<IBatchRenderableObject, RegionRenderer> renderersByObj = new HashMap<>();
    protected final HashMap<Integer, RegionRenderer> outOfBoundsRenderers = new HashMap<>();

    public Tile[][] tiles;

    public boolean staged = false;

    protected float[] currentColor = new float[3];
    protected double currentDepth;

    public double offX;
    public double offY;

    public boolean asPreview = false;
    public int previewWidth = 0;

    protected ShaderGroundOutOfBounds outsideShader;
    protected ShaderGroundIntro introShader;

    public static int f(int i)
    {
        return 1664525 * i + 1013904223;
    }

    public TerrainRenderer()
    {
        try
        {
            ShaderGroup ds = Game.game.window.shaderDefault;
            Game.game.shaderInstances.put(ds.getClass(), ds);

            this.outsideShader = Game.game.shaderOutOfBounds;
            this.introShader = Game.game.shaderIntro;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Game.exitToCrash(e);
        }
    }

    public ShaderGroup getShader(Class<? extends ShaderGroup> shaderClass)
    {
        ShaderGroup s = Game.game.shaderInstances.get(shaderClass);
        if (s != null)
            return s;
        else
        {
            try
            {
                s = shaderClass.getConstructor(BaseWindow.class).newInstance(Game.game.window);
                s.initialize();
                Game.game.shaderInstances.put(shaderClass, s);
                return s;
            }
            catch (Exception e)
            {
                Game.exitToCrash(e);
                return null;
            }
        }
    }

    public HashMap<Integer, RegionRenderer> getRenderers(Class<? extends ShaderGroup> s)
    {
        HashMap<Integer, RegionRenderer> m = this.renderers.get(s);
        if (m == null)
            this.renderers.put(s, new HashMap<>());
        return renderers.get(s);
    }

    public static class RegionRenderer
    {
        public BaseShapeBatchRenderer renderer;

        public int posX;

        public int posY;

        public ShaderGroup shader;

        public RegionRenderer(int x, int y, ShaderGroup s)
        {
            this.posX = x;
            this.posY = y;
            this.shader = s;
            this.renderer = Game.game.window.createShapeBatchRenderer(shader);
        }
    }

    public RegionRenderer getRenderer(IBatchRenderableObject o, double x, double y, boolean outOfBounds)
    {
        RegionRenderer s = null;
        HashMap<Integer, RegionRenderer> renderers = this.outOfBoundsRenderers;

        Class<? extends ShaderGroup> sg = ShaderGroup.class;

        if (Game.screen instanceof ScreenIntro || Game.screen instanceof ScreenExit)
            sg = ShaderGroundIntro.class;

        if (o instanceof Obstacle)
            sg = ((Obstacle) o).renderer;
        else if (o instanceof Tile && ((Tile) o).obstacleAbove != null)
            sg = ((Tile) o).obstacleAbove.tileRenderer;

        if (!outOfBounds)
        {
            s = renderersByObj.get(o);
            renderers = this.getRenderers(sg);
        }

        if (s == null)
        {
            int key = f((int) (f((int) (x / section_size)) + y / section_size));
            if (renderers.get(key) == null)
            {
                renderers.put(key, new RegionRenderer((int) (x / section_size), (int) (y / section_size), getShader(sg)));
            }

            s = renderers.get(key);

            if (!outOfBounds)
                renderersByObj.put(o, s);
        }

        return s;
    }

    public void addVertexCoord(BaseShapeBatchRenderer s, ShaderGroup shader, float f)
    {
        if (shader instanceof IObstacleVertexCoordShader)
            s.setAttribute(((IObstacleVertexCoordShader) shader).getVertexCoord(), f);
    }

    public void addBox(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options, boolean out)
    {
        RegionRenderer r = this.getRenderer(o, x, y, out);
        BaseShapeBatchRenderer s = r.renderer;
        s.beginAdd(o);
        ShaderGroup shader = r.shader;

        if (shader instanceof IGroundHeightShader)
        {
            if (!Game.enable3dBg && Game.screen instanceof ScreenIntro)
                s.setAttribute(((IGroundHeightShader) shader).getGroundHeight(), (float) (Math.random() * 10.0));
            else
                s.setAttribute(((IGroundHeightShader) shader).getGroundHeight(), (float) currentDepth);
        }

        if (shader instanceof IGroundColorShader)
            s.setAttribute(((IGroundColorShader) shader).getGroundColor(), currentColor);

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

        int h = (int) (z / Game.tile_size) * 8;

        if (options % 2 == 0)
        {
            s.setColor(r1, g1, b1, a, g);
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
            s.setColor(r2, g2, b2, a, g);
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
            s.setColor(r2, g2, b2, a, g);
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
            s.setColor(r3, g3, b3, a, g);
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
            s.setColor(r3, g3, b3, a, g);
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
            s.setColor(r1, g1, b1, a, g);
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

    public void remove(IBatchRenderableObject o)
    {
        this.getRenderer(o, 0, 0, false).renderer.delete(o);
        this.renderersByObj.remove(o);
    }

    public void populateTiles()
    {
        this.tiles = new Tile[Game.currentSizeX][Game.currentSizeY];
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
        for (HashMap<Integer, RegionRenderer> h : this.renderers.values())
        {
            for (RegionRenderer r : h.values())
            {
                r.renderer.free();
            }
        }

        for (RegionRenderer r : this.outOfBoundsRenderers.values())
        {
            r.renderer.free();
        }

        this.tiles = null;
        this.renderers.clear();
        this.renderersByObj.clear();
        this.outOfBoundsRenderers.clear();
        this.staged = false;
    }

    public void drawMap(HashMap<Integer, RegionRenderer> renderers, int xOffset, int yOffset)
    {
        for (RegionRenderer s : renderers.values())
        {
            double sX = asPreview ? previewWidth : Game.currentSizeX;
            double x = xOffset * Game.tile_size * sX + offX;
            double y = yOffset * Game.tile_size * Game.currentSizeY + offY;
            double z = 0;
            double sc = 1;

            boolean in = Game.followingCam || asPreview || Drawing.drawing.isIncluded(x + s.posX * section_size, y + s.posY * section_size, x + (s.posX + 1) * section_size, y + (s.posY + 1) * section_size);

            if (in)
            {
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
        else
        {
            for (Obstacle o : Game.redrawObstacles)
            {
                int i = Math.max(0, Math.min(Game.currentSizeX - 1, (int) (o.posX / Game.tile_size)));
                int j = Math.max(0, Math.min(Game.currentSizeY - 1, (int) (o.posY / Game.tile_size)));
                double r = Game.tilesR[i][j];
                double g = Game.tilesG[i][j];
                double b = Game.tilesB[i][j];
                this.currentDepth = Game.tilesDepth[i][j];
                currentColor[0] = (float) (r / 255.0);
                currentColor[1] = (float) (g / 255.0);
                currentColor[2] = (float) (b / 255.0);

                if (o.batchDraw && !o.removed)
                    o.draw();
            }

            for (int[] t : Game.redrawGroundTiles)
            {
                this.drawTile(t[0], t[1]);
            }

            Game.redrawObstacles.clear();
            Game.redrawGroundTiles.clear();
        }

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
            xStart = -(100 / previewWidth - 1);
            yStart = 0;
            xEnd = 100 / previewWidth - 1;
            yEnd = 0;
        }

        if (Game.screen instanceof ScreenIntro || Game.screen instanceof ScreenExit)
        {
            this.introShader.set();
            this.introShader.obstacleSizeFrac.set((float) Obstacle.draw_size);
            this.introShader.d3.set(Game.enable3d);

            for (int x = xStart; x <= xEnd; x++)
            {
                for (int y = yStart; y <= yEnd; y++)
                {
                    this.drawMap(this.outOfBoundsRenderers, x, y);
                }
            }

            Game.game.window.shaderDefault.set();
            return;
        }

        if (!(Game.screen instanceof ILevelPreviewScreen) && !(Game.screen instanceof IConditionalOverlayScreen && ((IConditionalOverlayScreen) Game.screen).isOverlayEnabled()))
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
                        if (Game.screen instanceof IBlankBackgroundScreen || (Game.screen instanceof IConditionalOverlayScreen) || x != 0 || y != 0)
                        {
                            this.drawMap(this.outOfBoundsRenderers, x, y);
                        }
                    }
                }
            }
        }

        if (!(Game.screen instanceof IBlankBackgroundScreen || (Game.screen instanceof IConditionalOverlayScreen && !((IConditionalOverlayScreen) Game.screen).isOverlayEnabled())))
        {
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
        }

        Game.game.window.shaderDefault.set();
    }

    public void drawTile(int i, int j)
    {
        double r = Game.tilesR[i][j];
        double g = Game.tilesG[i][j];
        double b = Game.tilesB[i][j];
        double depth = Game.tilesDepth[i][j];
        this.currentDepth = depth;
        currentColor[0] = (float) (r / 255.0);
        currentColor[1] = (float) (g / 255.0);
        currentColor[2] = (float) (b / 255.0);

        this.remove(this.tiles[i][j]);

        Drawing.drawing.setColor(r, g, b);

        if (Game.enable3d)
        {
            if (Game.tileDrawables[i][j] != null && !Game.tileDrawables[i][j].removed)
            {
                this.tiles[i][j].obstacleAbove = Game.tileDrawables[i][j];
                Game.tileDrawables[i][j].drawTile(this.tiles[i][j], r, g, b, depth, Game.tile_size);
            }
            else
            {
                this.tiles[i][j].obstacleAbove = null;
                this.addBox(this.tiles[i][j],
                        i * Game.tile_size,
                        j * Game.tile_size,
                        -Game.tile_size, Game.tile_size, Game.tile_size,
                        Game.tile_size + depth, BaseShapeRenderer.hide_behind_face, false);
            }
        }
        else
        {
            this.addBox(this.tiles[i][j],
                    i * Game.tile_size,
                    j * Game.tile_size,
                    0, Game.tile_size, Game.tile_size,
                    0, (byte) ~(BaseShapeRenderer.hide_front_face), false);
        }

        if (!this.staged)
        {
            if (Game.enable3d)
                this.addBox(this.tiles[i][j],
                    i * Game.tile_size,
                    j * Game.tile_size,
                    -Game.tile_size, Game.tile_size, Game.tile_size,
                    Game.tile_size + depth, BaseShapeRenderer.hide_behind_face, true);
            else
                this.addBox(this.tiles[i][j],
                        i * Game.tile_size,
                        j * Game.tile_size,
                        0, Game.tile_size, Game.tile_size,
                        0, (byte) ~(BaseShapeRenderer.hide_front_face), true);
        }
    }

    public float getShrubHeight()
    {
        float shrubMod = 0.25f;
        if (Game.screen instanceof ScreenGame)
            shrubMod = (float) ((ScreenGame) Game.screen).shrubberyScale;

        return shrubMod;
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
                this.drawTile(i, j);
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
            int i = Math.max(0, Math.min(Game.currentSizeX - 1, (int) (o.posX / Game.tile_size)));
            int j = Math.max(0, Math.min(Game.currentSizeY - 1, (int) (o.posY / Game.tile_size)));
            double r = Game.tilesR[i][j];
            double g = Game.tilesG[i][j];
            double b = Game.tilesB[i][j];
            this.currentDepth = Game.tilesDepth[i][j];
            currentColor[0] = (float) (r / 255.0);
            currentColor[1] = (float) (g / 255.0);
            currentColor[2] = (float) (b / 255.0);

            if (o.batchDraw)
                o.draw();
        }
        Obstacle.draw_size = d;
    }

    public static class Tile implements IBatchRenderableObject
    {
        public Obstacle obstacleAbove = null;
    }
}
