package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.obstacle.Obstacle;
import tanks.tank.TankSpawnMarker;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenDownloadLevel extends ScreenOnline implements ILevelPreviewScreen
{
    public String level;
    public TextBox levelName;
    public boolean downloaded = false;

    public ArrayList<TankSpawnMarker> spawns = new ArrayList<>();

    public Button download = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, this.objWidth, this.objHeight, "Download", new Runnable()
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
                download.setText("Level downloaded!");
            }
        }
    });

    @SuppressWarnings("unchecked")
    protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

    public ScreenDownloadLevel(String name, String level)
    {
        this.level = level;

        for (int i = 0; i < drawables.length; i++)
        {
            drawables[i] = new ArrayList<>();
        }

        Obstacle.draw_size = Game.tile_size;

        levelName = new TextBox(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 110, this.objWidth, this.objHeight, "Level save name", () ->
        {
            if (levelName.inputText.equals(""))
                levelName.inputText = levelName.previousInputText;
            updateDownloadButton();
        }
                , name.replace("_", " "));

        levelName.enableCaps = true;

        this.updateDownloadButton();
    }

    @Override
    public void update()
    {
        super.update();

        this.download.update();

        if (!this.downloaded)
            this.levelName.update();

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

        for (int i : this.shapes.keySet())
            this.shapes.get(i).draw();

        Drawing.drawing.setColor(0, 0, 0);

        for (int i : this.texts.keySet())
            this.texts.get(i).draw();

        for (int i : this.buttons.keySet())
            this.buttons.get(i).draw();

        for (int i : this.textboxes.keySet())
            this.textboxes.get(i).draw();

        this.download.draw();

        if (!this.downloaded)
            this.levelName.draw();
    }

    public void updateDownloadButton()
    {
        BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + levelName.inputText.replace(" ", "_") + ".tanks");

        if (file.exists())
        {
            download.setText("Pick a different name...");
            download.enabled = false;
        }
        else
        {
            download.setText("Download");
            download.enabled = true;
        }
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return this.spawns;
    }
}
