package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

import java.util.ArrayList;
import java.util.Arrays;

public class ScreenChangelog extends Screen
{
    public Screen prev = Game.screen;
    public ArrayList<String> pages = new ArrayList<>();
    public int currentPage;
    public String[] pageContents;

    public ScreenChangelog()
    {
        super(350, 40, 380, 60);

        this.music = "tomato_feast_1_options.ogg";
        this.musicID = "menu";

        this.next.image = "play.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.previous.image = "play.png";
        this.previous.imageSizeX = -25;
        this.previous.imageSizeY = 25;
        this.previous.imageXOffset = -145;
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

    Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Done", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = prev;
            Game.lastVersion = Game.version;
            ScreenOptions.saveOptions(Game.homedir);
            pageContents = pages.get(currentPage).split("\n");
        }
    }
    );

    Button next = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            currentPage++;
            pageContents = pages.get(currentPage).split("\n");
        }
    }
    );

    Button previous = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Previous page", new Runnable()
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

        if (pageContents != null)
        {
            for (int i = 0; i < pageContents.length; i++)
            {
                String s = pageContents[i];

                if (s.startsWith("*"))
                {
                    Drawing.drawing.setInterfaceFontSize(this.titleSize);
                    s = s.substring(1);
                }
                else
                    Drawing.drawing.setInterfaceFontSize(this.textSize);

                Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + (-(pageContents.length - 1) / 2.0 + i) * this.objYSpace / 2 - this.objYSpace, s);
            }
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
            new Changelog("v1.0.0", new String[]
                    {
                            "*What's new in Tanks v1.0.0:\n\n" +
                                    "*New features:\n\n" +
                                    "All-new adventure crusade\n" +
                                    "New snow obstacle: melts, slows tanks and bullets\n" +
                                    "Crusades can now be shared in parties\n" +
                                    "Mines now destroy bullets in range\n" +
                                    "Freezing bullets make the ground slippery\n" +
                                    "Mac users can now launch directly from the Jar\n" +
                                    "Adjusted tank coins and spawn rates\n" +
                                    "New update changelog screen",

                            "*Levels:\n\n" +
                                    "Max level size increased\n" +
                                    "More variation in level generator\n\n" +

                            "*Items:\n\n" +
                                    "New mine item\n" +
                                    "Add items and shops in individual levels\n" +
                                    "New item templates\n" +
                                    "New item textures",

                            "*User interfaces:\n\n" +
                                    "New \"restart\" button on the pause menu\n" +
                                    "New \"about\" screen with links\n" +
                                    "List reordering for item and crusade level lists\n" +
                                    "New \"test controls\" button for mobile input options\n" +
                                    "New fullscreen mode\n" +
                                    "White text is now used in dark levels\n" +
                                    "New chat background\n" +
                                    "Title text is now larger\n" +
                                    "Mobile UI scale improved\n" +
                                    "Pre-game user highlighting in multiplayer\n" +
                                    "New tank icons in player chat\n" +
                                    "New color selection sliders\n" +
                                    "New UI glow effects\n" +
                                    "Party lobby menu improvements\n" +
                                    "And many, many more minor improvements",

                            "*Graphics:\n\n" +
                                    "Updated bullet effects\n" +
                                    "Updated health indicators\n" +
                                    "Flashier 3D fireworks\n\n" +

                            "*Audio:\n\n" +
                                    "New volume controls\n" +
                                    "New songs for battle, crusade, and battle victory/defeat\n" +
                                    "New sound effects\n" +
                                    "\n\n...and countless bug fixes and other minor improvements. Enjoy!"
                    });

            new Changelog("v1.1.0", new String[]
                    {
                            "*What's new in Tanks v1.1.0:\n\n" +
                                    "*New features:\n\n" +
                                    "Added mustard tank which shoots over walls\n" +
                                    "Added orange-red tank which shoots explosive bullets\n" +
                                    "Added gold tank which boosts its allies' speed\n" +
                                    "Added boost panel\n" +
                                    "Added no bounce block\n" +
                                    "Added block which breaks when hit by a bullet\n" +
                                    "Added block which explodes when touched\n" +
                                    "Added light block\n\n" +

                                    "*Levels:\n\n" +
                                    "New time limit option for levels\n" +
                                    "Added lighting options to levels",

                                    "*Options:\n\n" +
                                    "Added shadows and shadow quality option\n" +
                                    "Added option to show time elapsed\n" +
                                    "Added keybind to instantly start a level\n\n" +

                                    "*More:\n\n" +
                                    "Made some tanks smarter\n" +
                                    "Bullets harmless to you (friendly fire off) now flash\n" +
                                    "Added a new extension API\n" +
                                    "...and the usual bug fixes and various minor improvements"

                    });
        }
    }
}
