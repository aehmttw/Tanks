package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.obstacle.Obstacle;
import tanks.tank.TankSpawnMarker;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenDownloadLevel extends ScreenOnline implements ILevelPreviewScreen
{
    public String level;
    public TextBox levelName;
    public boolean downloaded = false;
    public DisplayLevel levelDisplay;

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

    public ScreenDownloadLevel(String name, String level)
    {
        this.level = level;

        this.levelDisplay = new DisplayLevel();

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
            Game.recomputeHeightGrid();
    }

    @Override
    public void draw()
    {
        this.levelDisplay.draw();

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
        return this.levelDisplay.spawns;
    }
}
