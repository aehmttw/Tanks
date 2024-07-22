package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.obstacle.Obstacle;
import tanks.tank.TankAIControlled;
import tanks.tank.TankSpawnMarker;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenCrusadeEditLevel extends Screen implements ILevelPreviewScreen
{
    public Crusade.CrusadeLevel level;
    public Screen previous;
    public ScreenCrusadeEditor previous2;
    public TextBox index;
    public int insertionIndex;

    public boolean edit;

    public boolean saveMenu = false;
    public boolean removeMenu = false;
    public boolean saved = false;

    public TextBox levelName;

    public ArrayList<TankSpawnMarker> spawns = new ArrayList<>();

    public Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 50, this.objWidth, this.objHeight, "Cancel", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();
            previous2.refreshLevelButtons();

            Game.screen = previous;
        }
    });

    public Button remove = new Button(200, Drawing.drawing.interfaceSizeY - 50, this.objWidth, this.objHeight, "Remove level", () -> removeMenu = true);

    public Button saveLevel = new Button(200, Drawing.drawing.interfaceSizeY - 110, this.objWidth, this.objHeight, "Save to my levels", () -> saveMenu = true);

    public Button cancelRemove = new Button(this.centerX, (int) (this.centerY + this.objYSpace), this.objWidth, this.objHeight, "No", () -> removeMenu = false
    );

    public Button confirmRemove = new Button(this.centerX, (int) (this.centerY), this.objWidth, this.objHeight, "Yes", new Runnable()
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
            previous2.refreshLevelButtons();

            Game.cleanUp();

            Game.screen = previous2;
        }
    });

    public Button next = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, this.objWidth, this.objHeight, "Next", new Runnable()
    {
        @Override
        public void run()
        {
            previous2.crusade.levels.add(insertionIndex, level);
            Game.cleanUp();

            Crusade.CrusadeLevel level = previous2.crusade.levels.remove(insertionIndex + 1);

            ScreenCrusadeEditLevel s = new ScreenCrusadeEditLevel(level, insertionIndex + 2, previous2);
            Level l = new Level(level.levelString);
            l.customTanks = level.tanks;
            l.loadLevel(s);
            Game.screen = s;
        }
    });

    public Button prev = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 110, this.objWidth, this.objHeight, "Previous", new Runnable()
    {
        @Override
        public void run()
        {
            previous2.crusade.levels.add(insertionIndex, level);
            Game.cleanUp();

            Crusade.CrusadeLevel level = previous2.crusade.levels.remove(insertionIndex - 1);

            ScreenCrusadeEditLevel s = new ScreenCrusadeEditLevel(level, insertionIndex, previous2);
            Level l = new Level(level.levelString);
            l.customTanks = level.tanks;
            l.loadLevel(s);
            Game.screen = s;
        }
    });

    public Button saveLevelConfirm = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, this.objWidth, this.objHeight, "Save level", new Runnable()
    {
        @Override
        public void run()
        {
            BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + levelName.inputText.replace(" ", "_") + ".tanks");

            boolean success = false;
            if (!file.exists())
            {
                try
                {
                    if (file.create())
                    {
                        file.startWriting();
                        String ls = level.levelString;
                        StringBuilder tanks = new StringBuilder("\ntanks\n");
                        if (previous2.crusade.customTanks.size() > 0)
                        {
                            for (TankAIControlled t: previous2.crusade.customTanks)
                                tanks.append(t.toString()).append("\n");

                            ls = ls + tanks;
                        }
                        file.println(ls);
                        file.stopWriting();
                        success = true;
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace(Game.logger);
                    e.printStackTrace();
                }
            }

            if (success)
            {
                saveLevelConfirm.enabled = false;
                saved = true;
                saveLevelConfirm.setText("Level saved!");
            }
        }
    });

    public Button cancelSave = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, this.objWidth, this.objHeight, "Back", () -> saveMenu = false
    );

    @SuppressWarnings("unchecked")
    protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

    public ScreenCrusadeEditLevel(Crusade.CrusadeLevel level, int in, ScreenCrusadeEditor s2)
    {
        this(level, s2, s2);
        this.edit = true;

        add.setText("Ok");

        insertionIndex = in - 1;
        index.inputText = in + "";

        index.posX = Drawing.drawing.interfaceSizeX / 2;
        add.posX = Drawing.drawing.interfaceSizeX / 2;

        next.enabled = insertionIndex < previous2.crusade.levels.size();
        prev.enabled = insertionIndex > 0;

        this.allowClose = false;
    }

    public ScreenCrusadeEditLevel(Crusade.CrusadeLevel level, Screen s, ScreenCrusadeEditor s2)
    {
        super(350, 40, 380, 60);

        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.level = level;

        for (int i = 0; i < drawables.length; i++)
        {
            drawables[i] = new ArrayList<>();
        }

        Obstacle.draw_size = Game.tile_size;
        this.previous = s;
        this.previous2 = s2;

        index = new TextBox(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 110, this.objWidth, this.objHeight, "Level position", () ->
        {
            if (index.inputText.length() <= 0)
                index.inputText = index.previousInputText;

            insertionIndex = Integer.parseInt(index.inputText) - 1;

            next.enabled = insertionIndex < previous2.crusade.levels.size();
            prev.enabled = insertionIndex > 0;
        }
                , "");

        levelName = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, this.objWidth, this.objHeight, "Level save name", () ->
        {
            if (levelName.inputText.equals(""))
                levelName.inputText = levelName.previousInputText;
            updateSaveButton();
        }
                , level.levelName.replace("_", " "));

        levelName.enableCaps = true;

        this.updateSaveButton();

        index.allowLetters = false;
        index.allowSpaces = false;
        index.minValue = 1;
        index.maxValue = s2.crusade.levels.size() + 1;
        index.checkMaxValue = true;
        index.checkMinValue = true;
        insertionIndex = (int) (index.maxValue - 1);
        index.inputText = (int) index.maxValue + "";
        index.maxChars = 9;

        this.next.image = "icons/arrow_down.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.prev.image = "icons/arrow_up.png";
        this.prev.imageSizeX = 25;
        this.prev.imageSizeY = 25;
        this.prev.imageXOffset = 145;
    }

    public void updateSaveButton()
    {
        BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + levelName.inputText.replace(" ", "_") + ".tanks");

        if (file.exists())
        {
            saveLevelConfirm.setText("Pick a different name...");
            saveLevelConfirm.enabled = false;
        }
        else
        {
            saveLevelConfirm.setText("Save level");
            saveLevelConfirm.enabled = true;
        }
    }

    @Override
    public void update()
    {
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

        if (removeMenu)
        {
            if (Game.game.input.editorPause.isValid())
            {
                Game.game.input.editorPause.invalidate();
                removeMenu = false;
            }

            confirmRemove.update();
            cancelRemove.update();
        }
        else if (saveMenu)
        {
            if (Game.game.input.editorPause.isValid())
            {
                Game.game.input.editorPause.invalidate();
                saveMenu = false;
            }

            saveLevelConfirm.update();
            levelName.update();
            cancelSave.update();
        }
        else
        {
            this.index.update();
            this.add.update();

            if (edit)
            {
                this.saveLevel.update();
                this.remove.update();
                this.next.update();
                this.prev.update();
            }
            else
                this.back.update();

            if (Game.game.input.editorPause.isValid())
            {
                if (this.edit)
                    add.function.run();
                else
                    back.function.run();

                Game.game.input.editorPause.invalidate();
            }
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

        if (removeMenu)
        {
            Drawing.drawing.setColor(127, 178, 228, 64);
            Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "Are you sure you want to remove this level?");

            confirmRemove.draw();
            cancelRemove.draw();
        }
        else if (saveMenu)
        {
            Drawing.drawing.setColor(127, 178, 228, 64);
            Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.setInterfaceFontSize(this.titleSize);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Save level to my levels");

            saveLevelConfirm.draw();
            levelName.draw();
            cancelSave.draw();
        }
        else
        {
            double extraWidth = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2;
            double height = (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale;

            Drawing.drawing.setColor(0, 0, 0, 127);
            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX + extraWidth / 2, Drawing.drawing.interfaceSizeY / 2, extraWidth, height);
            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY / 2, 400, height);

            for (int i = 0; i < 7; i++)
            {
                if (insertionIndex - i - 1 < 0)
                    break;

                Drawing.drawing.setColor(255, 255, 255, 200 - i * 30);
                Drawing.drawing.setInterfaceFontSize(this.textSize);
                Drawing.drawing.drawInterfaceText(prev.posX, Drawing.drawing.interfaceSizeY / 2 - (i + 1) * 40 - 60, (insertionIndex - i) + ". " + previous2.crusade.levels.get(insertionIndex - i - 1).levelName.replace("_", " "));
            }

            for (int i = 0; i < 7; i++)
            {
                if (insertionIndex + i >= previous2.crusade.levels.size())
                    break;

                Drawing.drawing.setColor(255, 255, 255, 200 - i * 30);
                Drawing.drawing.setInterfaceFontSize(this.textSize);
                Drawing.drawing.drawInterfaceText(prev.posX, Drawing.drawing.interfaceSizeY / 2 + (i + 1) * 40 - 60, (insertionIndex + i + 2) + ". " + previous2.crusade.levels.get(insertionIndex + i).levelName.replace("_", " "));
            }

            Drawing.drawing.setColor(0, 0, 0, 127);
            Drawing.drawing.fillInterfaceRect(prev.posX, Drawing.drawing.interfaceSizeY / 2 - 60, 380, 40);

            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.drawInterfaceText(prev.posX, Drawing.drawing.interfaceSizeY / 2 - 60, (insertionIndex + 1) + ". " + level.levelName.replace("_", " "));

            this.index.draw();
            this.add.draw();

            if (edit)
            {
                this.saveLevel.draw();
                this.remove.draw();
                this.next.draw();
                this.prev.draw();
            }
            else
                this.back.draw();
        }
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return this.spawns;
    }

    @Override
    public void onAttemptClose()
    {
        Game.screen = new ScreenConfirmSaveCrusade(Game.screen, this.previous2);
    }
}
