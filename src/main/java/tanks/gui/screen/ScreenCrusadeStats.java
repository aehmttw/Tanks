package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.Selector;
import tanks.gui.SpeedrunTimer;
import tanks.hotbar.item.Item;
import tanks.registry.RegistryTank;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;

import java.util.ArrayList;

public class ScreenCrusadeStats extends Screen implements IDarkScreen, IHiddenChatboxScreen
{
    public enum View {tanks, levels, items, misc};
    public View view = View.tanks;

    public Crusade crusade;

    public long musicStartTime;
    public double age;
    public boolean musicStarted = false;

    public long lastEntryTime = System.currentTimeMillis() + 500;
    public int tankEntriesShown = -1;
    public int levelEntriesShown = -1;
    public int itemEntriesShown = -1;
    public int miscEntriesShown = -1;

    public static int page_size = 15;
    public int tankPage = 0;
    public int levelPage = 0;
    public int itemPage = 0;
    public int miscPage = 0;

    public ArrayList<Entry> tanks = new ArrayList<>();
    public ArrayList<Entry> levels = new ArrayList<>();
    public ArrayList<Entry> items = new ArrayList<>();
    public ArrayList<Entry> misc = new ArrayList<>();

    public static final int tanks_1 = -400;
    public static final int tanks_2 = -200;
    public static final int tanks_2a = -50;
    public static final int tanks_3 = 0;
    public static final int tanks_3a = 100;
    public static final int tanks_4 = 200;
    public static final int tanks_5 = 400;

    public static final int levels_1 = -400;
    public static final int levels_2 = -133;
    public static final int levels_3 = 133;
    public static final int levels_4 = 400;

    public static final int items_1 = -400;
    public static final int items_2 = -133;
    public static final int items_3 = 133;
    public static final int items_4 = 400;

    public static final int misc_1 = -150;
    public static final int misc_2 = 150;

    public double tankTimer = 0;
    public int tankType = 0;
    public int tankCount = -1;
    public ArrayList<Tank> rollingTanks = new ArrayList<>();

    public int totalKills = 0;
    public int totalCoins = 0;
    public int totalDeaths = 0;

    public double totalBestTimes = 0;
    public int totalAttempts = 0;

    public boolean wizardFinished = false;

    public CrusadePlayer player;

    public ScreenCrusadeStats(Crusade crusade, CrusadePlayer p, boolean intro)
    {
        super(350, 40, 250, 60);

        if (Drawing.drawing.interfaceScaleZoom > 1)
            page_size = 9;

        this.player = p;
        this.crusade = crusade;

        this.addTanks();
        this.addLevels();
        this.addItems();
        this.addMisc();

        if (intro)
        {
            this.musicStartTime = Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/results_intro_length.txt").get(0));
            Drawing.drawing.playSound("results_intro.ogg", 1f, true);
        }
        else
        {
            this.music = "results.ogg";
            this.musicStarted = true;

            this.tankEntriesShown = this.tanks.size() + 1;
            this.levelEntriesShown = this.levels.size() + 1;
            this.itemEntriesShown = this.items.size() + 1;
            this.miscEntriesShown = this.misc.size() + 1;
            this.wizardFinished = true;
        }

        if (!Game.fullStats)
        {
            this.exit.posY -= 30;
            this.view = View.misc;
            this.wizardFinished = true;
        }

        ArrayList<CrusadePlayer> players = new ArrayList<>();
        players.addAll(Crusade.currentCrusade.crusadePlayers.values());
        players.addAll(Crusade.currentCrusade.disconnectedPlayers);

        String[] playerNames = new String[players.size()];
        CrusadePlayer[] playerObjects = new CrusadePlayer[playerNames.length];

        int us = 0;

        int i = 0;
        for (CrusadePlayer pl: players)
        {
            playerNames[i] = pl.player.username;
            playerObjects[i] = pl;

            if (pl.player.clientID.equals(p.player.clientID))
                us = i;

            i++;
        }

        changePlayer = new Selector(this.centerX + 200, 80, this.objWidth, this.objHeight, "Player", playerNames, () ->
        {
            Game.screen = new ScreenCrusadeStats(crusade, playerObjects[changePlayer.selectedOption], false);
            ((ScreenCrusadeStats)Game.screen).view = view;
        });

        changePlayer.selectedOption = us;

        changePlayer.quick = true;

        if (ScreenPartyLobby.isClient || ScreenPartyHost.isServer)
            exit.setText("Back to party");
    }

    public void addTanks()
    {
        for (RegistryTank.TankEntry t: Game.registryTank.tankEntries)
        {
            Integer kills = this.player.tankKills.get(t.name);
            Integer deaths = this.player.tankDeaths.get(t.name);

            if (kills == null && deaths == null)
                continue;

            if (kills == null)
                kills = 0;

            if (deaths == null)
                deaths = 0;

            this.tanks.add(new TankEntry(this, t, kills, deaths));
        }

        Integer kills = this.player.tankKills.get("player");
        Integer deaths = this.player.tankDeaths.get("player");

        if (kills == null && deaths == null)
            return;

        if (kills == null)
            kills = 0;

        if (deaths == null)
            deaths = 0;

        this.tanks.add(new TankEntry(this, kills, deaths));

        if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
            exit.setText("Back to party");
    }

    public void addLevels()
    {
        for (Crusade.LevelPerformance l: crusade.performances)
        {
            String name = "Battle " + (l.index + 1);

            if (crusade.showNames)
                name = (l.index + 1 + ". " + crusade.levelNames.get(l.index).replace("_", " "));

            this.levels.add(new LevelEntry(name, l));

            this.totalAttempts += l.attempts;

            if (l.bestTime < Double.MAX_VALUE)
                this.totalBestTimes += l.bestTime;
        }
    }

    public void addItems()
    {
        this.addItem(TankPlayer.default_bullet);
        this.addItem(TankPlayer.default_mine);

        for (Item i: crusade.getShop())
        {
            this.addItem(i);
        }
    }

    public void addMisc()
    {
        this.misc.add(new MiscEntry("Battles cleared", this.crusade.currentLevel + ""));
        this.misc.add(new MiscEntry("Lives remaining", this.player.player.remainingLives + ""));
        this.misc.add(new MiscEntry("Total kills", this.totalKills + ""));
        this.misc.add(new MiscEntry("Total deaths", this.totalDeaths + ""));

        if (this.totalDeaths > 0)
            this.misc.add(new MiscEntry("Kill to death ratio", (this.totalKills * 100 / this.totalDeaths) / 100.0 + ""));

        this.misc.add(new MiscEntry("Time elapsed", SpeedrunTimer.getTime(this.crusade.timePassed)));
        this.misc.add(new MiscEntry("Coins spent", this.totalCoins - this.player.coins + ""));
        this.misc.add(new MiscEntry("Coins remaining", this.player.coins + ""));
    }

    public void addItem(Item i)
    {
        int uses = this.player.getItemUses(i.name);
        int hits = this.player.getItemHits(i.name);

        if (uses > 0 || hits > 0)
            this.items.add(new ItemEntry(i, uses, hits));
    }

    Button exit = new Button(this.centerX, Drawing.drawing.interfaceSizeY - 35, this.objWidth, this.objHeight, "Return to title", () ->
    {
        Crusade.crusadeMode = false;
        Crusade.currentCrusade = null;

        if (ScreenPartyLobby.isClient)
            Game.screen = new ScreenPartyLobby();
        else if (ScreenPartyHost.isServer)
            Game.screen = ScreenPartyHost.activeScreen;
        else
            Game.exitToTitle();
    }
    );

    Selector changePlayer;


    Button viewTanks = new Button(this.centerX - this.objXSpace * 1.5, Drawing.drawing.interfaceSizeY - 90, this.objWidth * 0.65, this.objHeight, "Tanks", () -> view = View.tanks
    );

    Button viewLevels = new Button(this.centerX - this.objXSpace * 0.5, Drawing.drawing.interfaceSizeY - 90, this.objWidth * 0.65, this.objHeight, "Battles", () -> view = View.levels
    );

    Button viewItems = new Button(this.centerX + this.objXSpace * 0.5, Drawing.drawing.interfaceSizeY - 90, this.objWidth * 0.65, this.objHeight, "Items", () -> view = View.items
    );

    Button viewMisc = new Button(this.centerX + this.objXSpace * 1.5, Drawing.drawing.interfaceSizeY - 90, this.objWidth * 0.65, this.objHeight, "Summary", () -> view = View.misc
    );

    Button next = new Button(this.centerX, Drawing.drawing.interfaceSizeY - 35, this.objWidth, this.objHeight, "Next", () ->
    {
        if (view == View.tanks)
        {
            if (tankEntriesShown > tanks.size())
                view = View.levels;
            else
                tankPage++;
        }
        else if (view == View.levels)
        {
            if (levelEntriesShown > levels.size())
                view = View.items;
            else
                levelPage++;
        }
        else if (view == View.items)
        {
            if (itemEntriesShown > items.size())
                view = View.misc;
            else
                itemPage++;
        }

        if (view == View.misc)
            wizardFinished = true;
    }
    );

    Button nextPage = new Button(this.centerX, 0, 500, 30, "Next page", () ->
    {
        if (view == View.tanks)
            tankPage++;
        else if (view == View.levels)
            levelPage++;
        else if (view == View.items)
            itemPage++;
        else if (view == View.misc)
            miscPage++;
    }
    );

    Button previousPage = new Button(this.centerX, 0, 500, 30, "Previous page", () ->
    {
        if (view == View.tanks)
            tankPage--;
        else if (view == View.levels)
            levelPage--;
        else if (view == View.items)
            itemPage--;
        else if (view == View.misc)
            miscPage--;
    }
    );

    @Override
    public void update()
    {
        Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);

        if (!musicStarted)
        {
            this.age += Panel.frameFrequency;

            if (this.age >= this.musicStartTime / 10.0)
            {
                musicStarted = true;
                this.music = "results.ogg";
                Panel.forceRefreshMusic = true;
            }
        }

        if ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && wizardFinished)
            changePlayer.update();

        if (this.view == View.tanks)
        {
            this.updateShownEntries(this.tankEntriesShown, this.tanks.size(), this.tankPage);
            this.updatePageButtons(this.tankEntriesShown, this.tanks.size(), this.tankPage);
        }
        else if (this.view == View.levels)
        {
            this.updateShownEntries(this.levelEntriesShown, this.levels.size(), this.levelPage);
            this.updatePageButtons(this.levelEntriesShown, this.levels.size(), this.levelPage);
        }
        else if (this.view == View.items)
        {
            this.updateShownEntries(this.itemEntriesShown, this.items.size(), this.itemPage);
            this.updatePageButtons(this.itemEntriesShown, this.items.size(), this.itemPage);
        }
        else if (this.view == View.misc)
        {
            this.updateShownEntries(this.miscEntriesShown, this.misc.size(), this.miscPage);
            this.updatePageButtons(this.miscEntriesShown, this.misc.size(), this.miscPage);

            if (this.tankTimer <= 0 && this.totalKills > 0)
            {
                if (this.tankCount < 0)
                    this.tankCount = ((TankEntry) this.tanks.get(this.tankType)).kills - 1;

                Tank t = Game.registryTank.getEntry(((TankEntry) this.tanks.get(this.tankType)).tank.name).getTank(Drawing.drawing.sizeX * 1.4, Drawing.drawing.sizeY - 200, Math.PI);

                if (((TankEntry) this.tanks.get(this.tankType)).tank.name.equals("player"))
                    t = new TankPlayer(t.posX, t.posY, t.angle);

                t.posX += 5 * this.tankTimer;

                this.rollingTanks.add(t);

                boolean skip = false;

                if (this.tankCount <= 0)
                {
                    this.tankType++;

                    if (this.tankType >= this.tanks.size())
                    {
                        this.tankType = 0;
                        this.tankTimer += 250;
                        skip = true;
                    }

                    this.tankCount = ((TankEntry) this.tanks.get(this.tankType)).kills;
                }

                this.tankCount--;

                if (!skip)
                {
                    Tank next = Game.registryTank.getEntry(((TankEntry) this.tanks.get(this.tankType)).tank.name).getTank(0, 0, 0);
                    this.tankTimer += Math.max(t.size, next.size) / 2.5;
                }
            }

            this.tankTimer -= Panel.frameFrequency;

            ArrayList<Tank> removeTanks = new ArrayList<>();
            for (Tank t: this.rollingTanks)
            {
                if (t.posX < Drawing.drawing.sizeX / 2)
                    t.drawAge -= Panel.frameFrequency * 2;

                if (t.posX < -Drawing.drawing.sizeX * 0.4)
                    removeTanks.add(t);

                t.posX -= 5 * Panel.frameFrequency;
            }

            this.rollingTanks.removeAll(removeTanks);
        }

        if (!wizardFinished)
            next.update();
        else
            exit.update();

        if (wizardFinished && Game.fullStats)
        {
            viewTanks.enabled = view != View.tanks;
            viewLevels.enabled = view != View.levels;
            viewItems.enabled = view != View.items;
            viewMisc.enabled = view != View.misc;

            viewTanks.update();
            viewLevels.update();
            viewItems.update();
            viewMisc.update();
        }
    }

    public void updatePageButtons(int shown, int size, int page)
    {
        if (size > page_size && shown >= Math.min((page + 1) * page_size, size))
        {
            if ((page + 1) * page_size < size && shown >= Math.min((page + 1) * page_size, size))
                nextPage.update();

            if (page > 0 && shown >= 0)
                previousPage.update();
        }

        next.enabled = shown > Math.min((page + 1) * page_size - 1, size);
    }

    public void updateShownEntries(int entriesShown, int size, int page)
    {
        if (this.view == View.misc && entriesShown < 0)
            entriesShown = 0;

        if (System.currentTimeMillis() - lastEntryTime > 100 && entriesShown < Math.min((page + 1) * page_size, size))
        {
            this.increaseEntriesShown();
            lastEntryTime = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() - lastEntryTime > 500 && entriesShown == size && view != View.misc)
            this.increaseEntriesShown();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        this.drawTopBar(Math.min(120, this.age * 10));
        this.drawBottomBar(Math.min(120, this.age * 10));

        Drawing.drawing.setColor(255, 255, 255);

        double offset = 0;

        if ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && wizardFinished)
            offset = -200;

        Drawing.drawing.setInterfaceFontSize(this.titleSize * 1.2);

        if (this.crusade.internal)
            Drawing.drawing.displayInterfaceText(this.centerX + offset, 40, this.crusade.name.replace("_", " "));
        else
            Drawing.drawing.drawInterfaceText(this.centerX + offset, 40, this.crusade.name.replace("_", " "));

        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        this.nextPage.image = "vertical_arrow.png";
        this.nextPage.imageSizeX = 15;
        this.nextPage.imageSizeY = -15;
        this.nextPage.imageXOffset = -225;

        this.previousPage.image = "vertical_arrow.png";
        this.previousPage.imageSizeX = 15;
        this.previousPage.imageSizeY = 15;
        this.previousPage.imageXOffset = -225;

        if (this.age > 12 || this.wizardFinished)
            Drawing.drawing.displayInterfaceText(this.centerX + offset, 80, "statistics");

        if ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && this.wizardFinished)
        {
            changePlayer.draw();
        }

        if (this.view == View.tanks)
        {
            int entries = this.tanks.size();

            int entriesOnPage = Math.min(entries, page_size);
            double aboveY = Game.screen.centerY + (-1 - ((entriesOnPage - 1.0) / 2.0)) * 30 - 10;
            double belowY = Game.screen.centerY + (entriesOnPage - ((entriesOnPage - 1.0) / 2.0)) * 30 + 10;

            if (entries > page_size)
            {
                aboveY -= 50;
                belowY += 50;
            }

            this.nextPage.posY = belowY - 50;
            this.previousPage.posY = aboveY + 50;

            if (tankEntriesShown >= 0)
            {
                drawBar(aboveY, 50, 127);
                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.setInterfaceFontSize(24);
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + tanks_1, aboveY, "Tank");
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + tanks_2, aboveY, "Kills");
                Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_2a, aboveY, "x");
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + tanks_3, aboveY, "Coins");
                Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_3a, aboveY, "->");
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + tanks_4, aboveY, "Total coins");
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + tanks_5, aboveY, "Deaths");
            }

            if (tankEntriesShown > this.tanks.size())
            {
                drawBar(belowY, 50, 127);
                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.setInterfaceFontSize(32);
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + tanks_1, belowY, "Total");
                Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_2, belowY, this.totalKills + "");
                Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_4, belowY, this.totalCoins + "");
                Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_5, belowY, this.totalDeaths + "");
            }

            this.drawPageEntries(this.tankEntriesShown, this.tanks.size(), this.tankPage, this.tanks);
        }
        else if (this.view == View.levels)
        {
            int entries = this.levels.size();

            int entriesOnPage = Math.min(entries, page_size);
            double aboveY = Game.screen.centerY + (-1 - ((entriesOnPage - 1.0) / 2.0)) * 30 - 10;
            double belowY = Game.screen.centerY + (entriesOnPage - ((entriesOnPage - 1.0) / 2.0)) * 30 + 10;

            if (entries > page_size)
            {
                aboveY -= 50;
                belowY += 50;
            }

            this.nextPage.posY = belowY - 50;
            this.previousPage.posY = aboveY + 50;

            if (levelEntriesShown >= 0)
            {
                drawBar(aboveY, 50, 127);
                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.setInterfaceFontSize(24);
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + levels_1, aboveY, "Battle");
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + levels_2, aboveY, "Attempts");
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + levels_3, aboveY, "Clear time");
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + levels_4, aboveY, "Total time");
            }

            if (levelEntriesShown > this.levels.size())
            {
                drawBar(belowY, 50, 127);
                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.setInterfaceFontSize(32);
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + levels_1, belowY, "Total");
                Drawing.drawing.drawInterfaceText(Game.screen.centerX + levels_2, belowY, this.totalAttempts + "");
                Drawing.drawing.drawInterfaceText(Game.screen.centerX + levels_3, belowY, SpeedrunTimer.getTime(this.totalBestTimes));
                Drawing.drawing.drawInterfaceText(Game.screen.centerX + levels_4, belowY, SpeedrunTimer.getTime(crusade.timePassed));
            }

            this.drawPageEntries(this.levelEntriesShown, this.levels.size(), this.levelPage, this.levels);
        }
        else if (this.view == View.items)
        {
            int entries = this.items.size();

            int entriesOnPage = Math.min(entries, page_size);
            double aboveY = Game.screen.centerY + (-1 - ((entriesOnPage - 1.0) / 2.0)) * 30 - 10;
            double belowY = Game.screen.centerY + (entriesOnPage - ((entriesOnPage - 1.0) / 2.0)) * 30 + 10;

            if (entries > page_size)
            {
                aboveY -= 50;
                belowY += 50;
            }

            this.nextPage.posY = belowY - 50;
            this.previousPage.posY = aboveY + 50;

            if (itemEntriesShown >= 0)
            {
                drawBar(aboveY, 50, 127);
                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.setInterfaceFontSize(24);
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + items_1, aboveY, "Item");
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + items_2, aboveY, "Times used");
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + items_3, aboveY, "Hits landed");
                Drawing.drawing.displayInterfaceText(Game.screen.centerX + items_4, aboveY, "Accuracy");
            }

            if (itemEntriesShown > this.items.size())
                drawBar(belowY, 50, 127);

            this.drawPageEntries(this.itemEntriesShown, this.items.size(), this.itemPage, this.items);
        }
        else if (this.view == View.misc)
        {
            this.drawPageEntries(this.miscEntriesShown, this.misc.size(), this.miscPage, this.misc);

            for (Tank t: this.rollingTanks)
            {
               t.draw();
            }
        }

        if (!wizardFinished)
            next.draw();
        else
            exit.draw();

        Drawing.drawing.setColor(255, 255, 255, 127);
        Drawing.drawing.setInterfaceFontSize(32);

        int o = 90;
        double s = 250;

        if (Game.fullStats)
        {
            if (wizardFinished)
            {
                viewTanks.draw();
                viewLevels.draw();
                viewItems.draw();
                viewMisc.draw();
            }
            else
            {
                setWizardStyle(this.view == View.tanks);
                Drawing.drawing.displayInterfaceText(this.centerX - s * 1.5, Drawing.drawing.interfaceSizeY - o, "Tanks");
                setWizardStyle(this.view == View.levels);
                Drawing.drawing.displayInterfaceText(this.centerX - s * 0.5, Drawing.drawing.interfaceSizeY - o, "Battles");
                setWizardStyle(this.view == View.items);
                Drawing.drawing.displayInterfaceText(this.centerX + s * 0.5, Drawing.drawing.interfaceSizeY - o, "Items");
                setWizardStyle(this.view == View.misc);
                Drawing.drawing.displayInterfaceText(this.centerX + s * 1.5, Drawing.drawing.interfaceSizeY - o, "Summary");

                setWizardStyle(false);
                Drawing.drawing.drawInterfaceText(this.centerX - s * 1, Drawing.drawing.interfaceSizeY - o, ">");
                Drawing.drawing.drawInterfaceText(this.centerX - s * 0, Drawing.drawing.interfaceSizeY - o, ">");
                Drawing.drawing.drawInterfaceText(this.centerX + s * 1, Drawing.drawing.interfaceSizeY - o, ">");
            }
        }
    }

    public void drawPageEntries(int shown, int size, int page, ArrayList<Entry> entries)
    {
        if (size > page_size)
        {
            if ((page + 1) * page_size < size && shown >= Math.min((page + 1) * page_size, size))
                nextPage.draw();

            if (page > 0 && shown >= 0)
                previousPage.draw();
        }

        for (int i = page_size * page; i < Math.min(size, page_size * (page + 1)); i++)
        {
            if (i < shown)
                entries.get(i).draw(i, size, page_size);
        }
    }

    public void increaseEntriesShown()
    {
        Drawing.drawing.playSound("boost.ogg", 2f);

        if (view == View.tanks)
            tankEntriesShown++;
        else if (view == View.levels)
            levelEntriesShown++;
        else if (view == View.items)
            itemEntriesShown++;
        else if (view == View.misc)
            miscEntriesShown++;
    }

    public void setWizardStyle(boolean opaque)
    {
        if (opaque)
        {
            Drawing.drawing.setInterfaceFontSize(32);
            Drawing.drawing.setColor(255, 255, 255);
        }
        else
        {
            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.setColor(255, 255, 255, 127);
        }
    }

    public void drawTopBar(double height)
    {
        double extraHeight = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2;
        double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, -extraHeight / 2, width, extraHeight);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, height / 2, width, height);
    }

    public void drawBottomBar(double height)
    {
        double extraHeight = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2;
        double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY + extraHeight / 2, width, extraHeight);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - height / 2, width, height);
    }

    public void drawBar(double ypos, double height, double opacity)
    {
        double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

        Drawing.drawing.setColor(0, 0, 0, opacity);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, ypos, width, height);
    }

    public static class Entry
    {
        public double yPos = 0;

        public double age = 0;
        public double maxAge = 10;

        public void draw(int num, int count, int pageSize)
        {
            this.age = Math.min(this.age + Panel.frameFrequency, this.maxAge);

            int numOnPage = num % pageSize;
            int entriesOnPage = Math.min(pageSize, count);
            this.yPos = Game.screen.centerY + (numOnPage - ((entriesOnPage - 1.0) / 2.0)) * 30;
        }

        public void drawBar(double ypos, double height, double opacity)
        {
            double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

            Drawing.drawing.setColor(0, 0, 0, opacity * age / maxAge);
            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, ypos, width, height * age / maxAge);
        }

        public double getXOffset()
        {
            return -50 * (1 - age / maxAge);
        }
    }

    public static class TankEntry extends Entry
    {
        public Tank tank;
        public int kills;
        public int coins;
        public int deaths;

        public TankEntry(ScreenCrusadeStats screen, RegistryTank.TankEntry tank, int kills, int deaths)
        {
            this.kills = kills;
            this.deaths = deaths;

            this.tank = tank.getTank(0, 0, 0);
            this.coins = this.tank.coinValue;

            screen.totalKills += this.kills;
            screen.totalCoins += this.kills * this.coins;
            screen.totalDeaths += this.deaths;
        }

        public TankEntry(ScreenCrusadeStats screen, int kills, int deaths)
        {
            this.kills = kills;
            this.deaths = deaths;

            this.tank = new TankPlayer(0, 0, 0);
            this.coins = this.tank.coinValue;

            screen.totalKills += this.kills;
            screen.totalCoins += this.kills * this.coins;
            screen.totalDeaths += this.deaths;
        }

        @Override
        public void draw(int num, int count, int pageSize)
        {
            super.draw(num, count, pageSize);

            if (num % 2 == 0)
                this.drawBar(this.yPos, 30, 64);
            else
                this.drawBar(this.yPos, 30, 32);

            Drawing.drawing.setInterfaceFontSize(24);
            this.tank.drawForInterface(this.getXOffset() + Game.screen.centerX + tanks_1, this.yPos, 0.5 * age / maxAge);
            Drawing.drawing.setColor(processColor(this.tank.colorR), processColor(this.tank.colorG), processColor(this.tank.colorB), 255 * age / maxAge);
            Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + tanks_2, this.yPos, this.kills + "");
            Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + tanks_2a, this.yPos, "x");
            Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + tanks_3, this.yPos, this.coins + "");
            Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + tanks_3a, this.yPos, "->");
            Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + tanks_4, this.yPos, this.coins * this.kills + "");
            Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + tanks_5, this.yPos, this.deaths + "");
        }
    }

    public static class LevelEntry extends Entry
    {
        public Crusade.LevelPerformance level;
        public String name;

        public LevelEntry(String name, Crusade.LevelPerformance levelPerformance)
        {
            this.level = levelPerformance;
            this.name = name;
        }

        @Override
        public void draw(int num, int count, int pageSize)
        {
            super.draw(num, count, pageSize);

            if (num % 2 == 0)
                this.drawBar(this.yPos, 30, 64);
            else
                this.drawBar(this.yPos, 30, 32);

            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.setColor(255, 255, 255, 255 * age / maxAge);

            if (name.contains(". "))
                Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + levels_1 - 110, this.yPos, this.name, false);
            else
                Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + levels_1, this.yPos, this.name);

            if (this.level.attempts == 1 && level.bestTime < Double.MAX_VALUE)
                Drawing.drawing.setColor(127, 255, 127, 255 * age / maxAge);

            Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + levels_2, this.yPos, this.level.attempts + "");

            Drawing.drawing.setColor(255, 255, 255, 255 * age / maxAge);

            if (this.level.bestTime >= Double.MAX_VALUE)
            {
                Drawing.drawing.setColor(255, 255, 255, 127 * age / maxAge);
                Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + levels_3, this.yPos, "-", false);
            }
            else
                Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + levels_3 + 52, this.yPos, SpeedrunTimer.getTime(this.level.bestTime), true);

            if (this.level.attempts == 1)
                Drawing.drawing.setColor(255, 255, 255, 127 * age / maxAge);
            else
                Drawing.drawing.setColor(255, 255, 255, 255 * age / maxAge);

            Drawing.drawing.drawInterfaceText( this.getXOffset() + Game.screen.centerX + levels_4 + 52, this.yPos, SpeedrunTimer.getTime(this.level.totalTime), true);
        }
    }

    public static class ItemEntry extends Entry
    {
        public Item item;
        public int uses;
        public int hits;

        public ItemEntry(Item i, int uses, int hits)
        {
            this.item = i;
            this.uses = uses;
            this.hits = hits;
        }

        @Override
        public void draw(int num, int count, int pageSize)
        {
            super.draw(num, count, pageSize);

            if (num % 2 == 0)
                this.drawBar(this.yPos, 30, 64);
            else
                this.drawBar(this.yPos, 30, 32);

            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.setColor(255, 255, 255, 255 * age / maxAge);
            Drawing.drawing.drawInterfaceImage(this.item.icon, this.getXOffset() + Game.screen.centerX + items_1 - 140, this.yPos, 30, 30);
            Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + items_1 - 105, this.yPos, this.item.name, false);
            Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + items_2, this.yPos, this.uses + "");

            if (!this.item.supportsHits)
            {
                Drawing.drawing.setColor(255, 255, 255, 127 * age / maxAge);
                Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + items_3, this.yPos, "-");
            }
            else
                Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + items_3, this.yPos, this.hits + "");

            if (this.uses <= 0 || !this.item.supportsHits)
            {
                Drawing.drawing.setColor(255, 255, 255, 127 * age / maxAge);
                Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + items_4, this.yPos, "-");
            }
            else
                Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + items_4, this.yPos, (this.hits * 1000 / this.uses) / 10.0 + "%");
        }
    }

    public static class MiscEntry extends Entry
    {
        public String stat;
        public String value;

        public MiscEntry(String stat, String value)
        {
            this.stat = stat;
            this.value = value;
        }

        @Override
        public void draw(int num, int count, int pageSize)
        {
            super.draw(num, count, pageSize);

            if (num % 2 == 0)
                this.drawBar(this.yPos, 30, 64);
            else
                this.drawBar(this.yPos, 30, 32);


            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.setColor(255, 255, 255, 255 * age / maxAge);
            Drawing.drawing.displayInterfaceText(this.getXOffset() + Game.screen.centerX + misc_1, this.yPos, this.stat);
            Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + misc_2, this.yPos, this.value);
        }
    }


    public static double processColor(double color)
    {
        return (color * 2 + 255) / 3;
    }
}
