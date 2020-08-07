package tanks;

import basewindow.BaseFile;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.Hotbar;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.Item;
import tanks.tank.Tank;
import tanks.tank.Turret;

import java.util.ArrayList;
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

    public Player(UUID clientID, String username)
    {
        this.clientID = clientID;
        this.username = username;
    }

    public String toString()
    {
        return this.username + " (" + this.clientID + ")";
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
                remainingLives--;

            f.println(remainingLives + "");
            f.println(Crusade.currentCrusade.crusadeCoins.get(this) + "");

            StringBuilder items = new StringBuilder();
            for (Item i : Crusade.currentCrusade.crusadeItembars.get(this).slots)
            {
                items.append(i.name).append(",").append(i.stackSize).append("|");
            }

            f.println(items.substring(0, items.length() - 1));
            f.stopWriting();

            if ((remainingLives <= 0 || Crusade.currentCrusade.win) && f.exists())
                f.delete();
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
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
                c = new Crusade(Game.game.fileManager.getInternalFileContents(fileName), name, fileName);
            else
                c = new Crusade(Game.game.fileManager.getFile(fileName), name);

            hotbar = new Hotbar();
            hotbar.itemBar = new ItemBar(this);

            c.currentLevel = Integer.parseInt(f.nextLine());
            c.saveLevel = c.currentLevel;
            c.started = true;
            this.remainingLives = Integer.parseInt(f.nextLine());
            c.crusadeCoins.put(this, Integer.parseInt(f.nextLine()));
            c.crusadeItembars.put(this, new ItemBar(this));

            String[] items = f.nextLine().split("\\|");

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
                        c.crusadeItembars.get(Game.player).slots[i] = Item.parseItem(this, it.toString());
                        c.crusadeItembars.get(Game.player).slots[i].stackSize = count;
                    }
                }
            }

            f.stopReading();

            return c;
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        return null;
    }
}
