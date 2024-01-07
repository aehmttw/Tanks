package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.gui.Button;
import tanks.gui.Selector;
import tanks.gui.SpeedrunTimer;
import tanks.hotbar.item.Item;
import tanks.obstacle.Obstacle;
import tanks.registry.RegistryTank;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ScreenCrusadeStats extends Screen implements IDarkScreen, IHiddenChatboxScreen, ICrusadePreviewScreen
{
    public enum View {tanks, levels, items, misc};
    public View view = View.tanks;
    public View prevView = View.tanks;

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

    public static final int levels_1r = -300;
    public static final int levels_2r = 0;
    public static final int levels_3r = 300;

    public static final int items_1 = -400;
    public static final int items_2 = -133;
    public static final int items_3 = 133;
    public static final int items_4 = 400;

    public static final int misc_1 = -150;
    public static final int misc_2 = 150;

    public double topBarTimer = 0;
    public double bottomBarTimer = 0;
    public static double maxBarTime = 10;

    public double tankTimer = 0;
    public int tankType = 0;
    public int tankCount = -1;
    public ArrayList<Tank> rollingTanks = new ArrayList<>();

    public int totalKills = 0;
    public int totalCoins = 0;
    public int totalDeaths = 0;

    public double totalBestTimes = 0;
    public int totalAttempts = 0;

    public boolean recordExists = false;
    public boolean showRecord = false;
    public double recordDisplayFrac = 0;
    public double previousBest = 0;

    public boolean wizardFinished = false;

    public CrusadePlayer player;

    public ScreenCrusadeLevels background;

    public HashMap<String, TankAIControlled> customTanks = new HashMap<>();

    public boolean onlyRecord = false;
    public Screen prev;

    public ScreenCrusadeStats(Crusade crusade, ScreenCrusadeDetails screen)
    {
        this(crusade, null, false, screen);
    }

    public CrusadePlayer setupOnlyRecords(ScreenCrusadeDetails screenCrusadeStats)
    {
        CrusadePlayer cp = new CrusadePlayer(Game.player);
        crusade.crusadePlayers.put(Game.player, cp);
        Crusade.currentCrusade = crusade;
        this.view = ScreenCrusadeStats.View.levels;
        this.onlyRecord = true;
        this.showRecord = true;
        this.prev = screenCrusadeStats;
        this.recordDisplayFrac = 1;
        return cp;
    }

    public ScreenCrusadeStats(Crusade crusade, CrusadePlayer p, boolean intro)
    {
        this(crusade, p, intro, null);
    }

    public ScreenCrusadeStats(Crusade crusade, CrusadePlayer p, boolean intro, ScreenCrusadeDetails screen)
    {
        super(350, 40, 250, 60);

        if (Drawing.drawing.interfaceScaleZoom > 1)
            page_size = 9;

        if (Game.previewCrusades)
        {
            if (screen == null)
                this.background = new ScreenCrusadeLevels(crusade);
            else
                this.background = screen.background;
        }

        this.crusade = crusade;

        if (screen != null)
            p = setupOnlyRecords(screen);

        this.player = p;
        this.forceInBounds = Game.previewCrusades;

        Obstacle.draw_size = Game.tile_size;

        this.addTanks();
        this.addLevels();
        this.addItems();
        this.addMisc();

        for (TankAIControlled t: this.crusade.customTanks)
        {
            this.customTanks.put(t.name, t);
        }

        if (intro)
        {
            this.musicStartTime = Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/results_intro_length.txt").get(0));
            Drawing.drawing.playSound("results_intro.ogg", 1f, true);
        }
        else
        {
            if (onlyRecord)
            {
                this.music = this.prev.music;
                this.musicID = this.prev.musicID;
            }
            else
            {
                this.music = "results.ogg";
                this.musicStarted = true;
            }

            this.tankEntriesShown = this.tanks.size() + 1;
            this.levelEntriesShown = this.levels.size() + 1;
            this.itemEntriesShown = this.items.size() + 1;
            this.miscEntriesShown = this.misc.size() + 1;
            this.wizardFinished = true;

            this.age = 12;
        }

        if (!Game.fullStats)
        {
            this.view = View.misc;
            this.wizardFinished = true;

            this.tankEntriesShown = this.tanks.size() + 1;
            this.levelEntriesShown = this.levels.size() + 1;
            this.itemEntriesShown = this.items.size() + 1;
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

            if (pl.player.clientID.equals(this.player.player.clientID))
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

        for (TankAIControlled t: crusade.customTanks)
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

        if (kills == null)
            kills = 0;

        if (deaths == null)
            deaths = 0;

        if (kills > 0 || deaths > 0)
            this.tanks.add(new TankEntry(this, kills, deaths));

        ArrayList<Entry> copy = new ArrayList<>(this.tanks);
        assignRanks(copy, (o1, o2) -> (int) Math.signum(((TankEntry) o2).kills - ((TankEntry) o1).kills), (entry, rank) -> ((TankEntry) entry).killRank = rank);
        assignRanks(copy, (o1, o2) -> (int) Math.signum(((TankEntry) o2).deaths - ((TankEntry) o1).deaths), (entry, rank) -> ((TankEntry) entry).deathRank = rank);
        assignRanks(copy, (o1, o2) -> (int) Math.signum(((TankEntry) o2).coins * ((TankEntry) o2).kills - ((TankEntry) o1).coins * ((TankEntry) o1).kills), (entry, rank) -> ((TankEntry) entry).coinRank = rank);
    }

    public void addLevels()
    {
        if (onlyRecord)
        {
            for (int i = 0; i < crusade.levels.size(); i++)
            {
                Crusade.CrusadeLevel l = crusade.levels.get(i);
                String name = "Battle " + (i + 1);

                if (crusade.showNames)
                    name = ((i + 1) + ". " + crusade.levels.get(i).levelName.replace("_", " "));

                this.levels.add(new LevelEntry(name, null, crusade.respawnTanks, this));
            }
        }
        else
        {
            for (Crusade.LevelPerformance l : crusade.performances)
            {
                String name = "Battle " + (l.index + 1);

                if (crusade.showNames)
                    name = (l.index + 1 + ". " + crusade.levels.get(l.index).levelName.replace("_", " "));

                this.levels.add(new LevelEntry(name, l, crusade.respawnTanks, this));

                this.totalAttempts += l.attempts;

                if (l.bestTime < Double.MAX_VALUE)
                    this.totalBestTimes += l.bestTime;
            }
        }

        if (crusade.internal && crusade.crusadePlayers.get(Game.player).player.remainingLives > 0 && !ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
        {
            try
            {
                BaseFile dir = Game.game.fileManager.getFile(Game.homedir + Game.crusadeDir + "/records/internal");
                if (!dir.exists())
                    dir.mkdirs();

                BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.crusadeDir + "/records/internal/" + crusade.name.replace(" ", "_").toLowerCase() + ".record");
                double bestTime = Double.MAX_VALUE;

                if (f.exists())
                {
                    this.recordExists = true;
                    bestTime = 0;
                    f.startReading();
                    int i = 0;
                    while (f.hasNextLine())
                    {
                        double t = Double.parseDouble(f.nextLine());
                        if (i < this.levels.size())
                        {
                            LevelEntry l = ((LevelEntry) (this.levels.get(i)));
                            l.bestTime = t;

                            if (!onlyRecord)
                                l.timeDiff = l.level.totalTime - l.bestTime;
                        }

                        bestTime += t;
                        i++;
                    }
                    this.previousBest = bestTime;
                    f.stopReading();
                }

                if (crusade.timePassed <= bestTime && !Game.invulnerable && !TankPlayer.enableDestroyCheat && !onlyRecord)
                {
                    if (!f.exists())
                        f.create();

                    f.startWriting();
                    for (Crusade.LevelPerformance l : crusade.performances)
                    {
                        f.println(l.totalTime + "");
                    }
                    f.stopWriting();
                }
            }
            catch (Exception e)
            {
                Game.exitToCrash(e);
            }
        }

        ArrayList<Entry> copy = new ArrayList<>(this.levels);

        if (!onlyRecord)
        {
            assignRanks(copy, (o1, o2) -> (int) Math.signum(((LevelEntry) o2).level.totalTime - ((LevelEntry) o1).level.totalTime), (entry, rank) -> ((LevelEntry) entry).timeRank = rank);
            assignRanks(copy, (o1, o2) -> (int) Math.signum(((LevelEntry) o2).level.bestTime - ((LevelEntry) o1).level.bestTime), (entry, rank) -> ((LevelEntry) entry).clearRank = rank);
            assignRanks(copy, (o1, o2) -> (int) Math.signum(((LevelEntry) o2).level.attempts - ((LevelEntry) o1).level.attempts), (entry, rank) -> ((LevelEntry) entry).triesRank = rank);
        }

        if (recordExists)
        {
            assignRanks(copy, (o1, o2) -> (int) Math.signum(((LevelEntry) o2).bestTime - ((LevelEntry) o1).bestTime), (entry, rank) -> ((LevelEntry) entry).bestTimeRank = rank);
            assignRanks(copy, (o1, o2) -> (int) Math.signum(Math.abs(((LevelEntry) o2).timeDiff) - Math.abs(((LevelEntry) o1).timeDiff)), (entry, rank) -> ((LevelEntry) entry).timeDiffRank = rank);
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

        ArrayList<Entry> copy = new ArrayList<>(this.items);
        assignRanks(copy, (o1, o2) -> (int) Math.signum(((ItemEntry) o2).uses - ((ItemEntry) o1).uses), (entry, rank) -> ((ItemEntry) entry).useRank = rank);
        assignRanks(copy, (o1, o2) -> (int) Math.signum(((ItemEntry) o2).hits - ((ItemEntry) o1).hits), (entry, rank) -> ((ItemEntry) entry).hitRank = rank);
        assignRanks(copy, (o1, o2) -> (int) Math.signum(((ItemEntry) o2).hits * 1.0 / Math.max(1, ((ItemEntry) o2).uses) - ((ItemEntry) o1).hits * 1.0 / Math.max(1, ((ItemEntry) o1).uses)), (entry, rank) -> ((ItemEntry) entry).accuracyRank = rank);
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

    public void assignRanks(ArrayList<Entry> entries, Comparator<Entry> comparison, BiConsumer<Entry, Double> setRank)
    {
        assignRanks(entries, comparison, setRank, (entry -> true));
    }

    public void assignRanks(ArrayList<Entry> entries, Comparator<Entry> comparison, BiConsumer<Entry, Double> setRank, Function<Entry, Boolean> isValid)
    {
        if (entries.isEmpty())
            return;

        Collections.sort(entries, comparison);

        int ranks = 0;

        if (isValid.apply(entries.get(0)))
            ranks++;

        for (int i = 1; i < entries.size(); i++)
        {
            if (isValid.apply(entries.get(i)) && comparison.compare(entries.get(i - 1), entries.get(i)) != 0)
                ranks++;
        }

        if (ranks > 1)
            setRank.accept(entries.get(0), 0.0);
        else
            setRank.accept(entries.get(0), 0.5);

        int ranksCurrent = 0;
        for (int i = 1; i < entries.size(); i++)
        {
            if (isValid.apply(entries.get(i)) && comparison.compare(entries.get(i - 1), entries.get(i)) != 0)
                ranksCurrent++;

            if (ranks <= 1)
                setRank.accept(entries.get(i), 0.5);
            else
                setRank.accept(entries.get(i), ranksCurrent / (ranks - 1.0));
        }
    }

    Button exit = new Button(this.centerX, Drawing.drawing.interfaceSizeY - 35, this.objWidth, this.objHeight, "Exit", () ->
    {
        Crusade.crusadeMode = false;
        Crusade.currentCrusade = null;

        if (ScreenPartyLobby.isClient)
            Game.screen = new ScreenPartyLobby();
        else if (ScreenPartyHost.isServer)
            Game.screen = ScreenPartyHost.activeScreen;
        else
        {
            Game.cleanUp();
            Panel.panel.zoomTimer = 0;
            Game.screen = new ScreenPlaySingleplayer();
        }
    }
    );

    Button exit2 = new Button(this.centerX, Drawing.drawing.interfaceSizeY - 60, this.objWidth, this.objHeight, "Back", () ->
    {
        Crusade.crusadeMode = false;
        Crusade.currentCrusade = null;

        Game.screen = prev;
    }
    );

    Selector changePlayer;


    Button viewTanks = new Button(this.centerX - this.objXSpace * 1.5, Drawing.drawing.interfaceSizeY - 90, this.objWidth * 0.65, this.objHeight, "Tanks", () -> view = View.tanks);

    Button viewLevels = new Button(this.centerX - this.objXSpace * 0.5, Drawing.drawing.interfaceSizeY - 90, this.objWidth * 0.65, this.objHeight, "Battles", () -> view = View.levels);

    Button viewItems = new Button(this.centerX + this.objXSpace * 0.5, Drawing.drawing.interfaceSizeY - 90, this.objWidth * 0.65, this.objHeight, "Items", () -> view = View.items);

    Button viewMisc = new Button(this.centerX + this.objXSpace * 1.5, Drawing.drawing.interfaceSizeY - 90, this.objWidth * 0.65, this.objHeight, "Summary", () -> view = View.misc);

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

        for (Entry e: tanks)
            e.age = 0;

        for (Entry e: levels)
            e.age = 0;

        for (Entry e: items)
            e.age = 0;

        for (Entry e: misc)
            e.age = 0;
    }
    );

    Button showRecordButton = new Button(-1000, -1000, 35, 35, "", () -> {this.showRecord = !this.showRecord; }, "Toggle showing best time");

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

        for (Entry e: tanks)
            e.age = 0;

        for (Entry e: levels)
            e.age = 0;

        for (Entry e: items)
            e.age = 0;

        for (Entry e: misc)
            e.age = 0;
    }
    );

    @Override
    public void update()
    {
        if (!showRecord)
            this.recordDisplayFrac = Math.max(this.recordDisplayFrac - Panel.frameFrequency / 10, 0);
        else
            this.recordDisplayFrac = Math.min(this.recordDisplayFrac + Panel.frameFrequency / 10, 1);

        Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);

        if (onlyRecord)
            Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 3, 191);

        if (!musicStarted && !onlyRecord)
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

            int entries = this.levels.size();

            int entriesOnPage = Math.min(entries, page_size);
            double belowY = Game.screen.centerY + (entriesOnPage - ((entriesOnPage - 1.0) / 2.0)) * 30 + 10;

            if (entries > page_size)
            {
                belowY += 50;
            }

            if (this.recordExists && levelEntriesShown > this.levels.size())
            {
                double l4 = levels_4 * (recordDisplayFrac) + levels_3r * (1 - recordDisplayFrac);
                if (crusade.respawnTanks)
                    l4 = levels_4;

                double o = -50 * (1 - bottomBarTimer / maxBarTime);

                this.showRecordButton.posX = o + Game.screen.centerX + l4 + 100;
                this.showRecordButton.posY = belowY;
                this.showRecordButton.fullInfo = true;

                if (this.showRecord)
                    this.showRecordButton.image = "icons/nostar.png";
                else
                    this.showRecordButton.image = "icons/star.png";

                this.showRecordButton.imageSizeX = 25;
                this.showRecordButton.imageSizeY = 25;

                if (!onlyRecord)
                    this.showRecordButton.update();
            }
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

                Tank t;
                TankAIControlled t1 = this.customTanks.get(((TankEntry) this.tanks.get(this.tankType)).tank.name);

                if (t1 == null)
                    t = Game.registryTank.getEntry(((TankEntry) this.tanks.get(this.tankType)).tank.name).getTank(Drawing.drawing.sizeX * 1.4, Drawing.drawing.sizeY - 200, Math.PI);
                else
                {
                    TankAIControlled t2 = new TankAIControlled("", Drawing.drawing.sizeX * 1.4, Drawing.drawing.sizeY - 200, 0, 0, 0, 0, Math.PI, TankAIControlled.ShootAI.none);
                    t1.cloneProperties(t2);
                    t = t2;
                }

                if (((TankEntry) this.tanks.get(this.tankType)).tank.name.equals("player"))
                    t = new TankPlayer(t.posX, t.posY, t.angle);

                t.posX += 5 * this.tankTimer;
                t.fullBrightness = true;

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

        if (onlyRecord)
            exit2.update();
        else if (!wizardFinished)
            next.update();
        else
            exit.update();

        if (!onlyRecord)
        {
            if (!wizardFinished)
            {
                if (Game.game.input.moveRight.isValid() && next.enabled)
                {
                    next.function.run();
                    Game.game.input.moveRight.invalidate();
                }
            }
            else
            {
                if (Game.game.input.moveRight.isValid())
                {
                    if (view == View.tanks)
                        view = View.levels;
                    else if (view == View.levels)
                        view = View.items;
                    else if (view == View.items)
                        view = View.misc;
                    else
                        view = View.tanks;

                    Drawing.drawing.playSound("boost.ogg", 2f);
                    Game.game.input.moveRight.invalidate();
                }

                if (Game.game.input.moveLeft.isValid())
                {
                    if (view == View.tanks)
                        view = View.misc;
                    else if (view == View.levels)
                        view = View.tanks;
                    else if (view == View.items)
                        view = View.levels;
                    else
                        view = View.items;

                    Drawing.drawing.playSound("boost.ogg", 2f);
                    Game.game.input.moveLeft.invalidate();
                }
            }

            if (wizardFinished)
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
    }

    public void updatePageButtons(int shown, int size, int page)
    {
        if (size > page_size && shown >= Math.min((page + 1) * page_size, size))
        {
            if ((page + 1) * page_size < size && shown >= Math.min((page + 1) * page_size, size))
                nextPage.update();

            if (page > 0 && shown >= 0)
                previousPage.update();

            if (Game.game.input.moveDown.isValid() && (page + 1) * page_size < size && shown >= Math.min((page + 1) * page_size, size))
            {
                nextPage.function.run();
                Game.game.input.moveDown.invalidate();
                Drawing.drawing.playSound("boost.ogg", 2f);
            }

            if (Game.game.input.moveUp.isValid() && page > 0 && shown >= 0)
            {
                previousPage.function.run();
                Game.game.input.moveUp.invalidate();
                Drawing.drawing.playSound("boost.ogg", 2f);
            }
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
        if (Game.previewCrusades)
        {
            this.background.draw();

            if (!Game.game.window.drawingShadow)
                Game.game.window.clearDepth();

            Drawing.drawing.setColor(0, 0, 0, Math.max(0, Panel.darkness));
            Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);
        }
        else
            this.drawDefaultBackground();

        this.drawTopBar(Math.min(120, this.age * 10));
        this.drawBottomBar(Math.min(120, this.age * 10));

        if (prevView != view)
        {
            prevView = view;

            for (Entry e: tanks)
                e.age = 0;

            for (Entry e: levels)
                e.age = 0;

            for (Entry e: items)
                e.age = 0;

            for (Entry e: misc)
                e.age = 0;

            topBarTimer = 0;
            bottomBarTimer = 0;
        }

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

        this.nextPage.image = "icons/arrow_down.png";
        this.nextPage.imageSizeX = 15;
        this.nextPage.imageSizeY = 15;
        this.nextPage.imageXOffset = -225;

        this.previousPage.image = "icons/arrow_up.png";
        this.previousPage.imageSizeX = 15;
        this.previousPage.imageSizeY = 15;
        this.previousPage.imageXOffset = -225;

        if (this.age > 12 || this.wizardFinished)
        {
            if (onlyRecord)
                Drawing.drawing.displayInterfaceText(this.centerX + offset, 80, "best run");
            else
                Drawing.drawing.displayInterfaceText(this.centerX + offset, 80, "statistics");
        }

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
                if (!Game.game.window.drawingShadow)
                    topBarTimer = Math.min(topBarTimer + Panel.frameFrequency, maxBarTime);

                double f = topBarTimer / maxBarTime;
                double o = -50 * (1 - topBarTimer / maxBarTime);

                drawBar(aboveY, 50 * f, 127 * f);
                Drawing.drawing.setColor(255, 255, 255, 255 * f);
                Drawing.drawing.setInterfaceFontSize(24);
                Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + tanks_1, aboveY, "Tank");
                Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + tanks_2, aboveY, "Kills");
                Drawing.drawing.drawInterfaceText(o + Game.screen.centerX + tanks_2a, aboveY, "x");
                Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + tanks_3, aboveY, "Coins");
                Drawing.drawing.drawInterfaceText(o + Game.screen.centerX + tanks_3a, aboveY, "->");
                Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + tanks_4, aboveY, "Total coins");
                Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + tanks_5, aboveY, "Deaths");
            }

            if (tankEntriesShown > this.tanks.size())
            {
                if (!Game.game.window.drawingShadow)
                    bottomBarTimer = Math.min(bottomBarTimer + Panel.frameFrequency, maxBarTime);

                double f = bottomBarTimer / maxBarTime;
                double o = -50 * (1 - bottomBarTimer / maxBarTime);

                drawBar(belowY, 50 * f, 127 * f);
                Drawing.drawing.setColor(255, 255, 255, 255 * f);
                Drawing.drawing.setInterfaceFontSize(32);
                Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + tanks_1, belowY, "Total");
                Drawing.drawing.drawInterfaceText(o + Game.screen.centerX + tanks_2, belowY, this.totalKills + "");
                Drawing.drawing.drawInterfaceText(o + Game.screen.centerX + tanks_4, belowY, this.totalCoins + "");
                Drawing.drawing.drawInterfaceText(o + Game.screen.centerX + tanks_5, belowY, this.totalDeaths + "");
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
                if (!Game.game.window.drawingShadow)
                    topBarTimer = Math.min(topBarTimer + Panel.frameFrequency, maxBarTime);

                double f = topBarTimer / maxBarTime;
                double o = -50 * (1 - topBarTimer / maxBarTime);

                if (onlyRecord)
                    o += 267;

                drawBar(aboveY, 50 * f, 127 * f);
                Drawing.drawing.setColor(255, 255, 255, 255 * f);
                Drawing.drawing.setInterfaceFontSize(24);

                if (showRecord)
                {
                    double l1 = levels_1;
                    double l4 = levels_4;

                    if (!crusade.respawnTanks)
                    {
                        l1 = levels_1 * (recordDisplayFrac) + levels_1r * (1 - recordDisplayFrac);
                        l4 = levels_4 * (recordDisplayFrac) + levels_3r * (1 - recordDisplayFrac);
                    }

                    Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + l1, aboveY, "Battle");

                    if (!onlyRecord)
                        Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + l4, aboveY, "Time this run");

                    double o2 = (1 - this.recordDisplayFrac) * -50;
                    Drawing.drawing.setColor(255, 255, 255, 255 * f * this.recordDisplayFrac);
                    Drawing.drawing.displayInterfaceText(o + o2 + Game.screen.centerX + levels_2, aboveY, "Best time");

                    if (!onlyRecord)
                        Drawing.drawing.displayInterfaceText(o + o2 + Game.screen.centerX + levels_3, aboveY, "Difference");
                }
                else if (crusade.respawnTanks)
                {
                    Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + levels_1, aboveY, "Battle");
                    Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + levels_4, aboveY, "Total time");

                    double o2 = (this.recordDisplayFrac) * -50;
                    Drawing.drawing.setColor(255, 255, 255, 255 * f * (1 - this.recordDisplayFrac));
                    Drawing.drawing.displayInterfaceText(o + o2 + Game.screen.centerX + levels_2, aboveY, "Attempts");
                    Drawing.drawing.displayInterfaceText(o + o2 + Game.screen.centerX + levels_3, aboveY, "Clear time");
                }
                else
                {
                    double l1 = levels_1 * (recordDisplayFrac) + levels_1r * (1 - recordDisplayFrac);
                    double l4 = levels_4 * (recordDisplayFrac) + levels_3r * (1 - recordDisplayFrac);

                    Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + l1, aboveY, "Battle");
                    Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + l4, aboveY, "Total time");

                    double o2 = (this.recordDisplayFrac) * -50;
                    Drawing.drawing.setColor(255, 255, 255, 255 * f * (1 - this.recordDisplayFrac));
                    Drawing.drawing.displayInterfaceText(o + o2 + Game.screen.centerX + levels_2r, aboveY, "Attempts");
                }
            }

            if (levelEntriesShown > this.levels.size())
            {
                if (!Game.game.window.drawingShadow)
                    bottomBarTimer = Math.min(bottomBarTimer + Panel.frameFrequency, maxBarTime);

                double f = bottomBarTimer / maxBarTime;
                double o = -50 * (1 - bottomBarTimer / maxBarTime);

                if (onlyRecord)
                    o += 267;

                drawBar(belowY, 50 * f, 127 * f);
                Drawing.drawing.setColor(255, 255, 255, 255 * f);
                Drawing.drawing.setInterfaceFontSize(32);

                if (showRecord)
                {
                    double l1 = levels_1;
                    double l4 = levels_4;

                    if (!crusade.respawnTanks)
                    {
                        l1 = levels_1 * (recordDisplayFrac) + levels_1r * (1 - recordDisplayFrac);
                        l4 = levels_4 * (recordDisplayFrac) + levels_3r * (1 - recordDisplayFrac);
                    }

                    double diff = crusade.timePassed - this.previousBest;
                    Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + l1, belowY, "Total");

                    if (!onlyRecord)
                        Drawing.drawing.drawInterfaceText(o + Game.screen.centerX + l4, belowY, SpeedrunTimer.getTime(crusade.timePassed));

                    double o2 = (1 - this.recordDisplayFrac) * -50;
                    double a = 255 * f * this.recordDisplayFrac;
                    Drawing.drawing.setColor(255, 255, 255, a);
                    Drawing.drawing.drawInterfaceText(o + o2 + Game.screen.centerX + levels_2, belowY, SpeedrunTimer.getTime(this.previousBest));

                    if (!onlyRecord)
                    {
                        String s;
                        if (Math.round(diff) == 0)
                        {
                            Drawing.drawing.setColor(255, 255, 127, a);
                            s = SpeedrunTimer.getTime(0);
                        }
                        else if (Math.round(diff) > 0)
                        {
                            Drawing.drawing.setColor(255, 127, 127, a);
                            s = "+" + SpeedrunTimer.getTime(diff);
                        }
                        else
                        {
                            Drawing.drawing.setColor(127, 255, 127, a);
                            s = "-" + SpeedrunTimer.getTime(-diff);
                        }
                        Drawing.drawing.drawInterfaceText(o + o2 + Game.screen.centerX + levels_3, belowY, s);
                    }
                }
                else if (crusade.respawnTanks)
                {
                    Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + levels_1, belowY, "Total");
                    Drawing.drawing.drawInterfaceText(o + Game.screen.centerX + levels_4, belowY, SpeedrunTimer.getTime(crusade.timePassed));

                    double o2 = (this.recordDisplayFrac) * -50;
                    double a = 255 * f * (1 - this.recordDisplayFrac);
                    Drawing.drawing.setColor(255, 255, 255, a);
                    Drawing.drawing.drawInterfaceText(o + o2 + Game.screen.centerX + levels_2, belowY, this.totalAttempts + "");
                    Drawing.drawing.drawInterfaceText(o + o2 + Game.screen.centerX + levels_3, belowY, SpeedrunTimer.getTime(this.totalBestTimes));
                }
                else
                {
                    double l1 = levels_1 * (recordDisplayFrac) + levels_1r * (1 - recordDisplayFrac);
                    double l4 = levels_4 * (recordDisplayFrac) + levels_3r * (1 - recordDisplayFrac);

                    Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + l1, belowY, "Total");
                    Drawing.drawing.drawInterfaceText(o + Game.screen.centerX + l4, belowY, SpeedrunTimer.getTime(crusade.timePassed));

                    double o2 = (this.recordDisplayFrac) * -50;
                    double a = 255 * f * (1 - this.recordDisplayFrac);
                    Drawing.drawing.setColor(255, 255, 255, a);
                    Drawing.drawing.drawInterfaceText(o + o2 + Game.screen.centerX + levels_2r, belowY, this.totalAttempts + "");
                }

                if (!onlyRecord)
                {
                    this.showRecordButton.draw();

                    double l4 = levels_4;

                    if (!crusade.respawnTanks)
                        l4 = levels_4 * (recordDisplayFrac) + levels_3r * (1 - recordDisplayFrac);

                    if (this.previousBest > crusade.timePassed && recordExists)
                    {
                        Drawing.drawing.setColor(255, 255, 127, 255 * f);
                        Drawing.drawing.setInterfaceFontSize(14);
                        Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + l4 + 145 - 5, belowY - 8, "New");
                        Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + l4 + 145, belowY + 8, "best!");
                    }
                }
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
                if (!Game.game.window.drawingShadow)
                    topBarTimer = Math.min(topBarTimer + Panel.frameFrequency, maxBarTime);

                double f = topBarTimer / maxBarTime;
                double o = -50 * (1 - topBarTimer / maxBarTime);

                drawBar(aboveY, 50 * f, 127 * f);
                Drawing.drawing.setColor(255, 255, 255, 255 * f);
                Drawing.drawing.setInterfaceFontSize(24);
                Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + items_1, aboveY, "Item");
                Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + items_2, aboveY, "Times used");
                Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + items_3, aboveY, "Hits landed");
                Drawing.drawing.displayInterfaceText(o + Game.screen.centerX + items_4, aboveY, "Accuracy");
            }

            if (itemEntriesShown > this.items.size())
            {
                if (!Game.game.window.drawingShadow)
                    bottomBarTimer = Math.min(bottomBarTimer + Panel.frameFrequency, maxBarTime);

                double f = bottomBarTimer / maxBarTime;
                drawBar(belowY, 50 * f, 127 * f);
            }

            this.drawPageEntries(this.itemEntriesShown, this.items.size(), this.itemPage, this.items);
        }
        else if (this.view == View.misc)
        {
            this.drawPageEntries(this.miscEntriesShown, this.misc.size(), this.miscPage, this.misc);

            if (!Game.game.window.drawingShadow)
            {
                for (Tank t : this.rollingTanks)
                {
                    t.draw();
                }
            }
        }

        if (onlyRecord)
            exit2.draw();
        else if (!wizardFinished)
            next.draw();
        else
            exit.draw();

        Drawing.drawing.setColor(255, 255, 255, 127);
        Drawing.drawing.setInterfaceFontSize(32);

        int o = 90;
        double s = 250;

        if (!onlyRecord)
        {
            if (wizardFinished || !Game.fullStats)
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
        public double maxAge = maxBarTime;

        public void draw(int num, int count, int pageSize)
        {
            if (!Game.game.window.drawingShadow)
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

    public static void drawStatistic(double x, double y, String text, double rank, double r, double g, double b, double a, double size)
    {
        drawStatistic(x, y, text, rank, r, g, b, a, size,false);
    }

    public static void drawStatistic(double x, double y, String text, double rank, double r, double g, double b, double a, double size, boolean rightAligned)
    {
        if (size < 0)
            Drawing.drawing.setInterfaceFontSize((rank / 2 + 0.75) * -size);
        else
            Drawing.drawing.setInterfaceFontSize(size);

        if (rank <= 0.5)
        {
            Drawing.drawing.setColor(r, g, b, (rank + 0.5) * a);

            if (rightAligned)
                Drawing.drawing.drawInterfaceText(x, y, text, true);
            else
                Drawing.drawing.drawInterfaceText(x, y, text);
        }
        else
        {
            Drawing.drawing.setColor(r / 2, g / 2, b / 2, Math.pow(a / 255, 4) * 255);

            double diff = (rank - 0.5) * 6;

            if (rightAligned)
            {
                Drawing.drawing.drawInterfaceText(x, y + diff, text, true);
                Drawing.drawing.drawInterfaceText(x - diff, y, text, true);
                Drawing.drawing.drawInterfaceText(x, y - diff, text, true);
                Drawing.drawing.drawInterfaceText(x + diff, y, text, true);

                diff /= Math.sqrt(2);
                Drawing.drawing.drawInterfaceText(x + diff, y + diff, text, true);
                Drawing.drawing.drawInterfaceText(x - diff, y + diff, text, true);
                Drawing.drawing.drawInterfaceText(x - diff, y - diff, text, true);
                Drawing.drawing.drawInterfaceText(x + diff, y - diff, text, true);

                Drawing.drawing.setColor(r, g, b, Math.pow(a / 255, 4) * 255);
                Drawing.drawing.drawInterfaceText(x, y, text, true);
            }
            else
            {
                Drawing.drawing.drawInterfaceText(x, y + diff, text);
                Drawing.drawing.drawInterfaceText(x - diff, y, text);
                Drawing.drawing.drawInterfaceText(x, y - diff, text);
                Drawing.drawing.drawInterfaceText(x + diff, y, text);

                diff /= Math.sqrt(2);
                Drawing.drawing.drawInterfaceText(x + diff, y + diff, text);
                Drawing.drawing.drawInterfaceText(x - diff, y + diff, text);
                Drawing.drawing.drawInterfaceText(x - diff, y - diff, text);
                Drawing.drawing.drawInterfaceText(x + diff, y - diff, text);

                Drawing.drawing.setColor(r, g, b, Math.pow(a / 255, 4) * 255);
                Drawing.drawing.drawInterfaceText(x, y, text);
            }
        }
    }

    public static class TankEntry extends Entry
    {
        public Tank tank;
        public int kills;
        public int coins;
        public int deaths;

        public double killRank;
        public double coinRank;
        public double deathRank;

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

        public TankEntry(ScreenCrusadeStats screen, TankAIControlled tank, int kills, int deaths)
        {
            this.kills = kills;
            this.deaths = deaths;

            TankAIControlled t = new TankAIControlled("", 0, 0, 0, 0, 0, 0, 0, TankAIControlled.ShootAI.none);
            tank.cloneProperties(t);
            this.tank = t;
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
            double r = processColor(this.tank.colorR);
            double g = processColor(this.tank.colorG);
            double b = processColor(this.tank.colorB);
            double a = 255 * age / maxAge;
            drawStatistic(this.getXOffset() + Game.screen.centerX + tanks_2, this.yPos, this.kills + "", 1 - killRank, r, g, b, a, -24);
            drawStatistic(this.getXOffset() + Game.screen.centerX + tanks_2a, this.yPos, "x", 0, r, g, b, a, 24);
            drawStatistic(this.getXOffset() + Game.screen.centerX + tanks_3, this.yPos, this.coins + "", 0.25, r, g, b, a, 24);
            drawStatistic(this.getXOffset() + Game.screen.centerX + tanks_3a, this.yPos, "->", 0, r, g, b, a, 24);
            drawStatistic(this.getXOffset() + Game.screen.centerX + tanks_4, this.yPos, this.coins * this.kills + "", 1 - coinRank, r, g, b, a, -24);
            drawStatistic(this.getXOffset() + Game.screen.centerX + tanks_5, this.yPos, this.deaths + "", 1 - deathRank, r, g, b, a, -24);
            Drawing.drawing.setInterfaceFontSize(24);
        }
    }

    public static class LevelEntry extends Entry
    {
        public Crusade.LevelPerformance level;
        public String name;
        public boolean respawns;

        public double clearRank;
        public double timeRank;
        public double triesRank;

        public double bestTime;
        public double timeDiff;

        public double bestTimeRank;
        public double timeDiffRank;

        public ScreenCrusadeStats screen;

        public LevelEntry(String name, Crusade.LevelPerformance levelPerformance, boolean respawns, ScreenCrusadeStats s)
        {
            this.level = levelPerformance;
            this.name = name;
            this.respawns = respawns;
            this.screen = s;
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

            double l1 = levels_1;
            double l2 = levels_2;
            double l3 = levels_3;
            double l4 = levels_4;

            if (!respawns)
            {
                l1 = levels_1 * (screen.recordDisplayFrac) + levels_1r * (1 - screen.recordDisplayFrac);
                l4 = levels_4 * (screen.recordDisplayFrac) + levels_3r * (1 - screen.recordDisplayFrac);

                if (!screen.showRecord)
                    l2 = levels_2r;
            }

            if (name.contains(". "))
                Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + l1 - 110, this.yPos, this.name, false);
            else
                Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + l1, this.yPos, this.name);

            double r = 255;
            double g = 255;
            double b = 255;
            double a = 255 * age / maxAge;

            double a2 = this.screen.recordDisplayFrac;

            if (!this.screen.showRecord)
                a2 = 1 - a2;

            if (!screen.showRecord && this.level.attempts == 1 && level.bestTime < Double.MAX_VALUE)
            {
                r = 127;
                b = 127;
            }

            if (screen.showRecord)
            {
                double o2 = (1 - this.screen.recordDisplayFrac) * -50;

                drawStatistic(this.getXOffset() + Game.screen.centerX + l2 + 52 + o2, this.yPos, SpeedrunTimer.getTime(this.bestTime) + "", 1 - bestTimeRank, 255, 255, 255, a * a2, 24, true);

                if (!screen.onlyRecord)
                {
                    String s;
                    if (Math.round(this.timeDiff) == 0)
                    {
                        b = 127;
                        s = SpeedrunTimer.getTime(0);
                    }
                    else if (Math.round(this.timeDiff) > 0)
                    {
                        g = 127;
                        b = 127;
                        s = "+" + SpeedrunTimer.getTime(this.timeDiff);
                    }
                    else
                    {
                        r = 127;
                        b = 127;
                        s = "-" + SpeedrunTimer.getTime(-this.timeDiff);
                    }

                    drawStatistic(this.getXOffset() + Game.screen.centerX + l3 + 52 + o2, this.yPos, s, 1 - timeDiffRank, r, g, b, a * a2, 24, true);
                }
            }
            else
            {
                double o2 = (this.screen.recordDisplayFrac) * -50;

                drawStatistic(this.getXOffset() + Game.screen.centerX + l2 + o2, this.yPos, this.level.attempts + "", 1 - triesRank, r, g, b, a * a2, 24);

                if (respawns)
                {
                    if (this.level.bestTime >= Double.MAX_VALUE)
                    {
                        Drawing.drawing.setColor(255, 255, 255, 127 * age / maxAge);
                        Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + l3, this.yPos, "-", false);
                    }
                    else
                        drawStatistic(this.getXOffset() + Game.screen.centerX + l3 + 52 + o2, this.yPos, SpeedrunTimer.getTime(this.level.bestTime), 1 - clearRank, r, g, b, a * a2, 24, true);
                }
            }

            if (!screen.onlyRecord)
            {
                drawStatistic(this.getXOffset() + Game.screen.centerX + l4 + 52, this.yPos, SpeedrunTimer.getTime(this.level.totalTime), 1 - timeRank, 255, 255, 255, a, 24, true);
                Drawing.drawing.setInterfaceFontSize(24);
            }
        }

        public double getXOffset()
        {
            if (screen.onlyRecord)
                return super.getXOffset() + 267;
            else
                return super.getXOffset();
        }
    }

    public static class ItemEntry extends Entry
    {
        public Item item;
        public int uses;
        public int hits;

        public double useRank;
        public double hitRank;
        public double accuracyRank;

        public ItemEntry(Item i, int uses, int hits)
        {
            this.item = i;
            this.uses = uses;
            this.hits = hits;

            if (!i.supportsHits)
                hits = -1;
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
            drawStatistic(this.getXOffset() + Game.screen.centerX + items_2, this.yPos, this.uses + "", 1 - this.useRank, 255, 255, 255, 255 * age / maxAge, 24);

            if (!this.item.supportsHits)
            {
                Drawing.drawing.setColor(255, 255, 255, 127 * age / maxAge);
                Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + items_3, this.yPos, "-");
            }
            else
                drawStatistic(this.getXOffset() + Game.screen.centerX + items_3, this.yPos, this.hits + "", 1 - this.hitRank, 255, 255, 255, 255 * age / maxAge, 24);

            if (this.uses <= 0 || !this.item.supportsHits)
            {
                Drawing.drawing.setColor(255, 255, 255, 127 * age / maxAge);
                Drawing.drawing.drawInterfaceText(this.getXOffset() + Game.screen.centerX + items_4, this.yPos, "-");
            }
            else
                drawStatistic(this.getXOffset() + Game.screen.centerX + items_4, this.yPos, (this.hits * 1000 / this.uses) / 10.0 + "%", 1 - this.accuracyRank, 255, 255, 255, 255 * age / maxAge, 24);
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
