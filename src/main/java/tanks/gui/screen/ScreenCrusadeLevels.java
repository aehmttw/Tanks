package tanks.gui.screen;

import basewindow.transformation.*;
import tanks.*;
import tanks.obstacle.Obstacle;
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
        public Level level;
        public Drawing.LevelRenderer renderer = new Drawing.LevelRenderer();

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
    protected double age = 0;
    protected boolean initialized = false;
    protected Crusade crusade;

    protected int index = 0;

    protected static ScreenCrusadeLevels currentScreen;

    public ScreenCrusadeLevels(Crusade c)
    {
        if (currentScreen != null)
        {
            for (ScreenLevel l: currentScreen.levels)
            {
                l.renderer.free();
            }
        }

        currentScreen = this;

        ArrayList<Movable> movables = Game.movables;
        ArrayList<Obstacle> obstacles = Game.obstacles;

        for (int i = 0; i < drawables.length; i++)
        {
            drawables[i] = new ArrayList<>();
        }

        Game.movables = movables;
        Game.obstacles = obstacles;
        this.crusade = c;
        this.selfBatch = false;
        this.forceInBounds = true;
        this.minBgWidth = 100;
    }

    public void initialize(ScreenLevel l)
    {
        l.level.loadLevel(this);
        l.startIndex = index;
        index += l.level.sizeY;
        l.width = l.level.sizeX;
        l.endIndex = index;
        this.levels.add(l);
        this.levelsPos.put(index - 1, l);

        Drawing.drawing.setRenderer(l.renderer);
        Game.movables = l.movables;
        Game.obstacles = l.obstacles;

        Drawing.drawing.setRenderer(l.renderer);

        Drawing.drawing.beginTerrainRenderers();

        for (int i = 0; i < Game.game.heightGrid.length; i++)
        {
            Arrays.fill(Game.game.heightGrid[i], -1000);
            Arrays.fill(Game.game.groundHeightGrid[i], -1000);
        }

        if (Game.enable3d)
            for (Obstacle o: Game.obstacles)
            {
                if (o.replaceTiles)
                    o.postOverride();

                int x = (int) (o.posX / Game.tile_size);
                int y = (int) (o.posY / Game.tile_size);

                if (!(!Game.fancyTerrain || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY))
                {
                    Game.game.heightGrid[x][y] = Math.max(o.getTileHeight(), Game.game.heightGrid[x][y]);
                    Game.game.groundHeightGrid[x][y] = Math.max(o.getGroundHeight(), Game.game.groundHeightGrid[x][y]);
                }
            }

        this.drawDefaultBackground();

        if (Game.enable3d && (Obstacle.draw_size <= 0 || Obstacle.draw_size >= Game.tile_size) && Game.game.window.shapeRenderer.supportsBatching)
        {
            for (int i = 0; i < drawables.length; i++)
            {
                for (Obstacle o : Game.obstacles)
                {
                    if (o.drawLevel == i && o.batchDraw)
                        o.draw();
                }
            }
        }

        Drawing.drawing.stageRenderers();
    }

    @Override
    public void update()
    {

    }

    public void addTransitionLevels(ScreenLevel l)
    {
        int fade = 8;

        for (int i = 1; i < fade; i++)
        {
            ScreenLevel l2 = new ScreenLevel();
            double frac = i * 1.0 / fade;
            int r = (int) (Level.currentColorR * (1 - frac) + l.level.colorR * frac);
            int g = (int) (Level.currentColorG * (1 - frac) + l.level.colorG * frac);
            int b = (int) (Level.currentColorB * (1 - frac) + l.level.colorB * frac);
            int dr = (int) (Level.currentColorVarR * (1 - frac) + l.level.colorVarR * frac);
            int dg = (int) (Level.currentColorVarG * (1 - frac) + l.level.colorVarG * frac);
            int db = (int) (Level.currentColorVarB * (1 - frac) + l.level.colorVarB * frac);
            l2.level = new Level("{28,1," + r + "," + g + "," + b + "," + dr + "," + dg + "," + db + "||10000-0-player}");
            Game.movables = l2.movables;
            Game.obstacles = l2.obstacles;
            this.initialize(l2);
        }
    }

    public void draw()
    {
        Transformation prevShadow = Game.game.window.lightBaseTransformation[0];
        Game.game.window.lightBaseTransformation[0] = this.shadowScale;

        //Game.game.window.transformations.add(this.translation);

        if (Game.enable3d)
        {
            Game.game.window.transformations.add(this.transform);
        }

        Game.game.window.loadPerspective();

        if (Game.game.window.drawingShadow)
            this.age += Panel.frameFrequency;

        ArrayList<Movable> movables = Game.movables;
        ArrayList<Obstacle> obstacles = Game.obstacles;

        if (!initialized)
        {
            for (String s: this.crusade.levels)
            {
                ScreenLevel l = new ScreenLevel();
                l.level = new Level(s);

                this.addTransitionLevels(l);

                Game.movables = l.movables;
                Game.obstacles = l.obstacles;

                this.initialize(l);
            }

            ScreenLevel l = new ScreenLevel();
            l.level = new Level("{28,36||10000-0-player}");

            this.addTransitionLevels(l);
            this.initialize(l);

            Game.cleanUp();
        }

        double indexStart = (this.age / 10) % this.index;
        int iindexStart = (int) indexStart;
        double rem = indexStart - iindexStart;

        this.initialized = true;

        for (int i = -28; i <= 28 * 2; i++)
        {
            if (i == 28 * 2 || this.levelsPos.get((i + iindexStart + index * 10) % index) != null)
            {
                int j = (i + iindexStart + index * 10) % index;
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

                    j = (j + index * 10) % index;
                }

                if (l == null)
                    break;

                Drawing.drawing.setRenderer(l.renderer);

                Game.movables = l.movables;
                Game.obstacles = l.obstacles;

                Drawing.drawing.drawTerrainRenderers(false, Drawing.drawing.interfaceSizeX / 2 - (l.width / 2.0) * Game.tile_size, Game.tile_size * -(i - rem), 0, 1);

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

        Drawing.drawing.setRenderer(Drawing.drawing.defaultRenderer);

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
