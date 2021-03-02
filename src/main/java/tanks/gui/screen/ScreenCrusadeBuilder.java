package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.Selector;
import tanks.gui.TextBox;
import tanks.hotbar.item.Item;
import tanks.registry.RegistryItem;

import java.util.ArrayList;

public class ScreenCrusadeBuilder extends Screen implements IItemScreen
{
    public enum Mode {options, levels, items}

    public Crusade crusade;
    public Mode mode = Mode.options;

    public ButtonList levelButtons;
    public ButtonList itemButtons;

    public TextBox crusadeName;
    public TextBox startingLives;
    public TextBox bonusLifeFrequency;

    public Selector itemSelector;

    public ScreenCrusadeBuilder instance = this;

    public int titleOffset = -270;

    public Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Exit", new Runnable()
    {
        @Override
        public void run()
        {
            save();
            Game.screen = new ScreenCrusadeDetails(new Crusade(Game.game.fileManager.getFile(crusade.fileName), crusade.name));
        }
    }
    );

    public Button options = new Button(Drawing.drawing.interfaceSizeX / 2 - 380, 60, this.objWidth, this.objHeight, "Options", new Runnable()
    {
        @Override
        public void run()
        {
            mode = Mode.options;
            levelButtons.reorder = false;
            itemButtons.reorder = false;
        }
    });

    public Button levels = new Button(Drawing.drawing.interfaceSizeX / 2, 60, this.objWidth, this.objHeight, "Levels", new Runnable()
    {
        @Override
        public void run()
        {
            mode = Mode.levels;
            levelButtons.reorder = false;
            itemButtons.reorder = false;
        }
    });

    public Button items = new Button(Drawing.drawing.interfaceSizeX / 2 + 380, 60, this.objWidth, this.objHeight, "Shop", new Runnable()
    {
        @Override
        public void run()
        {
            mode = Mode.items;
            levelButtons.reorder = false;
            itemButtons.reorder = false;
        }
    });

    public Button addLevel = new Button(Drawing.drawing.interfaceSizeX / 2 + 380, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Add level", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenCrusadeAddLevel((ScreenCrusadeBuilder) Game.screen);
        }
    }
    );

    public Button reorderLevels = new Button(Drawing.drawing.interfaceSizeX / 2 - 380, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Reorder levels", new Runnable()
    {
        @Override
        public void run()
        {
            levelButtons.reorder = !levelButtons.reorder;

            if (levelButtons.reorder)
                reorderLevels.text = "Stop reordering";
            else
                reorderLevels.text = "Reorder levels";
        }
    }
    );

    public Button reorderItems = new Button(Drawing.drawing.interfaceSizeX / 2 - 380, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Reorder items", new Runnable()
    {
        @Override
        public void run()
        {
            itemButtons.reorder = !itemButtons.reorder;

            if (itemButtons.reorder)
                reorderItems.text = "Stop reordering";
            else
                reorderItems.text = "Reorder items";
        }
    }
    );

    public Button addItem = new Button(Drawing.drawing.interfaceSizeX / 2 + 380, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Add item", new Runnable()
    {
        @Override
        public void run()
        {
            itemSelector.setScreen();
        }
    }
    );

    public ScreenCrusadeBuilder(Crusade c)
    {
        super(350, 40, 380, 60);

        this.music = "tomato_feast_4.ogg";
        this.musicID = "menu";

        this.crusade = c;

        for (Item i: c.crusadeItems)
        {
            i.importProperties();
        }

        String[] itemNames = new String[Game.registryItem.itemEntries.size() + 1];
        String[] itemImages = new String[Game.registryItem.itemEntries.size() + 1];

        for (int i = 0; i < Game.registryItem.itemEntries.size(); i++)
        {
            RegistryItem.ItemEntry r = Game.registryItem.getEntry(i);
            itemNames[i] = r.name;
            itemImages[i] = r.image;
        }

        itemNames[Game.registryItem.itemEntries.size()] = "From template";
        itemImages[Game.registryItem.itemEntries.size()] = "item.png";

        itemSelector = new Selector(0, 0, 0, 0, "item type", itemNames, () ->
        {
            if (itemSelector.selectedOption == itemSelector.options.length - 1)
            {
                Game.screen = new ScreenAddSavedItem(this, this.addItem);
            }
            else
            {
                Item i = Game.registryItem.getEntry(itemSelector.options[itemSelector.selectedOption]).getItem();
                addItem(i);
            }
        });

        itemSelector.images = itemImages;
        itemSelector.quick = true;

        crusadeName = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, this.objWidth, this.objHeight, "Crusade name", new Runnable()
        {
            @Override
            public void run()
            {
                BaseFile file = Game.game.fileManager.getFile(crusade.fileName);

                if (crusadeName.inputText.length() > 0 && !Game.game.fileManager.getFile(Game.homedir + Game.crusadeDir + "/" + crusadeName.inputText + ".tanks").exists())
                {
                    if (file.exists())
                    {
                        file.renameTo(Game.homedir + Game.crusadeDir + "/" + crusadeName.inputText.replace(" ", "_") + ".tanks");
                    }

                    while (file.exists())
                    {
                        file.delete();
                    }

                    crusade.name = crusadeName.inputText;
                    crusade.fileName = Game.homedir + Game.crusadeDir + "/" + crusadeName.inputText.replace(" ", "_") + ".tanks";
                }
                else
                {
                    crusadeName.inputText = crusade.name.split("\\.")[0].replace("_", " ");
                }
            }
        }
                , crusade.name.split("\\.")[0].replace("_", " "));

        crusadeName.enableCaps = true;

        startingLives = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Starting lives", new Runnable()
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
        startingLives.maxChars = 9;

        bonusLifeFrequency = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, this.objWidth, this.objHeight, "Bonus life frequency", new Runnable()
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
        bonusLifeFrequency.maxChars = 9;

        if (Drawing.drawing.interfaceScaleZoom > 1)
        {
            this.levelButtons = new ButtonList(new ArrayList<>(), 0, 0, 0);
            this.itemButtons = new ButtonList(new ArrayList<>(), 0, 0, 0);

            this.titleOffset = -210;

            this.levelButtons.controlsYOffset = -30;
            this.itemButtons.controlsYOffset = -30;
        }
        else
        {
            this.levelButtons = new ButtonList(new ArrayList<>(), 0, 0, -30);
            this.itemButtons = new ButtonList(new ArrayList<>(), 0, 0, -30);
        }

        this.levelButtons.arrowsEnabled = true;
        this.itemButtons.arrowsEnabled = true;

        this.levelButtons.reorderBehavior = (i, j) ->
        {
            this.crusade.levels.add(j, this.crusade.levels.remove((int)i));
            this.crusade.levelNames.add(j, this.crusade.levelNames.remove((int)i));
            this.refreshLevelButtons();
        };

        this.itemButtons.reorderBehavior = (i, j) ->
        {
            this.crusade.crusadeItems.add(j, this.crusade.crusadeItems.remove((int)i));
            this.refreshItemButtons();
        };

        this.refreshLevelButtons();
        this.refreshItemButtons();

        this.levelButtons.indexPrefix = true;
    }

    public void refreshLevelButtons()
    {
        this.levelButtons.buttons.clear();

        for (int i = 0; i < this.crusade.levels.size(); i++)
        {
            int j = i;
            this.levelButtons.buttons.add(new Button(0, 0, this.objWidth, this.objHeight, this.crusade.levelNames.get(i).replace("_", " "), new Runnable()
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

        this.levelButtons.sortButtons();
    }

    public void refreshItemButtons()
    {
        this.itemButtons.buttons.clear();

        for (int i = 0; i < this.crusade.crusadeItems.size(); i++)
        {
            int j = i;

            Button b = new Button(0, 0, this.objWidth, this.objHeight, this.crusade.crusadeItems.get(i).name, new Runnable()
            {
                @Override
                public void run()
                {
                    Game.screen = new ScreenEditItem(crusade.crusadeItems.get(j), (IItemScreen) Game.screen);
                }
            });

            b.image = crusade.crusadeItems.get(j).icon;
            b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
            b.imageSizeX = b.sizeY;
            b.imageSizeY = b.sizeY;

            int p = crusade.crusadeItems.get(i).price;
            String price = p + " ";
            if (p == 0)
                price = "Free!";
            else if (p == 1)
                price += "coin";
            else
                price += "coins";

            b.subtext = price;

            this.itemButtons.buttons.add(b);
        }

        this.itemButtons.sortButtons();
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
            levelButtons.update();

            quit.update();
            addLevel.update();
            reorderLevels.update();
        }
        else if (mode == Mode.options)
        {
            crusadeName.update();
            startingLives.update();
            bonusLifeFrequency.update();
            quit.update();
        }
        else if (mode == Mode.items)
        {
            itemButtons.update();

            quit.update();
            addItem.update();
            reorderItems.update();
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
            reorderLevels.draw();
            quit.draw();
            addLevel.draw();
            levelButtons.draw();

            Drawing.drawing.setInterfaceFontSize(this.titleSize);
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + titleOffset, "Crusade levels");
        }
        else if (mode == Mode.options)
        {
            bonusLifeFrequency.draw();
            startingLives.draw();
            crusadeName.draw();

            quit.draw();

            Drawing.drawing.setInterfaceFontSize(this.titleSize);
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + titleOffset, "Crusade options");
        }
        else if (mode == Mode.items)
        {
            reorderItems.draw();
            quit.draw();
            addItem.draw();

            itemButtons.draw();

            Drawing.drawing.setInterfaceFontSize(this.titleSize);
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + titleOffset, "Crusade items");
        }
    }

    @Override
    public void addItem(Item i)
    {
        crusade.crusadeItems.add(i);
        Game.screen = new ScreenEditItem(i, instance);
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
