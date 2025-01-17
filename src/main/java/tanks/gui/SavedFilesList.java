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
    public BiConsumer<String, BaseFile> behavior;
    public Function<BaseFile, String> hover;
    public String extension;
    public BaseFile directoryFile;

    public String directory;
    public boolean sortedByTime = false;
    public BiConsumer<BaseFile, Button> auxiliarySetup = null;
    HashMap<Button, Long> times = new HashMap<>();
    public ArrayList<Button> fileButtons = new ArrayList<>();
    public boolean drawOpenFileButton = false;

    Button openFolder = new Button(-1000, -1000, this.objHeight, this.objHeight, "", () ->
            Game.game.fileManager.openFileManager(this.directory), "Open folder in file manager");

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

        this.behavior = behavior;
        this.hover = hover;
        this.extension = ext;
        this.directoryFile = Game.game.fileManager.getFile(dir);

        if (!directoryFile.exists())
            directoryFile.mkdirs();

        refresh();
    }

    public void refresh()
    {
        buttons.clear();
        fileButtons.clear();

        ArrayList<String> files = new ArrayList<>();

        try
        {
            ArrayList<String> ds = directoryFile.getSubfiles();

            for (String p : ds)
            {
                if (p.endsWith(extension))
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
        sort(sortedByTime);
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
        s.drawOpenFileButton = this.drawOpenFileButton;

        return s;
    }

    public void sort(boolean byTime)
    {
        this.fileButtons.removeIf(b -> b.text == null);
        this.buttons.removeAll(this.fileButtons);

        // IMPORTANT: there's a nicer way to do this but libgdx doesnt support it
        if (byTime)
            this.fileButtons.sort((o1, o2) -> (int) Math.signum(times.get(o2) - times.get(o1)));
        else
            this.fileButtons.sort(Comparator.comparing(o -> o.text.toLowerCase()));

        this.buttons.addAll(this.fileButtons);
    }

    @Override
    public void update()
    {
        this.openFolder.posX = Drawing.drawing.interfaceSizeX / 2 + this.xOffset + this.objXSpace / 2 * 1.35;
        this.openFolder.posY = Drawing.drawing.interfaceSizeY / 2 + this.yOffset - this.objYSpace * 3.5;

        if (this.drawOpenFileButton)
            this.openFolder.update();

        super.update();
    }

    @Override
    public void draw()
    {
        this.openFolder.posX = Drawing.drawing.interfaceSizeX / 2 + this.xOffset + this.objXSpace / 2 * 1.35;
        this.openFolder.posY = Drawing.drawing.interfaceSizeY / 2 + this.yOffset - this.objYSpace * 3.5;

        super.draw();

        if (this.drawOpenFileButton)
            this.openFolder.draw();
    }
}
