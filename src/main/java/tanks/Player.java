package tanks;

import tanks.hotbar.Coins;
import tanks.hotbar.ItemBar;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;

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
}
