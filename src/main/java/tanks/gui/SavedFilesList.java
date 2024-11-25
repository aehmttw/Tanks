package tanks.gui;

import basewindow.BaseFile;
import tanks.BiConsumer;
import tanks.Drawing;
import tanks.Function;
import tanks.Game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class SavedFilesList extends ButtonList
{
    public String directory;
    public boolean sortedByTime = false;
    public BiConsumer<BaseFile, Button> auxiliarySetup = null;
    HashMap<Button, Long> times = new HashMap<>();
    public ArrayList<Button> fileButtons = new ArrayList<>();
    public boolean drawOpenFileButton = Game.framework == Game.Framework.lwjgl;

    Button openFolder = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2 * 1.35, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 4, this.objHeight, this.objHeight, "", () ->
    {
        Game.game.fileManager.openFileManager(this.directory);
    }, "Open folder in file manager");

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
        this.directory = dir;

        this.openFolder.image = "icons/folder.png";
        this.openFolder.fullInfo = true;
        this.openFolder.imageSizeX = 30;
        this.openFolder.imageSizeY = 30;

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

            Button b = new Button(0, 0, this.objWidth, this.objHeight, name.replace("_", " "), () -> behavior.accept(name, file), hover.apply(file));

            times.put(b, file.lastModified());

            if (this.auxiliarySetup != null)
                this.auxiliarySetup.accept(file, b);

            this.buttons.add(b);
            this.fileButtons.add(b);
        }

        for (int i = 0; i < this.buttons.size(); i++)
        {
            if (this.buttons.get(i).text == null)
            {
                this.buttons.remove(i);
                i--;
            }
        }

        //this.buttons.sort(Comparator.comparing(o -> o.text) /*(int) Math.signum(times.get(o2) - times.get(o1))*/);
        Collections.sort(buttons, (o1, o2) -> o1.text.compareTo(o2.text));

        this.sortButtons();
    }

    protected SavedFilesList()
    {
        this.openFolder.image = "icons/folder.png";
        this.openFolder.fullInfo = true;
        this.openFolder.imageSizeX = 30;
        this.openFolder.imageSizeY = 30;
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
        s.directory = this.directory;

        return s;
    }

    public void sort(boolean byTime)
    {
        this.buttons.removeAll(this.fileButtons);

        // IMPORTANT: there's a nicer way to do this but libgdx doesnt support it
        if (byTime)
            Collections.sort(this.fileButtons, (o1, o2) -> (int) Math.signum(times.get(o2) - times.get(o1)));
        else
            Collections.sort(this.fileButtons, (o1, o2) -> o1.text.toLowerCase().compareTo(o2.text.toLowerCase()));

        this.buttons.addAll(this.fileButtons);
    }

    @Override
    public void update()
    {
        if (this.drawOpenFileButton)
            this.openFolder.update();

        super.update();
    }

    @Override
    public void draw()
    {
        if (this.drawOpenFileButton)
            this.openFolder.draw();

        super.draw();
    }
}
