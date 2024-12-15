package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.obstacle.Obstacle;
import tanks.tank.TankSpawnMarker;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenSaveLevel extends Screen implements ILevelPreviewScreen
{
    public String level;
    public TextBox levelName;
    public boolean downloaded = false;

    public boolean fromInterlevel = false;

    public Screen screen;

    public DisplayLevel levelDisplay;

    public Button back = new Button(Drawing.drawing.interfaceSizeX - 580, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            if (!fromInterlevel)
                Game.cleanUp();
            else
                Game.silentCleanUp();

            Chunk.populateChunks(Game.currentLevel);
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

                if (fromInterlevel)
                    download.setText("Level saved!");
                else
                    download.setText("Level downloaded!");
            }
        }
    });

    public ScreenSaveLevel(String name, String level, Screen s)
    {
        super(350, 40, 380, 60);

        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.screen = s;
        this.level = level;

        this.levelDisplay = new DisplayLevel();

        Obstacle.draw_size = Game.tile_size;

        levelName = new TextBox(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 150, this.objWidth, this.objHeight, "Level save name", () ->
        {
            if (levelName.inputText.isEmpty())
                levelName.inputText = levelName.previousInputText;
            updateDownloadButton();
        }
                , name.replace("_", " "));

        levelName.enableCaps = true;

        this.updateDownloadButton();

        if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
        {
            levelName.posY += 40;
            download.posY += 40;
            back.posY += 40;
        }
    }

    @Override
    public void update()
    {
        this.download.update();
        this.back.update();

        if (!this.downloaded)
            this.levelName.update();
    }

    @Override
    public void draw()
    {
        this.levelDisplay.draw();

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
            download.setText("Pick a different name...");
            download.enabled = false;
        }
        else
        {
            if (fromInterlevel)
                download.setText("Save");
            else
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
