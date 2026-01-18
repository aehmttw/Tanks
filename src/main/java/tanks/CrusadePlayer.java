package tanks;

import basewindow.BaseFile;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.ItemBar;
import tanks.item.Item;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;

import java.util.HashMap;
import java.util.HashSet;

public class CrusadePlayer
{
    public Player player;

    public ItemBar itemBar;
    public int coins;
    public HashSet<String> ownedBuilds = new HashSet<>();
    public String currentBuild = "player";

    public CrusadePlayer(Player p)
    {
        this.player = p;
        this.currentBuild = p.buildName;
        this.ownedBuilds.add(this.currentBuild);
        this.itemBar = new ItemBar(p);
    }

    public HashMap<String, Integer> tankKills = new HashMap<>();
    public HashMap<String, Integer> tankDeaths = new HashMap<>();
    public HashMap<String, Double> itemUses = new HashMap<>();
    public HashMap<String, Double> itemHits = new HashMap<>();

    public void addKill(Tank t)
    {
        String name = t.name;

        if (t instanceof TankPlayer)
            name = "player";

        if (Crusade.currentCrusade != null && !ScreenPartyLobby.isClient)
        {
            this.putIfAbsent(this.tankKills, name, 0);
            this.tankKills.put(name, this.tankKills.get(name) + 1);
        }
    }

    public void addDeath(Tank t)
    {
        String name = t.name;

        if (t instanceof TankPlayer)
            name = "player";

        if (Crusade.currentCrusade != null && !ScreenPartyLobby.isClient)
        {
            this.putIfAbsent(this.tankDeaths, name, 0);
            this.tankDeaths.put(name, this.tankDeaths.get(name) + 1);
        }
    }

    public void addItemUse(Item.ItemStack<?> i, double frac)
    {
        this.addItemStat(this.itemUses, i, frac);
    }

    public void addItemHit(Item.ItemStack<?> i, double frac)
    {
        this.addItemStat(this.itemHits, i, frac);
    }

    public double getItemUses(String i)
    {
        Double n = this.itemUses.get(i);

        if (n == null)
            return 0;

        return n;
    }

    public double getItemHits(String i)
    {
        Double n = this.itemHits.get(i);

        if (n == null)
            return 0;

        return n;
    }

    public void addItemStat(HashMap<String, Double> stat, Item.ItemStack<?> i, double frac)
    {
        String name = i.item.name;

        if (Crusade.currentCrusade != null)
        {
            this.putIfAbsent(stat, name, 0.0);
            stat.put(name, stat.get(name) + frac);
        }
    }

    public void saveCrusade()
    {
        try
        {
            if (Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).savedRemainingTanks)
            {
                Crusade.currentCrusade.livingTankIDs.clear();

                for (Movable m : Game.movables)
                {
                    if (m instanceof Tank && !m.destroy && ((Tank) m).crusadeID >= 0)
                        Crusade.currentCrusade.livingTankIDs.add(((Tank) m).crusadeID);
                }
            }

            BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.savedCrusadePath + Crusade.currentCrusade.name);

            if (Crusade.currentCrusade.internal)
                f = Game.game.fileManager.getFile(Game.homedir + Game.savedCrusadePath + "internal/" + Crusade.currentCrusade.name);

            if (Crusade.currentCrusade == null)
            {
                if (f.exists())
                    f.delete();

                return;
            }

            f.create();
            f.startWriting();
            f.println(Crusade.currentCrusade.name);
            f.println(Crusade.currentCrusade.fileName);
            f.println(Crusade.currentCrusade.internal + "");
            f.println(Crusade.currentCrusade.saveLevel + "");

            /*if (Game.screen instanceof ScreenGame && !win && !Game.playerTank.destroy)
            {
                Crusade.currentCrusade.recordPerformance(ScreenGame.lastTimePassed, win);
                this.coins = player.hotbar.coins;

                if (!(Game.screen instanceof ScreenCrashed || Game.screen instanceof ScreenOutOfMemory))
                    player.remainingLives--;
            }*/

            f.println(player.remainingLives + "");
            f.println(this.coins + "");

            StringBuilder items = new StringBuilder();
            for (Item.ItemStack<?> i : this.itemBar.slots)
            {
                items.append(i.item.name).append(",").append(i.stackSize).append("|");
            }

            f.println(items.substring(0, items.length() - 1));
            f.println(Crusade.currentCrusade.timePassed + "");
            f.println(this.tankKills.toString());
            f.println(this.tankDeaths.toString());
            f.println(Crusade.currentCrusade.performances.toString());
            f.println(this.itemUses.toString());
            f.println(this.itemHits.toString());
            f.println(Crusade.currentCrusade.livingTankIDs.toString());
            f.println(this.ownedBuilds.toString());
            f.println(this.currentBuild);

            f.stopWriting();

            if ((player.remainingLives <= 0 || Crusade.currentCrusade.win) && f.exists())
                f.delete();
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    public <T, U> void putIfAbsent(HashMap<T, U> map, T key, U value)
    {
        if (!map.containsKey(key))
            map.put(key, value);
    }
}
