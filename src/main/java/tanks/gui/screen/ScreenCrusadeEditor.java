package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.Selector;
import tanks.gui.TextBox;
import tanks.item.Item;
import tanks.item.ItemIcon;
import tanks.registry.RegistryItem;
import tanks.tank.TankAIControlled;
import tanks.tank.TankPlayer;
import tanks.tankson.ArrayListIndexPointer;
import tanks.tankson.MonitoredArrayListIndexPointer;
import tanks.tankson.Pointer;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class ScreenCrusadeEditor extends Screen implements ITankBuildScreen
{
    public Crusade crusade;

    public ButtonList buttons;

    public boolean showLevels = true;
    public boolean showItems = true;
    public boolean showBuilds = true;

    public int titleOffset = -270;

    public TextBox crusadeName;

    public boolean readOnly;
    public Screen previous;

    public Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Exit", new Runnable()
    {
        @Override
        public void run()
        {
            if (!readOnly)
            {
                save();
                Game.screen = new ScreenCrusadeDetails(new Crusade(Game.game.fileManager.getFile(crusade.fileName), crusade.name));
            }
            else
                Game.screen = previous;
        }
    }
    );

    public Button reorder = new Button(Drawing.drawing.interfaceSizeX / 2 - 380, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Reorder", new Runnable()
    {
        @Override
        public void run()
        {
            buttons.reorder = !buttons.reorder;

            if (buttons.reorder)
                reorder.setText("Stop reordering");
            else
                reorder.setText("Reorder");

            if (!buttons.reorder)
                executeReorder();
        }
    }
    );

    public Selector itemSelector;

    public static final String shownText = "\u00A7000200000255shown";
    public static final String hiddenText = "\u00A7200000000255hidden";

    double add_offset = this.objWidth / 2 - this.objHeight * 0.625;
    double hide_offset = this.objWidth / 2 - this.objHeight * 1.875;
    double label_offset = -this.objHeight * 0.5;
    double icon_offset = -this.objWidth / 2 + this.objHeight * 0.875;

    public Button toggleLevels = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace + hide_offset, Drawing.drawing.interfaceSizeY / 2 + titleOffset, this.objHeight, this.objHeight, "", () ->
    {
        if (buttons.reorder)
            reorder.function.run();
        showLevels = !showLevels;
        this.refreshButtons();
    }, "");

    public Button toggleItems = new Button(Drawing.drawing.interfaceSizeX / 2 + hide_offset, Drawing.drawing.interfaceSizeY / 2 + titleOffset, this.objHeight, this.objHeight, "", () ->
    {
        if (buttons.reorder)
            executeReorder();
        showItems = !showItems;
        this.refreshButtons();
    }, "");

    public Button toggleBuilds = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace + hide_offset, Drawing.drawing.interfaceSizeY / 2 + titleOffset, this.objHeight, this.objHeight, "", () ->
    {
        if (buttons.reorder)
            executeReorder();
        showBuilds = !showBuilds;
        this.refreshButtons();
    }, "");

    public Button addLevel = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace + add_offset, Drawing.drawing.interfaceSizeY / 2 + titleOffset, this.objHeight, this.objHeight, "+", () -> Game.screen = new ScreenCrusadeAddLevel(this), "Add a level");

    public Button addItem = new Button(Drawing.drawing.interfaceSizeX / 2 + add_offset, Drawing.drawing.interfaceSizeY / 2 + titleOffset, this.objHeight, this.objHeight, "+", new Runnable()
    {
        @Override
        public void run()
        {
            itemSelector.setScreen();
        }
    }, "Add an item"
    );

    public Button addBuild = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace + add_offset, Drawing.drawing.interfaceSizeY / 2 + titleOffset, this.objHeight, this.objHeight, "+", () ->
    {
        Game.screen = new ScreenAddSavedTankBuild(this, crusade.crusadeShopBuilds);
    }, "Add a player build");

    public Button options = new Button(Drawing.drawing.interfaceSizeX / 2 + 380, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Crusade options", () -> Game.screen = new ScreenCrusadeOptions(this));

    @Override
    public Pointer<TankPlayer.ShopTankBuild> addTank(TankPlayer.ShopTankBuild t, boolean select)
    {
        this.crusade.crusadeShopBuilds.add(new TankPlayer.CrusadeShopTankBuild(t));
        return new ArrayListIndexPointer<>(this.crusade.crusadeShopBuilds, this.crusade.crusadeShopBuilds.size() - 1).cast();
    }

    @Override
    public void removeTank(TankPlayer t)
    {
        this.crusade.crusadeShopBuilds.remove(t);
    }

    @Override
    public void refreshTanks(TankPlayer t)
    {
        this.refreshButtons();
    }


    public static abstract class CrusadeButton extends Button
    {
        public CrusadeButton(double x, double y, double sX, double sY, String text, Runnable f)
        {
            super(x, y, sX, sY, text, f);
        }
    }

    public static class CrusadeLevelButton extends CrusadeButton
    {
        public Crusade.CrusadeLevel level;

        public CrusadeLevelButton(Crusade c, int li, boolean readOnly)
        {
            super(0, 0, 350, 40, "", () ->
            {
                Game.game.window.validScrollDown = false;
                Game.game.window.validScrollUp = false;

                if (!readOnly)
                {
                    Crusade.CrusadeLevel level = c.levels.remove(li);
                    ScreenCrusadeEditLevel s = new ScreenCrusadeEditLevel(level, li + 1, (ScreenCrusadeEditor) Game.screen);
                    Level l = new Level(level.levelString, level.tanks);
                    l.loadLevel(s);
                    Game.screen = s;
                }
                else
                {
                    String level = c.levels.get(li).levelString;

                    ScreenCrusadePreviewLevel s = new ScreenCrusadePreviewLevel(c, level, li, Game.screen);
                    Level l = new Level(level, c.customTanks);
                    l.loadLevel(s);
                    Game.screen = s;
                }
            });
            this.level = c.levels.get(li);
            this.setText(li + 1);

            this.image = "icons/menu.png";
            this.imageXOffset = - this.sizeX / 2 + this.sizeY / 2 + 10;
            this.imageSizeX = this.sizeY;
            this.imageSizeY = this.sizeY;
        }

        public void setText(int li)
        {
            this.text = li + ". " + level.levelName.replace("_", " ");
        }
    }

    public static class CrusadeItemButton extends CrusadeButton
    {
        public Item.CrusadeShopItem item;

        public CrusadeItemButton(ScreenCrusadeEditor sc, int ii)
        {
            super(0, 0, 350, 40, "", () ->
            {
                try
                {
                    ScreenEditorCrusadeShopItem s = new ScreenEditorCrusadeShopItem(new MonitoredArrayListIndexPointer<>(sc.crusade.crusadeShopItems, ii, false, sc::refreshButtons), Game.screen);
                    s.onComplete = sc::refreshButtons;
                    Game.screen = s;
                }
                catch (NoSuchFieldException e)
                {
                    Game.exitToCrash(e);
                }
            });

            Crusade c = sc.crusade;
            this.text = c.crusadeShopItems.get(ii).itemStack.item.name;
            this.itemIcon = c.crusadeShopItems.get(ii).itemStack.item.icon;
            this.imageXOffset = - this.sizeX / 2 + this.sizeY / 2 + 10;
            this.imageSizeX = this.sizeY;
            this.imageSizeY = this.sizeY;

            this.bgColG = 238;
            this.bgColB = 220;

            int p = c.crusadeShopItems.get(ii).price;

            if (p == 0)
                this.setSubtext("Free!");
            else if (p == 1)
                this.setSubtext("1 coin");
            else
                this.setSubtext("%d coins", p);

            this.item = c.crusadeShopItems.get(ii);
        }
    }

    public static class CrusadeBuildButton extends CrusadeButton
    {
        public TankPlayer.CrusadeShopTankBuild build;

        public CrusadeBuildButton(ScreenCrusadeEditor sc, int ii)
        {
            super(0, 0, 350, 40, "", () ->
            {
                ScreenEditorPlayerTankBuild<TankPlayer.CrusadeShopTankBuild> s = new ScreenEditorPlayerTankBuild<TankPlayer.CrusadeShopTankBuild>(new MonitoredArrayListIndexPointer<>(sc.crusade.crusadeShopBuilds, ii, false, sc::refreshButtons), Game.screen);
                s.onComplete = sc::refreshButtons;
                Game.screen = s;
            });

            Crusade c = sc.crusade;
            this.text = c.crusadeShopBuilds.get(ii).name;

            this.bgColG = 238;
            this.bgColR = 220;

            int p = c.crusadeShopBuilds.get(ii).price;

            if (ii == 0)
                this.setSubtext("Default build");
            else if (p == 0)
                this.setSubtext("Free!");
            else if (p == 1)
                this.setSubtext("1 coin");
            else
                this.setSubtext("%d coins", p);

            this.build = c.crusadeShopBuilds.get(ii);
        }

        public void draw()
        {
            super.draw();

            this.build.drawForInterface(this.posX - this.sizeX / 2 + this.sizeY / 2 + 10, this.posY, 0.5);
        }
    }

    public ScreenCrusadeEditor(Crusade c)
    {
        this(c, false, null);
    }

    public ScreenCrusadeEditor(Crusade c, boolean readOnly, Screen prev)
    {
        super(350, 40, 380, 60);

        this.music = readOnly ? "menu_4.ogg" : "menu_editor.ogg";
        this.musicID = "menu";

        this.allowClose = readOnly;
        this.readOnly = readOnly;
        this.previous = prev;

        if (readOnly)
        {
            this.toggleBuilds.posX += add_offset - hide_offset;
            this.toggleItems.posX += add_offset - hide_offset;
            this.toggleLevels.posX += add_offset - hide_offset;
        }

        this.crusade = c;

        String[] itemNames = new String[Game.registryItem.itemEntries.size()];
        ItemIcon[] itemImages = new ItemIcon[Game.registryItem.itemEntries.size()];

        for (int i = 0; i < Game.registryItem.itemEntries.size(); i++)
        {
            RegistryItem.ItemEntry r = Game.registryItem.getEntry(i);
            itemNames[i] = r.name;
            itemImages[i] = r.icon;
        }

        itemSelector = new Selector(0, 0, 0, 0, "item type", itemNames, () ->
        {
            Consumer<Item.ItemStack<?>> addItem = (Item.ItemStack<?> i) ->
            {
                try
                {
                    this.crusade.crusadeShopItems.add(new Item.CrusadeShopItem(i));
                    ScreenEditorCrusadeShopItem s = new ScreenEditorCrusadeShopItem(new MonitoredArrayListIndexPointer<Item.CrusadeShopItem>(crusade.crusadeShopItems, crusade.crusadeShopItems.size() - 1, false, this::refreshButtons), this);
                    s.onComplete = this::refreshButtons;
                    Game.screen = s;
                }
                catch (NoSuchFieldException e)
                {
                    e.printStackTrace();
                }
            };

            Game.screen = new ScreenAddSavedItem(this, addItem, Game.formatString(itemSelector.options[itemSelector.selectedOption]), Game.registryItem.getEntry(itemSelector.selectedOption).item);
        });

        itemSelector.itemIcons = itemImages;
        itemSelector.quick = true;

        if (Drawing.drawing.interfaceScaleZoom > 1)
        {
            this.buttons = new ButtonList(new ArrayList<>(), 0, 0, 0);
            this.titleOffset = -210;

            this.buttons.controlsYOffset = -30;
        }
        else
        {
            this.buttons = new ButtonList(new ArrayList<>(), 0, 0, -30);
        }

        this.buttons.arrowsEnabled = true;

        this.buttons.reorderBehavior = (i, j) ->
        {
            this.buttons.buttons.add(j, this.buttons.buttons.remove((int)i));
            this.buttons.sortButtons();

            int levels = 0;
            for (Button b: this.buttons.buttons)
            {
                if (b instanceof CrusadeLevelButton)
                {
                    levels++;
                    ((CrusadeLevelButton) b).setText(levels);
                }
            }

            if (this.buttons.buttons.get(i) instanceof CrusadeBuildButton)
            {
                if (i == 0)
                    this.buttons.buttons.get(i).setSubtext("Default build");

                if (j == 1)
                    this.buttons.buttons.get(j).setSubtext("Free!");
            }
        };

        this.refreshButtons();

        addLevel.fullInfo = true;
        addItem.fullInfo = true;
        addBuild.fullInfo = true;

        toggleLevels.fullInfo = true;
        toggleItems.fullInfo = true;
        toggleBuilds.fullInfo = true;

        crusadeName = new TextBox(Drawing.drawing.interfaceSizeX / 2, 75, this.objWidth, this.objHeight, "Crusade name", () ->
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
                , crusade.name.split("\\.")[0].replace("_", " "));

        crusadeName.enableCaps = true;
    }

    public void executeReorder()
    {
        if (showLevels)
            crusade.levels.clear();

        if (showItems)
            crusade.crusadeShopItems.clear();

        if (showBuilds)
            crusade.crusadeShopBuilds.clear();

        int levels = 0;

        for (Button b : buttons.buttons)
        {
            if (b instanceof CrusadeLevelButton)
            {
                crusade.levels.add(((CrusadeLevelButton) b).level);
                levels++;
            }
            else if (b instanceof CrusadeItemButton)
            {
                crusade.crusadeShopItems.add(((CrusadeItemButton) b).item);
                ((CrusadeItemButton) b).item.levelUnlock = levels;
            }
            else if (b instanceof CrusadeBuildButton)
            {
                crusade.crusadeShopBuilds.add(((CrusadeBuildButton) b).build);
                ((CrusadeBuildButton) b).build.levelUnlock = levels;
            }
        }

        refreshButtons();
    }

    protected void setIcon(Button b, boolean shown)
    {
        int imgSize = 30;
        b.imageSizeX = imgSize;
        b.imageSizeY = imgSize;
        b.image = shown ? "icons/shown.png" : "icons/hidden.png";
    }

    public void refreshButtons()
    {
        toggleLevels.setHoverText("Levels: " + (showLevels ? shownText : hiddenText));
        toggleItems.setHoverText("Items: " + (showItems ? shownText : hiddenText));
        toggleBuilds.setHoverText("Builds: " + (showBuilds ? shownText : hiddenText));
        setIcon(toggleLevels, showLevels);
        setIcon(toggleItems, showItems);
        setIcon(toggleBuilds, showBuilds);

        this.buttons.buttons.clear();
        this.crusade.crusadeShopItems.sort(Comparator.comparingInt(o -> o.levelUnlock));
        this.crusade.crusadeShopBuilds.sort(Comparator.comparingInt(o -> o.levelUnlock));

        int j = 0;
        int k = 1;

        this.buttons.buttons.add(new CrusadeBuildButton(this, 0));

        for (int i = 0; i < this.crusade.levels.size(); i++)
        {
            for (; j < this.crusade.crusadeShopItems.size(); j++)
            {
                Item.CrusadeShopItem s = this.crusade.crusadeShopItems.get(j);
                if (s.levelUnlock == i)
                    this.buttons.buttons.add(new CrusadeItemButton(this, j));
                else
                    break;
            }

            for (; k < this.crusade.crusadeShopBuilds.size(); k++)
            {
                TankPlayer.CrusadeShopTankBuild s = this.crusade.crusadeShopBuilds.get(k);
                if (s.levelUnlock == i)
                    this.buttons.buttons.add(new CrusadeBuildButton(this, k));
                else
                    break;
            }

            this.buttons.buttons.add(new CrusadeLevelButton(this.crusade, i, readOnly));
        }

        for (; j < this.crusade.crusadeShopItems.size(); j++)
        {
            this.buttons.buttons.add(new CrusadeItemButton(this, j));
        }

        for (; k < this.crusade.crusadeShopBuilds.size(); k++)
        {
            this.buttons.buttons.add(new CrusadeBuildButton(this, k));
        }

        for (int i = 0; i < this.buttons.buttons.size(); i++)
        {
            Button b = this.buttons.buttons.get(i);
            if ((b instanceof CrusadeLevelButton && !this.showLevels) ||
                    (b instanceof CrusadeBuildButton && !this.showBuilds) ||
                    (b instanceof CrusadeItemButton && !this.showItems))
            {
                this.buttons.buttons.remove(i);
                i--;
            }
            else if (!(b instanceof CrusadeLevelButton) && readOnly)
                b.enabled = false;
        }

        this.buttons.sortButtons();
    }

    @Override
    public void update()
    {
        buttons.fixedFirstElements = showBuilds ? 1 : 0;

        if (showBuilds && buttons.buttons.size() > 1)
        {
            Button b = buttons.buttons.get(1);
            if (b instanceof CrusadeBuildButton)
            {
                CrusadeBuildButton cb = (CrusadeBuildButton) b;
                if (cb.build.price == 0)
                    buttons.fixedFirstElements = 0;
            }
        }

        buttons.update();

        quit.update();

        if (!readOnly)
        {
            addLevel.update();
            addItem.update();
            addBuild.update();

            options.update();

            if (showLevels)
                reorder.update();

            crusadeName.update();
        }

        toggleLevels.update();
        toggleItems.update();
        toggleBuilds.update();
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

        if (showLevels && !readOnly)
            reorder.draw();

        if (readOnly)
            quit.setText("Back");

        quit.draw();

        if (this.buttons.buttons.size() <= 0)
        {
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.setColor(0, 0, 0);


            String[] missing = new String[3];

            int m = 0;
            if (showLevels)
            {
                missing[m] = "levels";
                m++;
            }

            if (showItems)
            {
                missing[m] = "items";
                m++;
            }

            if (showBuilds)
            {
                missing[m] = "builds";
                m++;
            }

            double o = readOnly ? 0 : -30;

            if (m == 0)
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY, "Everything is currently hidden!");
            else if (m == 1)
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + o, "There are no %s in the crusade.", missing[0]);
            else if (m == 2)
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + o, "There are no %s or %s in the crusade.", missing[0], missing[1]);
            else if (m == 3)
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + o, "There are no %s, %s, or %s in the crusade.", missing[0], missing[1], missing[2]);

            if (m > 0 && !readOnly)
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + 30, "Use the '+' buttons at the top to add some!");
        }

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawConcentricPopup(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace, Drawing.drawing.interfaceSizeY / 2 + titleOffset, this.objWidth, this.objHeight * 1.25, 5, this.objHeight * 0.625);
        Drawing.drawing.drawConcentricPopup(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + titleOffset, this.objWidth, this.objHeight * 1.25, 5, this.objHeight * 0.625);
        Drawing.drawing.drawConcentricPopup(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace, Drawing.drawing.interfaceSizeY / 2 + titleOffset, this.objWidth, this.objHeight * 1.25, 5, this.objHeight * 0.625);

        buttons.draw();

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace + label_offset, Drawing.drawing.interfaceSizeY / 2 + titleOffset, "Levels (%d)", crusade.levels.size());
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 + label_offset, Drawing.drawing.interfaceSizeY / 2 + titleOffset, "Items (%d)", crusade.crusadeShopItems.size());
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace + label_offset, Drawing.drawing.interfaceSizeY / 2 + titleOffset, "Builds (%d)", crusade.crusadeShopBuilds.size());

        Drawing.drawing.drawInterfaceImage("icons/menu.png", Drawing.drawing.interfaceSizeX / 2 - this.objXSpace + icon_offset, Drawing.drawing.interfaceSizeY / 2 + titleOffset, objHeight, objHeight);
        Drawing.drawing.drawInterfaceImage("item.png", Drawing.drawing.interfaceSizeX / 2 + icon_offset, Drawing.drawing.interfaceSizeY / 2 + titleOffset, objHeight, objHeight);
        Drawing.drawing.drawInterfaceImage("tank_builds.png", Drawing.drawing.interfaceSizeX / 2 + this.objXSpace + icon_offset, Drawing.drawing.interfaceSizeY / 2 + titleOffset, objHeight, objHeight);

        if (!readOnly)
        {
            addLevel.draw();
            addItem.draw();
            addBuild.draw();
            options.draw();
            crusadeName.draw();
        }
        else
        {
            Drawing.drawing.setInterfaceFontSize(this.textSize * 2);
            double w1 = Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, this.crusade.name.replace("_", " ")) / Drawing.drawing.interfaceScale;
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            double w2 = Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, "Bonus life frequency: " + this.crusade.bonusLifeFrequency) / Drawing.drawing.interfaceScale;
            double o = this.objYSpace;
            double w = w1 + w2 + o;

            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.setInterfaceFontSize(this.textSize * 2);
            Drawing.drawing.drawInterfaceText(crusadeName.posX - w / 2, crusadeName.posY - this.objHeight / 2, this.crusade.name.replace("_", " "), false);
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(crusadeName.posX - w / 2 + w1 + o, crusadeName.posY - this.objHeight / 2 - 15, Translation.translate("Starting lives: %d", this.crusade.startingLives), false);
            Drawing.drawing.drawInterfaceText(crusadeName.posX - w / 2 + w1 + o, crusadeName.posY - this.objHeight / 2 + 15, Translation.translate("Bonus life frequency: %d", this.crusade.bonusLifeFrequency), false);
        }

        toggleLevels.draw();
        toggleItems.draw();
        toggleBuilds.draw();
    }

    public void save()
    {
        BaseFile f = Game.game.fileManager.getFile(crusade.fileName);

        try
        {
            f.startWriting();
            f.println("properties");
            f.println(this.crusade.startingLives + "," + this.crusade.bonusLifeFrequency + "," + this.crusade.showNames + "," + this.crusade.respawnTanks);
            f.println("items");

            for (Item.CrusadeShopItem i: this.crusade.crusadeShopItems)
                f.println(i.toString());

            f.println("tanks");
            HashMap<String, TankAIControlled> customTanks = new HashMap<>();
            HashMap<String, ArrayList<Integer>> customTankLevels = new HashMap<>();

            for (int i = 0; i < this.crusade.levels.size(); i++)
            {
                ArrayList<TankAIControlled> tanks = this.crusade.levels.get(i).tanks;

                for (TankAIControlled t: tanks)
                {
                    customTanks.put(t.name, t);
                    ArrayList<Integer> a = customTankLevels.get(t.name);

                    if (a == null)
                        a = new ArrayList<>();

                    a.add(i);
                    customTankLevels.put(t.name, a);
                }
            }

            for (String s: customTanks.keySet())
            {
                f.println(customTankLevels.get(s) + " " + customTanks.get(s));
            }

            f.println("builds");

            for (TankPlayer.CrusadeShopTankBuild b: this.crusade.crusadeShopBuilds)
            {
                f.println(b.toString());
            }

            f.println("levels");

            for (int i = 0; i < this.crusade.levels.size(); i++)
            {
                String l = this.crusade.levels.get(i).levelString;
                f.println(l.substring(l.indexOf('{'), l.indexOf('}') + 1) + " name=" + this.crusade.levels.get(i).levelName);
            }

            f.stopWriting();
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    @Override
    public void onAttemptClose()
    {
        Game.screen = new ScreenConfirmSaveCrusade(Game.screen, this);
    }
}
