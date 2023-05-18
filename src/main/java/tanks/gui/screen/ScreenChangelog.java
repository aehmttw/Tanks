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

        this.music = "menu_options.ogg";
        this.musicID = "menu";

        this.next.image = "icons/forward.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.previous.image = "icons/back.png";
        this.previous.imageSizeX = 25;
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

                Drawing.drawing.drawInterfaceText(this.centerX - 400, this.centerY + (-(pageContents.length - 1) / 2.0 + i) * this.objYSpace / 2 - this.objYSpace, s, false);
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

            new Changelog("v1.1.1", new String[]
                    {
                            "*What's new in Tanks v1.1.1:\n\n" +
                                    "Split fast/fancy/super graphics setting into multiple options\n" +
                                    "Reduced certain particle effects to improve performance\n" +
                                    "New option to change intensity of particle effects\n" +
                                    "Several bug fixes and minor improvements"
                    });

            new Changelog("v1.1.2", new String[]
                    {
                            "*What's new in Tanks v1.1.2:\n\n" +
                                    "Added new sound for arc bullets\n" +
                                    "Added support for custom resources in extensions\n" +
                                    "Added support for custom tank models"
                    });

            new Changelog("v1.1.3", new String[]
                    {
                            "*What's new in Tanks v1.1.3:\n\n" +
                                    "Explosive blocks now have a delay if triggered by other explosions\n" +
                                    "Several bug fixes and other minor improvements"
                    });

            new Changelog("v1.2.0", new String[]
                    {
                            "*What's new in Tanks v1.2.0:\n\n" +
                                    "*New features:\n\n" +
                                    "Added light pink tank which gets angry when it sees you\n" +
                                    "Added mimic tank which mimics the behavior of a nearby tank\n" +
                                    "Dummy tanks can be now used in the level editor\n" +
                                    "Added support for Steam peer-to-peer multiplayer\n" +
                                    "You can now override the game's default resources\n\n" +

                                    "*Balancing:\n\n" +
                                    "Tanks no longer take damage after the battle has ended\n" +
                                    "Blue tank electricity now only arcs between 4 targets (from 6)\n" +
                                    "Cyan tank freeze duration decreased by 1 second\n" +
                                    "Large timed levels have longer timers\n" +
                                    "Orangered tank is now immune to explosion damage\n\n",

                            "*More:\n\n" +
                                    "Improved the crusade editor level edit menu\n" +
                                    "Changed light block appearance\n" +
                                    "Fireworks are more varied and prettier!\n" +
                                    "Medic tank cross is now green\n" +
                                    "Performance improvements\n" +
                                    "...and, of course, bug fixes and other improvements"
                    });

            new Changelog("v1.2.1", new String[]
                    {
                            "*What's new in Tanks v1.2.1:\n\n" +
                                    "Added party host options menu\n" +
                                    "Added option to disable all friendly fire in parties\n" +
                                    "Added option to change the party countdown timer\n" +
                                    "Added auto ready multiplayer option\n" +
                                    "Added fullscreen button (in addition to the key)\n" +
                                    "Arc bullets now have colored shadows\n" +
                                    "Teleporter orbs are now the color of the teleporting tank\n" +
                                    "Player spawns are now more spread out in versus mode\n" +
                                    "Flash from bullets harmless to you (friendly fire off) is bigger\n" +
                                    "Bug fixes and other minor improvements"
                    });

            new Changelog("v1.2.2", new String[]
                    {
                            "*What's new in Tanks v1.2.2:\n\n" +
                                    "*New features:\n\n" +
                                    "Added crusade statistics screen\n" +
                                    "Added music for editor and dark levels\n" +
                                    "Added deterministic mode for speedrunners\n" +
                                    "Added built-in item templates\n" +
                                    "Added option to show level names in crusades\n" +
                                    "Added indicator for where arc bullets will land\n" +
                                    "Added mini tank to level editor\n",

                            "*Improvements:\n\n" +
                                    "Hovering over the item bar slots shows keybinds\n" +
                                    "Leaving and rejoining a party crusade before a new level is\n" +
                                    "    loaded recovers progress\n" +
                                    "Explosive blocks now award players coins for kills\n" +
                                    "Level timer now shows in editor after playing from 'My levels'\n" +
                                    "Reworked the 'Save this level' button\n" +
                                    "You can now save levels you play in parties\n" +
                                    "Bug fixes and other minor improvements"
                    });

            new Changelog("v1.3.0", new String[]
                    {
                            "*What's new in Tanks v1.3.0:\n\n" +
                                    "*New features:\n\n" +
                                    "Added all-new castle crusade\n" +
                                    "Added cut, copy, and paste to level editor\n" +
                                    "Added multishot and shot spread options to bullets\n" +
                                    "Added crusade option to disable respawning tanks\n\n" +

                                    "*Sounds and music:\n\n" +
                                    "Added individual battle music tracks for each tanks\n" +
                                    "New editor music which changes based on tanks present\n" +
                                    "New sounds for winning and losing battles",

                            "*User interfaces:\n\n" +
                                    "Improved lists with search, sort, and jump buttons\n" +
                                    "New window options menu for resolution and fullscreen\n" +
                                    "New option to warn before closing unsaved work\n" +
                                    "New option to prevent mouse leaving the window bounds\n\n" +

                                    "*Improvements:\n\n" +
                                    "Cyan tanks are now immune to freeze bullets and ice\n" +
                                    "Improved game title appearance\n" +
                                    "Huge rendering improvements\n" +
                                    "Added translation support\n" +
                                    "Holes are now bigger\n" +
                                    "More animations on the crusade stats screen\n" +
                                    "Added native support for ARM64 (M1) architecture on Mac\n" +
                                    "Bug fixes and other minor improvements"
                    });

            new Changelog("v1.3.1", new String[]
                    {
                            "*What's new in Tanks v1.3.1:\n\n" +
                                    "Added maximum framerate option\n" +
                                    "Scroll while holding the zoom key for manual zoom\n" +
                                    "Added keybinds to manually zoom in and out\n" +
                                    "Added automatic zoom mode with keybind\n" +
                                    "Improved keybindings screen\n" +
                                    "Bug fixes and other minor improvements\n"
                    });

            new Changelog("v1.3.2", new String[]
                    {
                            "*What's new in Tanks v1.3.2:\n\n" +
                                    "Changed appearance of electric bullets\n" +
                                    "Arc bullets can now be set to bounce\n" +
                                    "Bug fixes and other minor improvements\n"
                    });

            new Changelog("v1.4.0", new String[]
                    {
                            "*What's new in Tanks v1.4.0:\n\n" +
                                    "*New features:\n\n" +
                                    "Added tank editor and custom tanks\n" +
                                    "Added air bullet which pushes things\n" +
                                    "Added homing bullet which moves towards targets\n" +
                                    "Added light blue tank which uses air bullets\n" +
                                    "Added salmon tank which uses homing bullets\n" +
                                    "Added beginner crusade\n\n" +

                                    "*Tank behavior:\n\n" +
                                    "Tanks now try to avoid walls\n" +
                                    "Tanks now see through destructible blocks\n" +
                                    "Tanks now avoid explosive blocks",

                                    "*Balancing:\n\n" +
                                    "Boss tank now only spawns 5 tank types\n" +
                                    "Cooldowns are now per item\n" +
                                    "Made random level teleporters rarer\n" +
                                    "Increased player tank acceleration\n" +
                                    "Nerfed castle crusade level 'Castle artillery'\n\n" +

                                    "*Graphics:\n\n" +
                                    "New tank textures and models\n" +
                                    "Updated crusade info and stats screen background\n" +
                                    "Updated item icons\n" +
                                    "Made UI icons more colorful\n" +
                                    "Updated 3D explosion particle effect\n" +
                                    "Added indicator when new shop items are available\n" +
                                    "Added bullet cooldown indicator to hotbar",

                                    "*More:\n\n" +
                                    "New editor music\n" +
                                    "Shop is now available in versus mode\n" +
                                    "Increased some textbox character limits\n" +
                                    "IP address can now be hidden in parties\n\n" +
                                    "Bug fixes and other minor improvements\n"
                    });

            new Changelog("v1.4.1", new String[]
                    {
                            "*What's new in Tanks v1.4.1:\n\n" +
                                    "Added bandwidth usage to info bar\n" +
                                    "Fixed a memory leak with custom tank music\n" +
                                    "Fixed a bug with spawning tanks in multiplayer\n"
                    });

            new Changelog("v1.5.0", new String[]
                    {
                            "*What's new in Tanks v1.5.0:\n\n" +
                            "*New arcade mode minigame:\n\n" +
                                    "Point system based on tank kills\n" +
                                    "Continuously spawning enemy tank waves\n" +
                                    "Respawn if you die\n" +
                                    "Time limit of 2 minutes and 12 seconds\n" +
                                    "A rampage system for destroying tanks in a row\n" +
                                    "Tanks drop items that you can use to your advantage\n" +
                                    "Frenzy mode: destroy all you can when time runs out!\n" +
                                    "Point bonuses judging your performance at the end\n\n",
                            "*Crusades:\n\n" +
                                    "Font is now varied across statistics for readability\n" +
                                    "Added crusade descriptions for built-in crusades\n" +
                                    "Built-in crusades now track your best completion time,\n" +
                                    "which you can see directly from the crusade screen\n" +
                                    "Completed crusade runs can be compared to your best run\n\n" +
                            "*Options:\n\n" +
                                    "Reorganized options screens\n" +
                                    "Added new profile customization section to options\n" +
                                    "You can now use a custom tank color in singleplayer\n" +
                                    "Added option to show bullets under terrain\n" +
                                    "Added 30 FPS deterministic mode\n\n",
                            "*More:\n\n" +
                                    "Updated menu music\n" +
                                    "Added item switching indicator on tank\n" +
                                    "Updated tutorial to be more exciting\n" +
                                    "Removed laser from versus mode\n" +
                                    "Tanks will not explode on death if killed right after spawning\n" +
                                    "Changed fireworks appearance\n" +
                                    "Bug fixes and other minor improvements\n"
                    }
                    );

            new Changelog("v1.5.1", new String[]
                    {
                            "*What's new in Tanks v1.5.1:\n\n" +
                                    "Bug fixes\n"
                    }
            );

        }
    }
}