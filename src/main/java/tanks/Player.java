package tanks;

import basewindow.BaseFile;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.Hotbar;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.Item;
import tanks.network.ConnectedPlayer;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.Turret;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Player
{
    public int remainingLives;
    public Hotbar hotbar = new Hotbar();

    public UUID clientID;
    public String username;
    public Tank tank;

    public int colorR = 0;
    public int colorG = 150;
    public int colorB = 255;

    public int turretColorR = (int) Turret.calculateSecondaryColor(colorR);
    public int turretColorG = (int) Turret.calculateSecondaryColor(colorG);
    public int turretColorB = (int) Turret.calculateSecondaryColor(colorB);

    public boolean enableSecondaryColor = false;

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

            this.hotbar = new Hotbar();
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

            f.stopReading();

            ArrayList<Item> shop = c.getShop();

            for (int i = 0; i < items.length; i++)
            {
                String[] sec = items[i].split(",");
                String itemName = sec[0];
                int count = Integer.parseInt(sec[1]);

                for (Item it : shop)
                {
                    if (it.name.equals(itemName))
                    {
                        cp.itemBar.slots[i] = Item.parseItem(this, it.toString());
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

        this.connectedPlayer.colorR = this.colorR;
        this.connectedPlayer.colorG = this.colorG;
        this.connectedPlayer.colorB = this.colorB;
        this.connectedPlayer.colorR2 = this.turretColorR;
        this.connectedPlayer.colorG2 = this.turretColorG;
        this.connectedPlayer.colorB2 = this.turretColorB;
        return this.connectedPlayer;
    }
}
