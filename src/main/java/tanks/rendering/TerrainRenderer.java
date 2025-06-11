package tanks.rendering;

import basewindow.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.ScreenIntro;
import tanks.gui.screen.*;
import tanks.obstacle.Obstacle;

import java.util.HashMap;

public class TerrainRenderer
{
    public static final int section_size = 2000;

    protected final HashMap<Class<? extends ShaderGroup>, Int2ObjectOpenHashMap<RegionRenderer>> renderers = new HashMap<>();
    protected final HashMap<IBatchRenderableObject, RegionRenderer> renderersByObj = new HashMap<>();
    protected final Int2ObjectOpenHashMap<RegionRenderer> outOfBoundsRenderers = new Int2ObjectOpenHashMap<>();

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

    public boolean allowPartialLoading = false;
    public int stagedCount = 0;
    protected boolean bgStaged = false;
    public int totalObjectsCount = 0;
    public boolean hasContinuationed = false;

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

            Game.game.shaderInstances.put(this.outsideShader.getClass(), this.outsideShader);
            Game.game.shaderInstances.put(this.introShader.getClass(), this.introShader);
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

    public Int2ObjectOpenHashMap<RegionRenderer> getRenderers(Class<? extends ShaderGroup> s)
    {
        return renderers.computeIfAbsent(s, k -> new Int2ObjectOpenHashMap<>());
    }

    public static class RegionRenderer
    {
        public BaseShapeBatchRenderer renderer;
        public int posX;
        public int posY;
        public int num = 0;
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
        Int2ObjectOpenHashMap<RegionRenderer> renderers = this.outOfBoundsRenderers;

        Class<? extends ShaderGroup> sg = ShaderGroup.class;

        if (Game.screen instanceof ScreenIntro || Game.screen instanceof ScreenExit)
            sg = ShaderGroundIntro.class;

        int num = 0;
        if (o instanceof Obstacle)
        {
            sg = ((Obstacle) o).renderer;
            num = ((Obstacle) o).rendererNumber;
        }
        else if (o instanceof Tile && ((Tile) o).obstacleAbove != null)
        {
            sg = ((Tile) o).obstacleAbove.tileRenderer;
            num = ((Tile) o).obstacleAbove.tileRendererNumber;
        }

        if (!outOfBounds)
        {
            s = renderersByObj.get(o);
            renderers = this.getRenderers(sg);
        }
        else if (!(Game.screen instanceof ScreenIntro || Game.screen instanceof ScreenExit))
            sg = ShaderGroundOutOfBounds.class;


        if (s == null)
        {
            int key = f( f((int) (f((int) (x / section_size)) + y / section_size)) + num);
            if (renderers.get(key) == null)
            {
                RegionRenderer r = new RegionRenderer((int) (x / section_size), (int) (y / section_size), getShader(sg));
                r.num = num;
                renderers.put(key, r);
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

    public void addCenterCoord(BaseShapeBatchRenderer s, ShaderGroup shader, float x, float y, float z)
    {
        if (shader instanceof IObstacleCenterCoordShader)
            s.setAttribute(((IObstacleCenterCoordShader) shader).getCenterCoord(), x, y, z);
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

    /**
     * Different rendering method, easier to use if you want the shrinking/growing blocks animation to work properly but maybe a bit slower
     */
    public void addBoxWithCenter(IBatchRenderableObject o, double x, double y, double z, double sX, double sY, double sZ, byte options, boolean alternate, float cx, float cy, float cz)
    {
        x -= sX / 2;
        y -= sY / 2;

        RegionRenderer r = this.getRenderer(o, x, y, false);
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

        if (alternate)
            cz = -cz;

        if (options % 2 == 0)
        {
            s.setColor(r1, g1, b1, a, g);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y0, z0);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y0, z0);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y1, z0);

            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y0, z0);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y1, z0);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y1, z0);
        }

        if ((options >> 2) % 2 == 0)
        {
            s.setColor(r2, g2, b2, a, g);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y1, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y1, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y1, z0);

            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y1, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y1, z0);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y1, z0);
        }

        if ((options >> 3) % 2 == 0)
        {
            s.setColor(r2, g2, b2, a, g);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y0, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y0, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y0, z0);

            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y0, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y0, z0);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y0, z0);
        }

        if ((options >> 4) % 2 == 0)
        {
            s.setColor(r3, g3, b3, a, g);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y1, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y1, z0);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y0, z0);

            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y1, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y0, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y0, z0);
        }

        if ((options >> 5) % 2 == 0)
        {
            s.setColor(r3, g3, b3, a, g);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y1, z0);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y1, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y0, z1);

            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y1, z0);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y0, z0);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y0, z1);
        }

        if ((options >> 1) % 2 == 0)
        {
            s.setColor(r1, g1, b1, a, g);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y1, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y1, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x0, y0, z1);

            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y1, z1);
            addCenterCoord(s, shader, cx, cy, cz);
            s.addPoint(x1, y0, z1);
            addCenterCoord(s, shader, cx, cy, cz);
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
        this.totalObjectsCount = this.tiles.length * this.tiles[0].length + Game.obstacles.size();

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
        for (Int2ObjectOpenHashMap<RegionRenderer> h : this.renderers.values())
            for (RegionRenderer r : h.values())
                r.renderer.free();

        for (RegionRenderer r : this.outOfBoundsRenderers.values())
            r.renderer.free();

        this.tiles = null;
        this.renderers.clear();
        this.renderersByObj.clear();
        this.outOfBoundsRenderers.clear();
        this.staged = false;
        this.stagedCount = 0;
    }

    public void drawMap(Int2ObjectOpenHashMap<RegionRenderer> renderers, int xOffset, int yOffset)
    {
        for (RegionRenderer s : renderers.values())
        {
            double sX = asPreview ? previewWidth : Game.currentSizeX;
            double x = xOffset * Game.tile_size * sX + offX;
            double y = yOffset * Game.tile_size * Game.currentSizeY + offY;
            double z = 0;
            double sc = 1;

            boolean in = Game.followingCam || asPreview || Drawing.drawing.isIncluded(x + s.posX * section_size, y + s.posY * section_size, x + (s.posX + 1) * section_size, y + (s.posY + 1) * section_size);

            s.renderer.endModification();

            if (in)
            {
                if (s.shader instanceof RendererShader)
                    s.renderer.settings(((RendererShader) s.shader).depthTest, ((RendererShader) s.shader).glow, ((RendererShader) s.shader).depthMask);
                else
                    s.renderer.settings(true, false, true);

                double x1 = Drawing.drawing.gameToAbsoluteX(x, 0);
                double y1 = Drawing.drawing.gameToAbsoluteY(y, 0);
                s.renderer.setPosition(x1, y1, z * Drawing.drawing.scale);
                s.renderer.setScale(Drawing.drawing.scale * sc, Drawing.drawing.scale * sc, Drawing.drawing.scale * sc);

                if (s.shader instanceof IGlowShader)
                    s.renderer.setGlow(((IGlowShader) s.shader).getGlow());

                if (s.shader instanceof IUpdatedShader)
                    ((IUpdatedShader) s.shader).update(s.num);

                s.renderer.draw();
            }
        }
    }

    public void draw()
    {
        if (!staged)
        {
            this.stageBackground();

            if (bgStaged)
                this.stageObstacles();

            if (this.stagedCount >= this.totalObjectsCount)
            {
                this.staged = true;
                if (this.hasContinuationed)
                {
                    this.hasContinuationed = false;
                    throw new LoadingTerrainContinuation(this);
                }
            }
            else
            {
                this.hasContinuationed = true;
                throw new LoadingTerrainContinuation(this);
            }
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

            for (Game.GroundTile t : Game.redrawGroundTiles)
            {
                this.drawTile(t.x, t.y);
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
            this.introShader.setSize((float) (Obstacle.draw_size / Game.tile_size));
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
            if (!(Game.screen instanceof ScreenGame || Game.screen instanceof ScreenExit))
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
                byte o = BaseShapeRenderer.hide_behind_face;
                if (!Game.fancyTerrain || !Game.enable3dBg)
                {
                    if (Game.sampleEdgeGroundDepth(i - 1, j) >= 0) o |= BaseShapeRenderer.hide_left_face;
                    if (Game.sampleEdgeGroundDepth(i + 1, j) >= 0) o |= BaseShapeRenderer.hide_right_face;
                    if (Game.sampleEdgeGroundDepth(i, j - 1) >= 0) o |= BaseShapeRenderer.hide_high_face;
                    if (Game.sampleEdgeGroundDepth(i, j + 1) >= 0) o |= BaseShapeRenderer.hide_low_face;
                }

                this.tiles[i][j].obstacleAbove = null;
                this.addBox(this.tiles[i][j],
                        i * Game.tile_size,
                        j * Game.tile_size,
                        -Game.tile_size, Game.tile_size, Game.tile_size,
                        Game.tile_size + depth, o, false);
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

        if (this.stagedCount <= 0)
        {
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
        }

        long startTime = System.currentTimeMillis();
        int i = stagedCount / this.tiles[0].length;
        for (; i < this.tiles.length; i++)
        {
            for (int j = 0; j < this.tiles[i].length; j++)
            {
                this.drawTile(i, j);
            }

            if (System.currentTimeMillis() - startTime > (this.hasContinuationed ? 50 : 100) && allowPartialLoading)
            {
                i++;
                break;
            }
        }
        stagedCount = Math.max(i * this.tiles[0].length, stagedCount);
        bgStaged = i >= this.tiles.length;
        Obstacle.draw_size = s;
    }

    public void stageObstacles()
    {
        double d = Obstacle.draw_size;
        Obstacle.draw_size = Game.tile_size;
        int oi = stagedCount - (this.tiles.length * this.tiles[0].length);

        long startTime = System.currentTimeMillis();
        for (; oi < Game.obstacles.size(); oi++)
        {
            Obstacle o = Game.obstacles.get(oi);
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

            if (System.currentTimeMillis() - startTime > (this.hasContinuationed ? 50 : 100) && allowPartialLoading)
            {
                oi++;
                break;
            }
        }
        stagedCount = oi + (this.tiles.length * this.tiles[0].length);

        Obstacle.draw_size = d;
    }

    public static class Tile implements IBatchRenderableObject
    {
        public Obstacle obstacleAbove = null;
    }
}
