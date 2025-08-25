package tanks;

import basewindow.BaseFile;
import basewindow.Color;
import tanks.hotbar.Hotbar;
import tanks.hotbar.ItemBar;
import tanks.item.Item;
import tanks.network.ConnectedPlayer;
import tanks.tank.Tank;
import tanks.tank.Turret;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Player
{
    public int remainingLives;
    public Hotbar hotbar = new Hotbar(this);

    public UUID clientID;
    public String username;
    public Tank tank;
    public String buildName = "player";
    public HashSet<String> ownedBuilds = new HashSet<>();

    public long lastNudge = 0;

    public static final Color default_primary = new Color(0, 150, 255, 255);
    public static final Color default_secondary = new Color(Turret.calculateSecondaryColor(default_primary.red), Turret.calculateSecondaryColor(default_primary.green), Turret.calculateSecondaryColor(default_primary.blue), 255);
    public static final Color default_tertiary = new Color((default_primary.red + default_secondary.red) / 2, (default_primary.green + default_secondary.green) / 2, (default_primary.blue + default_secondary.blue) / 2, 255);

    public Color color = new Color().set(default_primary);
    public Color color2 = new Color().set(default_secondary);
    public Color color3 = new Color().set(default_tertiary);

    public boolean enableSecondaryColor = false;
    public boolean enableTertiaryColor = false;

    public boolean isBot = false;

    protected ConnectedPlayer connectedPlayer;

    public Player(UUID clientID, String username)
    {
        this.clientID = clientID;
        this.username = username;
        this.connectedPlayer = new ConnectedPlayer(clientID, username);
    }

    public String toString()
    {
        return this.username + " (" + this.clientID + ")";
    }

    public Crusade loadCrusade(BaseFile f)
    {
        try
        {
            if (!f.exists())
                return null;

            f.startReading();
            String name = f.nextLine();
            String fileName = f.nextLine();
            boolean internal = Boolean.parseBoolean(f.nextLine());

            Crusade c;

            if (internal)
                c = new Crusade(Game.game.fileManager.getInternalFileContents("/crusades" + fileName), name, fileName);
            else
                c = new Crusade(Game.game.fileManager.getFile(fileName), name);

            this.hotbar = new Hotbar(this);
            this.hotbar.itemBar = new ItemBar(this);

            c.currentLevel = Integer.parseInt(f.nextLine());
            c.saveLevel = c.currentLevel;
            c.started = true;

            CrusadePlayer cp = new CrusadePlayer(this);
            this.remainingLives = Integer.parseInt(f.nextLine());
            cp.coins = Integer.parseInt(f.nextLine());
            cp.itemBar = new ItemBar(this);
            c.crusadePlayers.put(this, cp);

            String[] items = f.nextLine().split("\\|");

            if (f.hasNextLine())
                c.timePassed = Double.parseDouble(f.nextLine());

            if (f.hasNextLine())
            {
                parseStringIntHashMap(cp.tankKills, f.nextLine());
                parseStringIntHashMap(cp.tankDeaths, f.nextLine());
            }

            if (f.hasNextLine())
                parseLevelPerformances(c.performances, f.nextLine());

            if (f.hasNextLine())
            {
                parseStringIntHashMap(cp.itemUses, f.nextLine());
                parseStringIntHashMap(cp.itemHits, f.nextLine());
            }

            if (f.hasNextLine())
            {
                parseIntHashSet(c.livingTankIDs, f.nextLine());
                c.retry = c.livingTankIDs.size() > 0;
            }

            if (f.hasNextLine())
            {
                parseStringHashSet(cp.ownedBuilds, f.nextLine());
                cp.currentBuild = f.nextLine();
            }

            f.stopReading();

            ArrayList<Item.ShopItem> shop = c.getShop();

            for (int i = 0; i < items.length; i++)
            {
                String[] sec = items[i].split(",");
                String itemName = sec[0];
                int count = Integer.parseInt(sec[1]);

                for (Item.ShopItem it : shop)
                {
                    if (it.itemStack.item.name.equals(itemName))
                    {
                        cp.itemBar.slots[i] = Item.CrusadeShopItem.fromString(it.toString()).itemStack;
                        cp.itemBar.slots[i].player = this;
                        cp.itemBar.slots[i].stackSize = count;
                    }
                }
            }

            return c;
        }
        catch (Exception e)
        {
            System.err.println("Failed to load saved crusade progress (log file includes contents): ");
            e.printStackTrace();
            Game.logger.println("Failed to load saved crusade progress: ");
            e.printStackTrace(Game.logger);
            Game.logger.println("Progress file contents:");

            try
            {
                f.startReading();
                while (f.hasNextLine())
                    Game.logger.println(f.nextLine());
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
        }

        return null;
    }

    public static void parseStringIntHashMap(HashMap<String, Integer> map, String str)
    {
        String[] parts = str.replace("{", "").replace("}", "").split(", ");

        for (String s: parts)
        {
            if (s.length() <= 0)
                continue;

            String[] sec = s.split("=");
            map.put(sec[0], Integer.parseInt(sec[1]));
        }
    }

    public static void parseIntHashSet(HashSet<Integer> set, String str)
    {
        String[] parts = str.replace("[", "").replace("]", "").split(", ");

        for (String s: parts)
        {
            if (s.length() <= 0)
                continue;

            set.add(Integer.parseInt(s));
        }
    }

    public static void parseStringHashSet(HashSet<String> set, String str)
    {
        String[] parts = str.replace("[", "").replace("]", "").split(", ");

        for (String s: parts)
        {
            if (s.length() <= 0)
                continue;

            set.add(s);
        }
    }

    public static void parseLevelPerformances(ArrayList<Crusade.LevelPerformance> performances, String str)
    {
        String[] parts = str.replace("[", "").replace("]", "").split(", ");

        for (String s: parts)
        {
            if (s.length() <= 0)
                continue;

            String[] sec = s.split("/");
            Crusade.LevelPerformance l = new Crusade.LevelPerformance(Integer.parseInt(sec[0]));
            l.attempts = Integer.parseInt(sec[1]);
            l.bestTime = Double.parseDouble(sec[2]);
            l.totalTime = Double.parseDouble(sec[3]);
            performances.add(l);
        }
    }

    public ConnectedPlayer getConnectedPlayer()
    {
        if (this == Game.player)
            this.connectedPlayer = new ConnectedPlayer(Game.player.clientID, Game.player.username);

        this.connectedPlayer.color.set(this.color);
        this.connectedPlayer.color2.set(this.color2);
        this.connectedPlayer.color3.set(this.color3);

        if (this.tank != null && this.tank.team != null && this.tank.team.enableColor)
            this.connectedPlayer.teamColor.set(this.tank.team.teamColor);
        else
            this.connectedPlayer.teamColor.set(255, 255, 255);

        return this.connectedPlayer;
    }
}
