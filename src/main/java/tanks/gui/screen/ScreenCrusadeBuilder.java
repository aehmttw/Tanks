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

    public Button quit = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Exit", new Runnable()
    {
        @Override
        public void run()
        {
            save();
            Game.screen = new ScreenCrusadeDetails(new Crusade(Game.game.fileManager.getFile(crusade.fileName), crusade.name));
        }
    }
    );

    public Button quit2 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Exit", new Runnable()
    {
        @Override
        public void run()
        {
            quit.function.run();
        }
    }
    );

    public Button options = new Button(Drawing.drawing.interfaceSizeX / 2 - 380, 60, this.objWidth, this.objHeight, "Options", new Runnable()
    {
        @Override
        public void run()
        {
            mode = Mode.options;
        }
    });

    public Button levels = new Button(Drawing.drawing.interfaceSizeX / 2, 60, this.objWidth, this.objHeight, "Levels", new Runnable()
    {
        @Override
        public void run()
        {
            mode = Mode.levels;
        }
    });

    public Button items = new Button(Drawing.drawing.interfaceSizeX / 2 + 380, 60, this.objWidth, this.objHeight, "Shop", new Runnable()
    {
        @Override
        public void run()
        {
            mode = Mode.items;
        }
    });

    public Button addLevel = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Add level", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenCrusadeAddLevel((ScreenCrusadeBuilder) Game.screen);
        }
    }
    );

    public Button addItem = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Add item", new Runnable()
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
            itemButtons.update();

            quit.update();
            addItem.update();
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
            levelButtons.draw();

            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + titleOffset, "Crusade levels");
        }
        else if (mode == Mode.options)
        {
            bonusLifeFrequency.draw();
            startingLives.draw();
            crusadeName.draw();

            quit2.draw();

            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + titleOffset, "Crusade options");
        }
        else if (mode == Mode.items)
        {
            quit.draw();
            addItem.draw();

            itemButtons.draw();

            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + titleOffset, "Crusade items");
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
