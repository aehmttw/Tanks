package tanks.gui;

import basewindow.BaseFile;
import tanks.BiConsumer;
import tanks.Function;
import tanks.Game;

import java.io.IOException;
import java.util.ArrayList;


public class SavedFilesList extends ButtonList
{
    public SavedFilesList(String dir, int page, int xOffset, int yOffset, BiConsumer<String, BaseFile> behavior, Function<BaseFile, String> hover)
    {
        super(new ArrayList<>(), page, xOffset, yOffset);

        BaseFile directory = Game.game.fileManager.getFile(dir);
        if (!directory.exists())
        {
            directory.mkdirs();
        }

        ArrayList<String> levels = new ArrayList<String>();

        try
        {
            ArrayList<String> ds = directory.getSubfiles();

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

            String name = pathSections[pathSections.length - 1].split("\\.")[0];
            BaseFile file = Game.game.fileManager.getFile(l);

            this.buttons.add(new Button(0, 0, this.objWidth, this.objHeight, name.replace("_", " "), new Runnable()
            {
                @Override
                public void run()
                {
                    behavior.accept(name, file);
                }
            }
                    , hover.apply(file)));
        }

        this.sortButtons();
    }
}
