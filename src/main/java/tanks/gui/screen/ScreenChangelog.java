package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

import java.util.ArrayList;
import java.util.Arrays;

public class ScreenChangelog extends Screen
{
    public ArrayList<String> pages = new ArrayList<>();
    public int currentPage;
    public String[] pageContents;

    public ScreenChangelog()
    {
        this.music = "tomato_feast_1.ogg";
        this.musicID = "menu";
    }

    public void setup()
    {
        for (Changelog l: Changelog.logs)
        {
            if (l.shouldAdd())
                this.add(l.pages);
        }
    }

    public void add(String[] log)
    {
        pages.addAll(Arrays.asList(log));
    }

    Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Done", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenTitle();
            Game.lastVersion = Game.version;
            ScreenOptions.saveOptions(Game.homedir);
            pageContents = pages.get(currentPage).split("\n");
        }
    }
    );

    Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            currentPage++;
            pageContents = pages.get(currentPage).split("\n");
        }
    }
    );

    Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            currentPage--;
            pageContents = pages.get(currentPage).split("\n");
        }
    }
    );

    @Override
    public void update()
    {
        if (pageContents == null)
            pageContents = pages.get(currentPage).split("\n");

        next.enabled = currentPage < pages.size() - 1;
        previous.enabled = currentPage > 0;

        next.update();
        previous.update();
        quit.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (next.enabled || previous.enabled)
        {
            next.draw();
            previous.draw();
        }

        quit.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(24);

        for (int i = 0; i < pageContents.length; i++)
        {
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + (-(pageContents.length - 1) / 2.0 + i) * 30 - 80, pageContents[i]);
        }
    }

    public static class Changelog
    {
        public static ArrayList<Changelog> logs = new ArrayList<>();

        public String[] pages;
        public String version;

        public Changelog(String version, String[] log)
        {
            this.version = version;
            this.pages = log;
            logs.add(this);
        }

        public boolean shouldAdd()
        {
            return Game.compareVersions(this.version, Game.lastVersion) > 0;
        }

        public static void setupLogs()
        {
            new Changelog("v1.0.a", new String[]
                    {
                            "What's new in Tanks v1.0.a:\n\n" +
                            "Individual levels can now have items and shops\n" +
                            "Increased the maximum level size to 400x400\n" +
                            "Added UI glow effects for super graphics\n" + "" +
                            "Other miscellaneous UI improvements\n"+
                            "Added update changelog screen\n" +
                            "Added launching directly from jar file on Mac\n" +
                            "Added a new menu music track\n" +
                            "Various bug fixes and improvements"
                    });

            new Changelog("v1.0.b", new String[]
                    {
                            "What's new in Tanks v1.0.b:\n\n" +
                            "Added mine item\n" +
                            "Other bug fixes and improvements"
                    });

            new Changelog("v1.0.c", new String[]
                    {
                            "What's new in Tanks v1.0.c:\n\n" +
                                    "Added battle music\n" +
                                    "Many bug fixes and improvements"
                    });
        }
    }
}
