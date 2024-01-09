package tanks.gui.screen;

import basewindow.transformation.*;
import tanks.*;
import tanks.obstacle.Obstacle;
import tanks.rendering.StaticTerrainRenderer;
import tanks.rendering.TerrainRenderer;
import tanks.tank.TankAIControlled;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ScreenCrusadeLevels extends Screen implements ILevelPreviewScreen
{
    public static class ScreenLevel
    {
        public ArrayList<Movable> movables = new ArrayList<>();
        public ArrayList<Obstacle> obstacles = new ArrayList<>();
        public ArrayList<TankAIControlled> tanks = new ArrayList<>();
        public String levelString;
        public Level level;
        public StaticTerrainRenderer renderer;
        public boolean isTransition = false;

        public int startIndex;
        public int endIndex;
        int width;
    }

    protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);
    protected RotationAboutPoint transform = new RotationAboutPoint(Game.game.window, 0, -Math.PI / 4, 0, 0, 0, -1);
    protected Translation translation = new Translation(Game.game.window, 0, 0, 0);

    double shadowScaleNum = 0.2;
    protected ScaleAboutPoint shadowScale = new ScaleAboutPoint(Game.game.window, shadowScaleNum, shadowScaleNum, shadowScaleNum, 0.5, 0.5, 0.5);

    protected ArrayList<ScreenLevel> levels = new ArrayList<>();
    protected HashMap<Integer, ScreenLevel> levelsPos = new HashMap<>();
    protected ArrayList<TankSpawnMarker> spawns = new ArrayList<>();
    protected double age = 360;
    protected boolean initialized = false;
    protected Crusade crusade;

    protected boolean allLoaded = false;
    protected int index = 0;
    protected int levelsLoaded = 0;

    public double lastR = Level.currentColorR;
    public double lastG = Level.currentColorG;
    public double lastB = Level.currentColorB;
    public double lastDR = Level.currentColorVarR;
    public double lastDG = Level.currentColorVarG;
    public double lastDB = Level.currentColorVarB;

    protected static ScreenCrusadeLevels currentScreen;

    public ScreenCrusadeLevels(Crusade c)
    {
        if (currentScreen != null)
        {
            for (ScreenLevel l: currentScreen.levels)
            {
                if (l.renderer != null)
                    l.renderer.reset();
            }
        }

        currentScreen = this;

        for (int i = 0; i < drawables.length; i++)
        {
            drawables[i] = new ArrayList<>();
        }

        this.crusade = c;
        this.selfBatch = false;
        this.forceInBounds = true;
        this.minBgWidth = 100;
    }

    public void initialize(ScreenLevel l)
    {
        l.renderer = new StaticTerrainRenderer();
        l.renderer.asPreview = true;

        ArrayList<Movable> movables = Game.movables;
        ArrayList<Obstacle> obstacles = Game.obstacles;

        Game.cleanUp();

        l.level = new Level(l.levelString);
        l.level.tilesRandomSeed = (int) (Math.random() * 1000);
        l.level.customTanks = l.tanks;

        if (!l.isTransition)
            addTransitionLevels(l);

        Game.movables = l.movables;
        Game.obstacles = l.obstacles;

        l.level.loadLevel(this);
        l.renderer.outOfBoundsRenderer = new StaticTerrainRenderer.RegionRenderer(Game.game.shaderOutOfBounds);

        l.startIndex = index;
        index += l.level.sizeY;
        l.width = l.level.sizeX;
        l.endIndex = index;
        l.renderer.previewWidth = l.level.sizeX;
        this.levelsPos.put(index - 1, l);

        TerrainRenderer r = Drawing.drawing.terrainRenderer;
        Drawing.drawing.terrainRenderer = l.renderer;

        for (int i = 0; i < Game.game.heightGrid.length; i++)
        {
            Arrays.fill(Game.game.heightGrid[i], -1000);
            Arrays.fill(Game.game.groundHeightGrid[i], -1000);
        }

        this.drawBgRect = false;
        this.stageOnly = true;
        this.drawDefaultBackground();
        this.drawBgRect = true;
        this.stageOnly = false;

        Drawing.drawing.terrainRenderer = r;

        Game.movables = movables;
        Game.obstacles = obstacles;

        Game.cleanUp();
    }

    @Override
    public void update()
    {

    }

    public void addTransitionLevels(ScreenLevel l)
    {
        int fade = 8;
        for (int f = 0; f < fade; f++)
        {
            ScreenLevel l2 = new ScreenLevel();
            double frac = f * 1.0 / fade;
            int r = (int) (this.lastR * (1 - frac) + l.level.colorR * frac);
            int g = (int) (this.lastG * (1 - frac) + l.level.colorG * frac);
            int b = (int) (this.lastB * (1 - frac) + l.level.colorB * frac);
            int dr = (int) (this.lastDR * (1 - frac) + l.level.colorVarR * frac);
            int dg = (int) (this.lastDG * (1 - frac) + l.level.colorVarG * frac);
            int db = (int) (this.lastDB * (1 - frac) + l.level.colorVarB * frac);
            l2.levelString = "{28,1," + r + "," + g + "," + b + "," + dr + "," + dg + "," + db + "||100-0-player}";
            Game.movables = l2.movables;
            Game.obstacles = l2.obstacles;
            l2.isTransition = true;
            initialize(l2);
        }

        this.lastR = l.level.colorR;
        this.lastG = l.level.colorG;
        this.lastB = l.level.colorB;
        this.lastDR = l.level.colorVarR;
        this.lastDG = l.level.colorVarG;
        this.lastDB = l.level.colorVarB;
    }

    public double getLevelPos(double i)
    {
        if (allLoaded)
            return (i + index * 10) % index;
        else
            return i;
    }

    public void draw()
    {
        TerrainRenderer r = Drawing.drawing.terrainRenderer;
        Transformation prevShadow = Game.game.window.lightBaseTransformation[0];
        Game.game.window.lightBaseTransformation[0] = this.shadowScale;

        if (Game.enable3d)
        {
            Game.game.window.transformations.add(this.transform);
        }

        Game.game.window.loadPerspective();

        if (Game.game.window.drawingShadow || !Game.shadowsEnabled)
            this.age += Panel.frameFrequency;

        ArrayList<Movable> movables = Game.movables;
        ArrayList<Obstacle> obstacles = Game.obstacles;

        if (!initialized)
        {
            ScreenLevel l0 = new ScreenLevel();
            this.levels.add(l0);
            l0.levelString = "{28,18||10000-0-player}";

            for (Crusade.CrusadeLevel level: this.crusade.levels)
            {
                ScreenLevel l = new ScreenLevel();
                this.levels.add(l);
                l.levelString = level.levelString;
                l.tanks = level.tanks;
            }

            ScreenLevel l = new ScreenLevel();
            this.levels.add(l);
            l.levelString = "{28,18||10000-0-player}";
        }

        double indexStart = getLevelPos(this.age / 10);
        int iindexStart = (int) indexStart;
        double rem = indexStart - iindexStart;

        this.initialized = true;

        for (int i = -28; i <= 28 * 2; i++)
        {
            if (i == 28 * 2 || this.levelsPos.get((int) getLevelPos(i + iindexStart)) != null)
            {
                int j = (int) getLevelPos(i + iindexStart);
                ScreenLevel l;
                while (true)
                {
                    if (this.levelsPos.get(j) != null)
                    {
                        l = this.levelsPos.get(j);
                        break;
                    }

                    j++;
                    i++;

                    j = (int) getLevelPos(j);

                    if (!allLoaded && j > this.index)
                    {
                        ScreenLevel n = this.levels.get(this.levelsLoaded);
                        Game.movables = movables;
                        Game.obstacles = obstacles;

                        initialize(n);
                        this.levelsLoaded++;

                        if (levels.indexOf(n) == levels.size() - 1)
                            allLoaded = true;
                    }
                }

                if (l == null)
                    break;

                Drawing.drawing.terrainRenderer = l.renderer;

                Game.movables = l.movables;
                Game.obstacles = l.obstacles;

                l.renderer.offX = Drawing.drawing.interfaceSizeX / 2 - (l.width / 2.0) * Game.tile_size;
                l.renderer.offY = Game.tile_size * -(i - rem);
                l.renderer.draw();

                for (Movable m: Game.movables)
                {
                    drawables[m.drawLevel].add(m);

                    if (m.showName)
                        drawables[m.nameTag.drawLevel].add(m.nameTag);
                }

                if (Game.enable3d && Game.game.window.shapeRenderer.supportsBatching)
                {
                    for (int n = 0; n < drawables.length; n++)
                    {
                        for (Obstacle o : Game.obstacles)
                        {
                            if (o.drawLevel == n && !o.batchDraw)
                            {
                                drawables[n].add(o);
                            }
                        }
                    }
                }
                else
                {
                    for (Obstacle o : Game.obstacles)
                    {
                        drawables[o.drawLevel].add(o);
                    }
                }

                translation.x = (Drawing.drawing.interfaceSizeX / 2 - (l.width / 2.0) * Game.tile_size) / Game.game.window.absoluteWidth * Drawing.drawing.interfaceScale;
                translation.y = Game.tile_size * -(i - rem) / Game.game.window.absoluteHeight * Drawing.drawing.scale;
                translation.applyAsShadow = true;
                Game.game.window.addMatrix();
                translation.apply();

                if (Game.game.window.drawingShadow)
                {
                    translation.x *= this.shadowScaleNum;
                    translation.y *= this.shadowScaleNum;
                }

                for (ArrayList<IDrawable> drawable : this.drawables)
                {
                    for (IDrawable d : drawable)
                    {
                        if (d != null)
                            d.draw();
                    }

                    if (Game.glowEnabled)
                    {
                        for (IDrawable d : drawable)
                        {
                            if (d instanceof IDrawableWithGlow && ((IDrawableWithGlow) d).isGlowEnabled())
                                ((IDrawableWithGlow) d).drawGlow();
                        }
                    }

                    drawable.clear();
                }

                translation.y = 0;
                translation.z = 0;

                Game.game.window.removeMatrix();
            }
        }

        Drawing.drawing.terrainRenderer = r;

        Game.movables = movables;
        Game.obstacles = obstacles;

        Game.game.window.transformations.remove(this.transform);
        Game.game.window.transformations.remove(this.translation);
        Game.game.window.loadPerspective();
        Game.game.window.lightBaseTransformation[0] = prevShadow;
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return this.spawns;
    }
}
