package tanks.network.event;

import tanks.*;

import java.util.UUID;

public class EventSetupHotbar extends PersonalEvent
{
    public UUID playerID;
    public boolean items;
    public boolean coins;

    public EventSetupHotbar()
    {

    }

    public EventSetupHotbar(Player p)
    {
        this.playerID = p.clientID;
        this.items = p.hotbar.itemBar.showItems;
        this.coins = p.hotbar.enabledCoins;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.player.clientID.equals(this.playerID))
        {
            Game.player.hotbar.itemBar.showItems = this.items;
            Game.player.hotbar.enabledCoins = this.coins;
        }
    }
}
