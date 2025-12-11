package tanks.network.event;

import tanks.*;

import java.util.UUID;

public class EventUpdateCoins extends PersonalEvent
{
    public UUID playerID;
    public int coins;

    public EventUpdateCoins()
    {

    }

    public EventUpdateCoins(Player p)
    {
        this.playerID = p.clientID;
        this.coins = p.hotbar.coins;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && this.playerID.equals(Game.clientID))
        {
            Game.player.hotbar.coins = coins;
        }
    }
}
