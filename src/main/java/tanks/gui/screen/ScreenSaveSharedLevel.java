package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.gui.*;
import tanks.obstacle.Obstacle;
import tanks.tank.TankSpawnMarker;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenSaveSharedLevel extends Screen implements ILevelPreviewScreen
{
    public String level;
    public TextBox levelName;
    public boolean downloaded = false;

    public Screen screen;

    public ArrayList<TankSpawnMarker> spawns = new ArrayList<TankSpawnMarker>();

    public Button back = new Button(Drawing.drawing.interfaceSizeX - 580, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();

            Game.screen = screen;
        }
    });

    public Button download = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Download", new Runnable()
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
                        file.println(level);
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
                download.enabled = false;
                downloaded = true;
                download.text = "Level downloaded!";
            }
        }
    });

    @SuppressWarnings("unchecked")
    protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

    public ScreenSaveSharedLevel(String name, String level, Screen s)
    {
        super(350, 40, 380, 60);

        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.screen = s;
        this.level = level;

        for (int i = 0; i < drawables.length; i++)
        {
            drawables[i] = new ArrayList<IDrawable>();
        }

        Obstacle.draw_size = Game.tile_size;

        levelName = new TextBox(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 150, this.objWidth, this.objHeight, "Level save name", new Runnable()
        {
            @Override
            public void run()
            {
                if (levelName.inputText.equals(""))
                    levelName.inputText = levelName.previousInputText;
                updateDownloadButton();
            }

        }
                , name.replace("_", " "));

        levelName.enableCaps = true;

        this.updateDownloadButton();
    }

    @Override
    public void update()
    {
        this.download.update();
        this.back.update();

        if (!this.downloaded)
            this.levelName.update();

        if (Game.enable3d)
            for (int i = 0; i < Game.obstacles.size(); i++)
            {
                Obstacle o = Game.obstacles.get(i);

                if (o.replaceTiles)
                    o.postOverride();
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

        this.download.draw();
        this.back.draw();

        if (!this.downloaded)
            this.levelName.draw();
    }

    public void updateDownloadButton()
    {
        BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + levelName.inputText.replace(" ", "_") + ".tanks");

        if (file.exists())
        {
            download.text = "Pick a different name...";
            download.enabled = false;
        }
        else
        {
            download.text = "Download";
            download.enabled = true;
        }
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return this.spawns;
    }
}
