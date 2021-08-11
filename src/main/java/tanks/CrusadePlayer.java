package tanks;

import basewindow.BaseFile;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.Item;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;

import java.util.HashMap;

public class CrusadePlayer
{
    public Player player;
    public ItemBar itemBar;
    public int coins;

    public CrusadePlayer(Player p)
    {
        this.player = p;
        this.itemBar = new ItemBar(p);
    }

    public HashMap<String, Integer> tankKills = new HashMap<>();
    public HashMap<String, Integer> tankDeaths = new HashMap<>();

    public void addKill(Tank t)
    {
        String name = t.name;

        if (t instanceof TankPlayer)
            name = "player";

        if (Crusade.currentCrusade != null)
        {
            this.tankKills.putIfAbsent(name, 0);
            this.tankKills.put(name, this.tankKills.get(name) + 1);
        }
    }

    public void addDeath(Tank t)
    {
        String name = t.name;

        if (t instanceof TankPlayer)
            name = "player";

        if (Crusade.currentCrusade != null)
        {
            this.tankDeaths.putIfAbsent(name, 0);
            this.tankDeaths.put(name, this.tankDeaths.get(name) + 1);
        }
    }

    public void saveCrusade()
    {
        saveCrusade(false);
    }

    public void saveCrusade(boolean win)
    {
        try
        {
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

            if (Game.screen instanceof ScreenGame && !win && !Game.playerTank.destroy)
                player.remainingLives--;

            f.println(player.remainingLives + "");
            f.println(this.coins + "");

            StringBuilder items = new StringBuilder();
            for (Item i : this.itemBar.slots)
            {
                items.append(i.name).append(",").append(i.stackSize).append("|");
            }

            f.println(items.substring(0, items.length() - 1));
            f.println(Crusade.currentCrusade.timePassed + "");
            f.println(this.tankKills.toString());
            f.println(this.tankDeaths.toString());
            f.stopWriting();

            if ((player.remainingLives <= 0 || Crusade.currentCrusade.win) && f.exists())
                f.delete();
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }
}
