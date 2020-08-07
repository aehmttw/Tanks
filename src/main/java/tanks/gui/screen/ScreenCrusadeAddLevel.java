package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenCrusadeAddLevel extends ScreenPlaySavedLevels
{
    public ScreenCrusadeBuilder previous;
    public ScreenCrusadeAddLevel instance;

    public ScreenCrusadeAddLevel(ScreenCrusadeBuilder s)
    {
        this.music = "tomato_feast_4.ogg";
        this.musicID = "menu";

        this.instance = this;
        this.previous = s;

        this.title = "Select a level to add";

        this.quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
        {
            @Override
            public void run()
            {
                Game.screen = previous;
            }
        }
        );
    }

    @Override
    public void addButtons()
    {
        BaseFile savedLevelsFile = Game.game.fileManager.getFile(Game.homedir + levelDir);
        if (!savedLevelsFile.exists())
        {
            savedLevelsFile.mkdirs();
        }

        ArrayList<String> levels = new ArrayList<String>();

        try
        {
            ArrayList<String> ds = savedLevelsFile.getSubfiles();

            for (String p : ds)
            {
                if (p.endsWith(".tanks"))
                    levels.add(p);
            }
        }
        catch (IOException e)
        {
            Game.exitToCrash(e);
        }

        for (String l: levels)
        {
            String[] pathSections = l.replace("\\", "/").split("/");

            buttons.add(new Button(0, 0, 350, 40, pathSections[pathSections.length - 1].split("\\.")[0].replace("_", " "), new Runnable()
            {
                @Override
                public void run()
                {
                    ScreenCrusadeEditLevel s = new ScreenCrusadeEditLevel(pathSections[pathSections.length - 1].split("\\.")[0], null, instance, previous);
                    if (Game.loadLevel(Game.game.fileManager.getFile(l), s))
                    {
                        s.level = Game.currentLevel.levelString;
                        Game.screen = s;
                    }
                }
            }
            ));
        }
    }
}
