package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenUploadLevel extends ScreenOnline
{
    public static final String levelDir = Game.directoryPath + "/levels";

    int rows = 6;
    int yoffset = -150;
    static int page = 0;

    Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            page++;
        }
    }
    );

    Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            page--;
        }
    }
    );

    ArrayList<Button> buttons = new ArrayList<Button>();

    public ScreenUploadLevel()
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
                    ScreenPreviewUploadLevel sc = new ScreenPreviewUploadLevel(pathSections[pathSections.length - 1].split("\\.")[0], (ScreenUploadLevel) Game.screen);
                    if (Game.loadLevel(Game.game.fileManager.getFile(l), sc))
                    {
                        sc.level = Game.currentLevel;
                        Game.screen = sc;
                    }
                }
            }
            ));

        }

        for (int i = 0; i < buttons.size(); i++)
        {
            int page = i / (rows * 3);
            int offset = 0;

            if (page * rows * 3 + rows < buttons.size())
                offset = -190;

            if (page * rows * 3 + rows * 2 < buttons.size())
                offset = -380;

            buttons.get(i).posY = Drawing.drawing.interfaceSizeY / 2 + yoffset + (i % rows) * 60;

            if (i / rows % 3 == 0)
                buttons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset;
            else if (i / rows % 3 == 1)
                buttons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380;
            else
                buttons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380 * 2;
        }
    }

    @Override
    public void update()
    {
        super.update();

        for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, buttons.size()); i++)
        {
            buttons.get(i).update();
        }

        if (page > 0)
            previous.update();

        if (buttons.size() > (1 + page) * rows * 3)
            next.update();
    }

    @Override
    public void draw()
    {
        super.draw();

        for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, buttons.size()); i++)
        {
            buttons.get(i).draw();
        }

        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Select a level to upload...");

        if (page > 0)
            previous.draw();

        if (buttons.size() > (1 + page) * rows * 3)
            next.draw();
    }

}
