package tanks;

import basewindow.BaseFile;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.Coins;
import tanks.hotbar.Item;
import tanks.hotbar.ItemBar;
import tanks.tank.Tank;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.UUID;

public class Player
{
    public int remainingLives;
    public ItemBar crusadeItemBar = new ItemBar(this);
    public Coins coins = new Coins();

    public UUID clientID;
    public String username;
    public Tank tank;

    public Player(UUID clientID, String username)
    {
        this.clientID = clientID;
        this.username = username;
    }

    public String toString()
    {
        return this.username + " (" + this.clientID + ")";
    }

    public void saveCrusade(BaseFile f) throws FileNotFoundException
    {
        saveCrusade(f, false);
    }

    public void saveCrusade(BaseFile f, boolean win) throws FileNotFoundException
    {
        if (Crusade.currentCrusade == null)
        {
            if (f.exists())
                f.delete();

            return;
        }

        f.startWriting();
        f.println(Crusade.currentCrusade.name);
        f.println(Crusade.currentCrusade.fileName);
        f.println(Crusade.currentCrusade.internal + "");
        f.println(Crusade.currentCrusade.saveLevel + "");

        if (Game.screen instanceof ScreenGame && !win && !Game.playerTank.destroy)
            remainingLives--;

        f.println(remainingLives + "");
        f.println(coins.coins + "");

        StringBuilder items = new StringBuilder();
        for (Item i: crusadeItemBar.slots)
        {
            items.append(i.name).append(",").append(i.stackSize).append("|");
        }

        f.println(items.substring(0, items.length() - 1));
        f.stopWriting();

        if ((remainingLives <= 0 || Crusade.currentCrusade.win) && f.exists())
            f.delete();
    }

    public void loadCrusade(BaseFile f) throws FileNotFoundException
    {
        if (!f.exists())
            return;

        f.startReading();
        String name = f.nextLine();
        String fileName = f.nextLine();
        boolean internal = Boolean.parseBoolean(f.nextLine());

        if (internal)
            Crusade.currentCrusade = new Crusade(Game.game.fileManager.getInternalFileContents(fileName), name, fileName);
        else
            Crusade.currentCrusade = new Crusade(Game.game.fileManager.getFile(fileName), name);

        Crusade.currentCrusade.currentLevel = Integer.parseInt(f.nextLine());
        Crusade.currentCrusade.saveLevel = Crusade.currentCrusade.currentLevel;
        this.remainingLives = Integer.parseInt(f.nextLine());
        this.coins.coins = Integer.parseInt(f.nextLine());

        String[] items = f.nextLine().split("\\|");

        ArrayList<Item> shop = Crusade.currentCrusade.getShop();

        for (int i = 0; i < items.length; i++)
        {
            String[] sec = items[i].split(",");
            String itemName = sec[0];
            int count = Integer.parseInt(sec[1]);

            for (Item it: shop)
            {
                if (it.name.equals(itemName))
                {
                    crusadeItemBar.slots[i] = Item.parseItem(this, it.toString());
                    crusadeItemBar.slots[i].stackSize = count;
                }
            }
        }

        crusadeItemBar.hotbar = Panel.panel.hotbar;

        f.stopReading();
    }
}
