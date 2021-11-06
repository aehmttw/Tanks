package tanks.gui;

import basewindow.BaseFile;
import tanks.BiConsumer;
import tanks.Function;
import tanks.Game;

import java.io.IOException;
import java.util.ArrayList;


public class SavedFilesList extends ButtonList
{
    public BiConsumer<BaseFile, Button> auxiliarySetup = null;

    public SavedFilesList(String dir, int page, int xOffset, int yOffset, BiConsumer<String, BaseFile> behavior, Function<BaseFile, String> hover)
    {
        this(dir, page, xOffset, yOffset, behavior, hover, null, ".tanks");
    }

    public SavedFilesList(String dir, int page, int xOffset, int yOffset, BiConsumer<String, BaseFile> behavior, Function<BaseFile, String> hover, String ext)
    {
        this(dir, page, xOffset, yOffset, behavior, hover, null, ext);
    }

    public SavedFilesList(String dir, int page, int xOffset, int yOffset, BiConsumer<String, BaseFile> behavior, Function<BaseFile, String> hover, BiConsumer<BaseFile, Button> auxiliarySetup)
    {
        this(dir, page, xOffset, yOffset, behavior, hover, auxiliarySetup, ".tanks");
    }

    public SavedFilesList(String dir, int page, int xOffset, int yOffset, BiConsumer<String, BaseFile> behavior, Function<BaseFile, String> hover, BiConsumer<BaseFile, Button> auxiliarySetup, String ext)
    {
        super(new ArrayList<>(), page, xOffset, yOffset);

        this.auxiliarySetup = auxiliarySetup;

        BaseFile directory = Game.game.fileManager.getFile(dir);
        if (!directory.exists())
        {
            directory.mkdirs();
        }

        ArrayList<String> files = new ArrayList<String>();

        try
        {
            ArrayList<String> ds = directory.getSubfiles();

            for (String p : ds)
            {
                if (p.endsWith(ext))
                    files.add(p);
            }
        }
        catch (IOException e)
        {
            Game.exitToCrash(e);
        }

        for (String l: files)
        {
            String[] pathSections = l.replace("\\", "/").split("/");

            String name = pathSections[pathSections.length - 1].split("\\.")[0];
            BaseFile file = Game.game.fileManager.getFile(l);

            Button b = new Button(0, 0, this.objWidth, this.objHeight, name.replace("_", " "), new Runnable()
            {
                @Override
                public void run()
                {
                    behavior.accept(name, file);
                }
            }
                    , hover.apply(file));

            if (this.auxiliarySetup != null)
                this.auxiliarySetup.accept(file, b);

            this.buttons.add(b);
        }

        this.sortButtons();
    }
}
