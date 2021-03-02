package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.obstacle.Obstacle;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public class ScreenCrusadeEditLevel extends Screen implements ILevelPreviewScreen
{
    public String name;
    public String level;
    public Screen previous;
    public ScreenCrusadeBuilder previous2;
    public TextBox index;
    public int insertionIndex;
    public int originalInsertionIndex;

    public boolean edit;

    public ArrayList<TankSpawnMarker> spawns = new ArrayList<TankSpawnMarker>();

    public Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 50, this.objWidth, this.objHeight, "Cancel", new Runnable()
    {
        @Override
        public void run()
        {
            if (edit)
            {
                previous2.crusade.levels.add(originalInsertionIndex, level);
                previous2.crusade.levelNames.add(originalInsertionIndex, name);
                previous2.refreshLevelButtons();
            }

            Game.cleanUp();
            previous2.refreshLevelButtons();

            Game.screen = previous;
        }
    });

    public Button remove = new Button(200, Drawing.drawing.interfaceSizeY - 50, this.objWidth, this.objHeight, "Remove level", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();
            previous2.refreshLevelButtons();

            Game.screen = previous;
        }
    });

    public Button add = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, this.objWidth, this.objHeight, "Add level", new Runnable()
    {
        @Override
        public void run()
        {
            previous2.crusade.levels.add(insertionIndex, level);
            previous2.crusade.levelNames.add(insertionIndex, name);
            previous2.refreshLevelButtons();

            Game.cleanUp();

            Game.screen = previous2;
        }
    });

    @SuppressWarnings("unchecked")
    protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

    public ScreenCrusadeEditLevel(String name, String level, int in, ScreenCrusadeBuilder s2)
    {
        this(name, level, s2, s2);
        this.edit = true;

        add.text = "Ok";

        insertionIndex = in - 1;
        originalInsertionIndex = insertionIndex;
        index.inputText = in + "";
    }

    public ScreenCrusadeEditLevel(String name, String level, Screen s, ScreenCrusadeBuilder s2)
    {
        super(350, 40, 380, 60);

        this.music = "tomato_feast_4.ogg";
        this.musicID = "menu";

        this.name = name;
        this.level = level;

        for (int i = 0; i < drawables.length; i++)
        {
            drawables[i] = new ArrayList<IDrawable>();
        }

        Obstacle.draw_size = Game.tile_size;
        this.previous = s;
        this.previous2 = s2;

        index = new TextBox(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 110, this.objWidth, this.objHeight, "Level position", new Runnable()
        {
            @Override
            public void run()
            {
                if (index.inputText.length() <= 0)
                    index.inputText = index.previousInputText;

                insertionIndex = Integer.parseInt(index.inputText) - 1;

            }

        }
                , "");

        index.allowLetters = false;
        index.allowSpaces = false;
        index.minValue = 1;
        index.maxValue = s2.crusade.levels.size() + 1;
        index.checkMaxValue = true;
        index.checkMinValue = true;
        insertionIndex = (int) (index.maxValue - 1);
        index.inputText = (int) index.maxValue + "";
        index.maxChars = 9;
    }

    @Override
    public void update()
    {
        this.index.update();
        this.back.update();
        this.add.update();

        if (edit)
            this.remove.update();

        if (Game.enable3d)
            for (int i = 0; i < Game.obstacles.size(); i++)
            {
                Obstacle o = Game.obstacles.get(i);

                if (o.replaceTiles)
                    o.postOverride();
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

        for (int i = 0; i < 7; i++)
        {
            if (insertionIndex - i - 1 < 0)
                break;

            Drawing.drawing.setColor(255, 255, 255, 200 - i * 30);
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(index.posX, Drawing.drawing.interfaceSizeY / 2 - (i + 1) * 40 - 60, (insertionIndex - i) + ". " + previous2.crusade.levelNames.get(insertionIndex - i - 1).replace("_", " "));
        }

        for (int i = 0; i < 7; i++)
        {
            if (insertionIndex + i >= previous2.crusade.levelNames.size())
                break;

            Drawing.drawing.setColor(255, 255, 255, 200 - i * 30);
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(index.posX, Drawing.drawing.interfaceSizeY / 2 + (i + 1) * 40 - 60, (insertionIndex + i + 2) + ". " + previous2.crusade.levelNames.get(insertionIndex + i).replace("_", " "));
        }

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(index.posX, Drawing.drawing.interfaceSizeY / 2 - 60, 380, 40);

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.drawInterfaceText(index.posX, Drawing.drawing.interfaceSizeY / 2 - 60, (insertionIndex + 1) + ". " + name.replace("_", " "));

        this.index.draw();
        this.back.draw();
        this.add.draw();

        if (edit)
            this.remove.draw();
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return this.spawns;
    }
}
