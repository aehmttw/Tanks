package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.obstacle.Obstacle;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public class ScreenCrusadePreviewLevel extends Screen implements ILevelPreviewScreen
{
    public String level;
    public Screen previous;
    public Crusade crusade;

    public int index;

    public ArrayList<TankSpawnMarker> spawns = new ArrayList<>();

    public Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();
            Game.screen = previous;
        }
    });

    public Button next = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Next", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();

            String level = crusade.levels.get(index + 1);

            ScreenCrusadePreviewLevel s = new ScreenCrusadePreviewLevel(crusade, level, index + 1, previous);
            new Level(level).loadLevel(s);
            Game.screen = s;
        }
    });

    public Button prev = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 150, this.objWidth, this.objHeight, "Previous", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();

            String level = crusade.levels.get(index - 1);

            ScreenCrusadePreviewLevel s = new ScreenCrusadePreviewLevel(crusade, level, index - 1, previous);
            new Level(level).loadLevel(s);
            Game.screen = s;
        }
    });

    @SuppressWarnings("unchecked")
    protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

    public ScreenCrusadePreviewLevel(Crusade crusade, String level, int index, Screen s)
    {
        super(350, 40, 380, 60);

        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.index = index;
        this.crusade = crusade;
        this.level = level;

        for (int i = 0; i < drawables.length; i++)
        {
            drawables[i] = new ArrayList<>();
        }

        Obstacle.draw_size = Game.tile_size;
        this.previous = s;

        next.enabled = index < crusade.levels.size() - 1;
        prev.enabled = index > 0;

        this.next.image = "icons/arrow_down.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.prev.image = "icons/arrow_up.png";
        this.prev.imageSizeX = 25;
        this.prev.imageSizeY = 25;
        this.prev.imageXOffset = 145;
    }

    @Override
    public void update()
    {
        this.back.update();
        this.next.update();
        this.prev.update();

        if (Game.enable3d)
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

        if (Game.game.input.editorPause.isValid())
        {
            back.function.run();
            Game.game.input.editorPause.invalidate();
        }
    }

    public void drawLevel()
    {
        for (Effect e: Game.tracks)
            drawables[0].add(e);

        for (Movable m: Game.movables)
            drawables[m.drawLevel].add(m);

        for (Obstacle o: Game.obstacles)
            drawables[o.drawLevel].add(o);

        for (Effect e: Game.effects)
            drawables[7].add(e);

        for (int i = 0; i < this.drawables.length; i++)
        {
            if (i == 5 && Game.enable3d)
            {
                Drawing drawing = Drawing.drawing;
                Drawing.drawing.setColor(174, 92, 16);
                Drawing.drawing.fillForcedBox(drawing.sizeX / 2, -Game.tile_size / 2, 0, drawing.sizeX + Game.tile_size * 2, Game.tile_size, Obstacle.draw_size, (byte) 0);
                Drawing.drawing.fillForcedBox(drawing.sizeX / 2, Drawing.drawing.sizeY + Game.tile_size / 2, 0, drawing.sizeX + Game.tile_size * 2, Game.tile_size, Obstacle.draw_size, (byte) 0);
                Drawing.drawing.fillForcedBox(-Game.tile_size / 2, drawing.sizeY / 2, 0, Game.tile_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
                Drawing.drawing.fillForcedBox(drawing.sizeX + Game.tile_size / 2, drawing.sizeY / 2, 0, Game.tile_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
            }

            for (IDrawable d: this.drawables[i])
            {
                d.draw();

                if (d instanceof Movable)
                    ((Movable) d).drawTeam();
            }

            if (Game.glowEnabled)
            {
                for (IDrawable d : this.drawables[i])
                {
                    if (d instanceof IDrawableWithGlow && ((IDrawableWithGlow) d).isGlowEnabled())
                        ((IDrawableWithGlow) d).drawGlow();
                }
            }

            drawables[i].clear();
        }
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        this.drawLevel();

        double extraWidth = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2;
        double height = (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale;

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX + extraWidth / 2, Drawing.drawing.interfaceSizeY / 2, extraWidth, height);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY / 2,
                400, height);

        double posX = Drawing.drawing.interfaceSizeX - 200;

        for (int i = 0; i < 7; i++)
        {
            if (index - i - 1 < 0)
                break;

            Drawing.drawing.setColor(255, 255, 255, 200 - i * 30);
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(posX, Drawing.drawing.interfaceSizeY / 2 - (i + 1) * 40 - 60, (index - i) + ". " + crusade.levelNames.get(index - i - 1).replace("_", " "));
        }

        for (int i = 0; i < 7; i++)
        {
            if (index + i + 1 >= crusade.levelNames.size())
                break;

            Drawing.drawing.setColor(255, 255, 255, 200 - i * 30);
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(posX, Drawing.drawing.interfaceSizeY / 2 + (i + 1) * 40 - 60, (index + i + 2) + ". " + crusade.levelNames.get(index + i + 1).replace("_", " "));
        }

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(posX, Drawing.drawing.interfaceSizeY / 2 - 60, 380, 40);

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.drawInterfaceText(posX, Drawing.drawing.interfaceSizeY / 2 - 60, (index + 1) + ". " + crusade.levelNames.get(index).replace("_", " "));

        this.back.draw();
        this.next.draw();
        this.prev.draw();
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return this.spawns;
    }
}
