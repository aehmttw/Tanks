package tanks.gui;

import basewindow.BaseFile;
import tanks.BiConsumer;
import tanks.Drawing;
import tanks.Function;
import tanks.Game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;


public class SavedFilesList extends ButtonList
{
    public boolean sortedByTime = false;
    public BiConsumer<BaseFile, Button> auxiliarySetup = null;
    HashMap<Button, Long> times = new HashMap<>();
    public ArrayList<Button> fileButtons = new ArrayList<>();

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

        ArrayList<String> files = new ArrayList<>();

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

            Button b = new Button(0, 0, this.objWidth, this.objHeight, name.replace("_", " "), () -> behavior.accept(name, file)
                    , hover.apply(file));

            times.put(b, file.lastModified());

            if (this.auxiliarySetup != null)
                this.auxiliarySetup.accept(file, b);

            this.buttons.add(b);
            this.fileButtons.add(b);
        }

        this.buttons.sort(Comparator.comparing(o -> o.text) /*(int) Math.signum(times.get(o2) - times.get(o1))*/);

        this.sortButtons();
    }

    protected SavedFilesList()
    {

    }

    public SavedFilesList clone()
    {
        SavedFilesList s = new SavedFilesList();
        s.page = this.page;
        s.xOffset = this.xOffset;
        s.yOffset = this.yOffset;
        s.auxiliarySetup = this.auxiliarySetup;
        s.buttons = new ArrayList<>();
        s.buttons.addAll(this.buttons);

        return s;
    }

    public void sort(boolean byTime)
    {
        this.buttons.removeAll(this.fileButtons);

        if (byTime)
            this.fileButtons.sort((o1, o2) -> (int) Math.signum(times.get(o2) - times.get(o1)));
        else
            this.fileButtons.sort(Comparator.comparing(o -> o.text));

        this.buttons.addAll(this.fileButtons);
    }
}
