package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.Selector;
import tanks.gui.TextBox;
import tanks.hotbar.item.Item;
import tanks.registry.RegistryItem;

import java.util.ArrayList;

public class ScreenCrusadeBuilder extends ItemScreen
{
    public enum Mode {options, levels, items}

    public Crusade crusade;
    public int levelsPage = 0;
    public int itemsPage = 0;
    public Mode mode = Mode.options;

    int rows = 6;
    int yoffset = -150;

    ArrayList<Button> levelButtons = new ArrayList<Button>();
    ArrayList<Button> itemButtons = new ArrayList<Button>();

    public TextBox crusadeName;
    public TextBox startingLives;
    public TextBox bonusLifeFrequency;

    public Selector itemSelector;

    public ScreenCrusadeBuilder instance = this;

    public Button quit = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Exit", new Runnable()
    {
        @Override
        public void run()
        {
            save();
            Game.screen = new ScreenCrusadeDetails(new Crusade(Game.game.fileManager.getFile(crusade.fileName), crusade.name));
        }
    }
    );

    public Button quit2 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Exit", new Runnable()
    {
        @Override
        public void run()
        {
            quit.function.run();
        }
    }
    );


    public Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            if (mode == Mode.levels)
                levelsPage++;
            else if (mode == Mode.items)
                itemsPage++;
        }
    }
    );

    public Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            if (mode == Mode.levels)
                levelsPage--;
            else if (mode == Mode.items)
                itemsPage--;
        }
    }
    );

    public Button options = new Button(Drawing.drawing.interfaceSizeX / 2 - 380, 60, 350, 40, "Options", new Runnable()
    {
        @Override
        public void run()
        {
            mode = Mode.options;
        }
    });

    public Button levels = new Button(Drawing.drawing.interfaceSizeX / 2, 60, 350, 40, "Levels", new Runnable()
    {
        @Override
        public void run()
        {
            mode = Mode.levels;
        }
    });

    public Button items = new Button(Drawing.drawing.interfaceSizeX / 2 + 380, 60, 350, 40, "Items", new Runnable()
    {
        @Override
        public void run()
        {
            mode = Mode.items;
        }
    });

    public Button addLevel = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Add level", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenCrusadeAddLevel((ScreenCrusadeBuilder) Game.screen);
        }
    }
    );

    public Button addItem = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Add item", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenSelector(itemSelector, Game.screen);
        }
    }
    );

    public ScreenCrusadeBuilder(Crusade c)
    {
        this.music = "tomato_feast_4.ogg";
        this.musicID = "menu";

        this.crusade = c;

        for (Item i: c.crusadeItems)
        {
            i.importProperties();
        }

        this.refreshLevelButtons();
        this.refreshItemButtons();

        String[] itemNames = new String[Game.registryItem.itemEntries.size()];
        for (int i = 0; i < Game.registryItem.itemEntries.size(); i++)
        {
            RegistryItem.ItemEntry r = Game.registryItem.getEntry(i);
            itemNames[i] = r.name;
        }

        itemSelector = new Selector(0, 0, 0, 0, "item type", itemNames, new Runnable()
        {
            @Override
            public void run()
            {
                Item i = Game.registryItem.getEntry(itemSelector.options[itemSelector.selectedOption]).getItem();
                crusade.crusadeItems.add(i);

                Game.screen = new ScreenEditItem(i, instance);
            }
        });

        itemSelector.quick = true;

        crusadeName = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, 350, 40, "Crusade name", new Runnable()
        {
            @Override
            public void run()
            {
                BaseFile file = Game.game.fileManager.getFile(crusade.fileName);

                if (crusadeName.inputText.length() > 0 && !Game.game.fileManager.getFile(Game.homedir + ScreenCrusades.crusadeDir + "/" + crusadeName.inputText + ".tanks").exists())
                {
                    if (file.exists())
                    {
                        file.renameTo(Game.homedir + ScreenCrusades.crusadeDir + "/" + crusadeName.inputText.replace(" ", "_") + ".tanks");
                    }

                    while (file.exists())
                    {
                        file.delete();
                    }

                    crusade.name = crusadeName.inputText;
                    crusade.fileName = Game.homedir + ScreenCrusades.crusadeDir + "/" + crusadeName.inputText.replace(" ", "_") + ".tanks";
                }
                else
                {
                    crusadeName.inputText = crusade.name.split("\\.")[0].replace("_", " ");
                }
            }
        }
                , crusade.name.split("\\.")[0].replace("_", " "));

        crusadeName.enableCaps = true;

        startingLives = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Starting lives", new Runnable()
        {
            @Override
            public void run()
            {
                if (startingLives.inputText.length() == 0)
                    startingLives.inputText = crusade.startingLives + "";
                else
                    crusade.startingLives = Integer.parseInt(startingLives.inputText);
            }
        }
                , crusade.startingLives + "");

        startingLives.allowLetters = false;
        startingLives.allowSpaces = false;
        startingLives.minValue = 1;
        startingLives.checkMinValue = true;

        bonusLifeFrequency = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "Bonus life frequency", new Runnable()
        {
            @Override
            public void run()
            {
                if (bonusLifeFrequency.inputText.length() == 0)
                    bonusLifeFrequency.inputText = crusade.bonusLifeFrequency + "";
                else
                    crusade.bonusLifeFrequency = Integer.parseInt(bonusLifeFrequency.inputText);
            }
        }
                , crusade.bonusLifeFrequency + "");

        bonusLifeFrequency.allowLetters = false;
        bonusLifeFrequency.allowSpaces = false;
        bonusLifeFrequency.minValue = 1;
        bonusLifeFrequency.checkMinValue = true;
    }

    public void refreshLevelButtons()
    {
        this.levelButtons.clear();

        for (int i = 0; i < this.crusade.levels.size(); i++)
        {
            int j = i;
            this.levelButtons.add(new Button(0, 0, 350, 40, this.crusade.levelNames.get(i).replace("_", " "), new Runnable()
            {
                @Override
                public void run()
                {
                    String name = crusade.levelNames.remove(j);
                    String level = crusade.levels.remove(j);

                    ScreenCrusadeEditLevel s = new ScreenCrusadeEditLevel(name, level, j + 1, (ScreenCrusadeBuilder) Game.screen);
                    new Level(level).loadLevel(s);
                    Game.screen = s;
                }
            }));
        }

        for (int i = 0; i < levelButtons.size(); i++)
        {
            int page = i / (rows * 3);
            int offset = 0;

            if (page * rows * 3 + rows < levelButtons.size())
                offset = -190;

            if (page * rows * 3 + rows * 2 < levelButtons.size())
                offset = -380;

            levelButtons.get(i).posY = Drawing.drawing.interfaceSizeY / 2 + yoffset + (i % rows) * 60;

            if (i / rows % 3 == 0)
                levelButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset;
            else if (i / rows % 3 == 1)
                levelButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380;
            else
                levelButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380 * 2;
        }
    }

    public void refreshItemButtons()
    {
        this.itemButtons.clear();

        for (int i = 0; i < this.crusade.crusadeItems.size(); i++)
        {
            int j = i;
            this.itemButtons.add(new Button(0, 0, 350, 40, this.crusade.crusadeItems.get(i).name, new Runnable()
            {
                @Override
                public void run()
                {
                    Game.screen = new ScreenEditItem(crusade.crusadeItems.get(j), (ItemScreen) Game.screen);
                }
            }));
        }

        for (int i = 0; i < itemButtons.size(); i++)
        {
            int page = i / (rows * 3);
            int offset = 0;

            if (page * rows * 3 + rows < itemButtons.size())
                offset = -190;

            if (page * rows * 3 + rows * 2 < itemButtons.size())
                offset = -380;

            itemButtons.get(i).posY = Drawing.drawing.interfaceSizeY / 2 + yoffset + (i % rows) * 60;

            if (i / rows % 3 == 0)
                itemButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset;
            else if (i / rows % 3 == 1)
                itemButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380;
            else
                itemButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380 * 2;
        }
    }

    @Override
    public void update()
    {
        options.enabled = mode != Mode.options;
        levels.enabled = mode != Mode.levels;
        items.enabled = mode != Mode.items;

        options.update();
        levels.update();
        items.update();

        if (mode == Mode.levels)
        {
            for (int i = levelsPage * rows * 3; i < Math.min(levelsPage * rows * 3 + rows * 3, levelButtons.size()); i++)
            {
                levelButtons.get(i).update();
            }

            quit.update();
            addLevel.update();

            if (levelsPage > 0)
                previous.update();

            if (levelButtons.size() > (1 + levelsPage) * rows * 3)
                next.update();
        }
        else if (mode == Mode.options)
        {
            crusadeName.update();
            startingLives.update();
            bonusLifeFrequency.update();
            quit2.update();
        }
        else if (mode == Mode.items)
        {
            for (int i = itemsPage * rows * 3; i < Math.min(itemsPage * rows * 3 + rows * 3, itemButtons.size()); i++)
            {
                itemButtons.get(i).update();
            }

            quit.update();
            addItem.update();

            if (itemsPage > 0)
                previous.update();

            if (itemButtons.size() > (1 + itemsPage) * rows * 3)
                next.update();
        }
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        double extraHeight = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2;
        double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, -extraHeight / 2, width, extraHeight);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, 60, width, 120);

        options.draw();
        levels.draw();
        items.draw();

        if (mode == Mode.levels)
        {
            quit.draw();
            addLevel.draw();

            if (levelsPage > 0)
                previous.draw();

            if (levelButtons.size() > (1 + levelsPage) * rows * 3)
                next.draw();

            for (int i = levelsPage * rows * 3; i < Math.min(levelsPage * rows * 3 + rows * 3, levelButtons.size()); i++)
            {
                Button b = levelButtons.get(i);
                String s = b.text;
                b.text = (i + 1) + ". " + s;
                b.draw();
                b.text = s;
            }

            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Crusade levels");
        }
        else if (mode == Mode.options)
        {
            crusadeName.draw();
            startingLives.draw();
            bonusLifeFrequency.draw();
            quit2.draw();

            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Crusade options");
        }
        else if (mode == Mode.items)
        {
            quit.draw();
            addItem.draw();

            if (itemsPage > 0)
                previous.draw();

            if (itemButtons.size() > (1 + itemsPage) * rows * 3)
                next.draw();

            for (int i = itemsPage * rows * 3; i < Math.min(itemsPage * rows * 3 + rows * 3, itemButtons.size()); i++)
            {
                itemButtons.get(i).draw();
            }

            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Crusade items");
        }
    }

    @Override
    public void removeItem(Item i)
    {
        this.crusade.crusadeItems.remove(i);
        this.refreshItemButtons();
    }

    @Override
    public void refreshItems()
    {
        this.refreshItemButtons();
    }

    public void save()
    {
        for (Item i: this.crusade.crusadeItems)
            i.exportProperties();

        BaseFile f = Game.game.fileManager.getFile(crusade.fileName);

        try
        {
            f.startWriting();
            f.println("properties");
            f.println(this.crusade.startingLives + "," + this.crusade.bonusLifeFrequency);
            f.println("items");

            for (Item i: this.crusade.crusadeItems)
                f.println(i.toString());

            f.println("levels");

            for (int i = 0; i < this.crusade.levels.size(); i++)
            {
                String l = this.crusade.levels.get(i);
                f.println(l.substring(l.indexOf('{'), l.indexOf('}') + 1) + " name=" + this.crusade.levelNames.get(i));
            }

            f.stopWriting();
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }
}
