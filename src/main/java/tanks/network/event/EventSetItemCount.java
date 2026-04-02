package tanks.network.event;

import tanks.*;

import java.util.UUID;

public class EventSetItemCount extends PersonalEvent
{
    public String name;
    public UUID playerID;
    public int slot;
    public int count;

    public EventSetItemCount()
    {

    }

    public EventSetItemCount(Player p, int slot, int count)
    {
        this.playerID = p.clientID;
        this.slot = slot;
        this.count = count;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && this.playerID.equals(Game.clientID))
        {
            Game.player.hotbar.itemBar.slots[slot].stackSize = this.count;
        }
    }
}
