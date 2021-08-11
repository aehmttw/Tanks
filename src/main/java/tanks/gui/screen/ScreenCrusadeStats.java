package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.registry.RegistryTank;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public class ScreenCrusadeStats extends Screen implements IDarkScreen
{
    public Crusade crusade;

    public long musicStartTime;
    public double age;
    public boolean musicStarted = false;

    public long lastEntryTime = System.currentTimeMillis() + 500;
    public int entriesShown = -1;

    public static int page_size = 15;
    public int tankPage = 0;

    public ArrayList<TankEntry> tanks = new ArrayList<>();

    public static final int tanks_1 = -400;
    public static final int tanks_2 = -200;
    public static final int tanks_2a = -50;
    public static final int tanks_3 = 0;
    public static final int tanks_3a = 100;
    public static final int tanks_4 = 200;
    public static final int tanks_5 = 400;

    public int totalKills = 0;
    public int totalCoins = 0;
    public int totalDeaths = 0;

    public ScreenCrusadeStats(Crusade crusade)
    {
        super(350, 40, 380, 60);

        if (Drawing.drawing.interfaceScaleZoom > 1)
            page_size = 8;

        this.crusade = crusade;

        this.musicStartTime = Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/results_intro_length.txt").get(0));
        Drawing.drawing.playSound("results_intro.ogg", 1f, true);

        for (RegistryTank.TankEntry t: Game.registryTank.tankEntries)
        {
            Integer kills = crusade.crusadePlayers.get(Game.player).tankKills.get(t.name);
            Integer deaths = crusade.crusadePlayers.get(Game.player).tankDeaths.get(t.name);

            if (kills == null && deaths == null)
                continue;

            if (kills == null)
                kills = 0;

            if (deaths == null)
                deaths = 0;

            this.tanks.add(new TankEntry(this, t, kills, deaths));
        }

        Integer kills = crusade.crusadePlayers.get(Game.player).tankKills.get("player");
        Integer deaths = crusade.crusadePlayers.get(Game.player).tankDeaths.get("player");

        if (kills == null && deaths == null)
            return;

        if (kills == null)
            kills = 0;

        if (deaths == null)
            deaths = 0;

        this.tanks.add(new TankEntry(this, kills, deaths));
    }

    Button exit = new Button(this.centerX, Drawing.drawing.interfaceSizeY - 60, this.objWidth, this.objHeight, "Return to title", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade.crusadeMode = false;
            Crusade.currentCrusade = null;
            Game.exitToTitle();
        }
    }
    );

    Button nextTankPage = new Button(this.centerX, 0, 500, 30, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            tankPage++;
        }
    }
    );

    Button previousTankPage = new Button(this.centerX, 0, 500, 30, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            tankPage--;
        }
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

        if (this.tanks.size() > page_size && this.entriesShown >= Math.min((this.tankPage + 1) * page_size, this.tanks.size()))
        {
            if ((this.tankPage + 1) * page_size < this.tanks.size() && this.entriesShown >= Math.min((this.tankPage + 1) * page_size, this.tanks.size()))
                nextTankPage.update();

            if (this.tankPage > 0 && this.entriesShown >= 0 )
                previousTankPage.update();
        }

        exit.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        this.drawTopBar(120);
        this.drawBottomBar(120);

        Drawing.drawing.setColor(255, 255, 255);

        Drawing.drawing.setInterfaceFontSize(this.titleSize * 1.2);
        Drawing.drawing.drawInterfaceText(this.centerX, 40, this.crusade.name.replace("_", " "));
        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        if (this.age > 12)
            Drawing.drawing.drawInterfaceText(this.centerX, 80, "statistics");

        if (System.currentTimeMillis() - lastEntryTime > 100 && this.entriesShown < Math.min((this.tankPage + 1) * page_size, this.tanks.size()))
        {
            entriesShown++;
            lastEntryTime = System.currentTimeMillis();
            Drawing.drawing.playSound("bullet_explode.ogg", 1.5f);
        }

        if (System.currentTimeMillis() - lastEntryTime > 500 && this.entriesShown == this.tanks.size())
        {
            entriesShown++;
            Drawing.drawing.playSound("bullet_explode.ogg", 1.5f);
        }

        int entries = this.tanks.size();

        int entriesOnPage = Math.min(entries, page_size);
        double aboveY = Game.screen.centerY + (-1 - ((entriesOnPage - 1.0) / 2.0)) * 30 - 10;
        double belowY = Game.screen.centerY + (entriesOnPage - ((entriesOnPage - 1.0) / 2.0)) * 30 + 10;

        if (entries > page_size)
        {
            aboveY -= 50;
            belowY += 50;
        }

        this.nextTankPage.posY = belowY - 50;
        this.previousTankPage.posY = aboveY + 50;

        this.nextTankPage.image = "vertical_arrow.png";
        this.nextTankPage.imageSizeX = 15;
        this.nextTankPage.imageSizeY = -15;
        this.nextTankPage.imageXOffset = -225;

        this.previousTankPage.image = "vertical_arrow.png";
        this.previousTankPage.imageSizeX = 15;
        this.previousTankPage.imageSizeY = 15;
        this.previousTankPage.imageXOffset = -225;

        if (entriesShown >= 0)
        {
            drawBar(aboveY, 50, 127);
            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_1, aboveY, "Tank");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_2, aboveY, "Kills");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_2a, aboveY, "x");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_3, aboveY, "Coins");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_3a, aboveY, "->");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_4, aboveY, "Total coins");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_5, aboveY, "Deaths");
        }

        if (entriesShown > this.tanks.size())
        {
            drawBar(belowY, 50, 127);
            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.setInterfaceFontSize(32);
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_1, belowY, "Total");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_2, belowY, this.totalKills + "");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_4, belowY, this.totalCoins + "");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_5, belowY, this.totalDeaths + "");
        }

        if (this.tanks.size() > page_size)
        {
            if ((this.tankPage + 1) * page_size < this.tanks.size() && this.entriesShown >= Math.min((this.tankPage + 1) * page_size, this.tanks.size()))
                nextTankPage.draw();

            if (this.tankPage > 0 && this.entriesShown >= 0 )
                previousTankPage.draw();
        }

        for (int i = page_size * this.tankPage; i < Math.min(entries, page_size * (tankPage + 1)); i++)
        {
            if (i < this.entriesShown)
                this.tanks.get(i).draw(i, this.tanks.size(), page_size);
        }

        exit.draw();
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

        public void draw(int num, int count, int pageSize)
        {
            int numOnPage = num % pageSize;
            int entriesOnPage = Math.min(pageSize, count);
            this.yPos = Game.screen.centerY + (numOnPage - ((entriesOnPage - 1.0) / 2.0)) * 30;
        }

        public void drawBar(double ypos, double height, double opacity)
        {
            double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

            Drawing.drawing.setColor(0, 0, 0, opacity);
            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, ypos, width, height);
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
            this.tank.drawForInterface(Game.screen.centerX + tanks_1, this.yPos, 0.5);
            Drawing.drawing.setColor(processColor(this.tank.colorR), processColor(this.tank.colorG), processColor(this.tank.colorB));
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_2, this.yPos, this.kills + "");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_2a, this.yPos, "x");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_3, this.yPos, this.coins + "");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_3a, this.yPos, "->");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_4, this.yPos, this.coins * this.kills + "");
            Drawing.drawing.drawInterfaceText(Game.screen.centerX + tanks_5, this.yPos, this.deaths + "");
        }
    }

    public static class LevelEntry extends Entry
    {
        public int level;

        public LevelEntry(int level)
        {
            this.level = level;
        }
    }

    public static double processColor(double color)
    {
        return (color * 2 + 255) / 3;
    }
}
