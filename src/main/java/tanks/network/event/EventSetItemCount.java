package tanks.network.event;

import tanks.Game;

import java.util.UUID;

public class EventSetItemCount extends PersonalEvent implements IStackableEvent
{
    public int count;
    public UUID playerID;
    public int slot;

    public EventSetItemCount()
    {

    }

    public EventSetItemCount(int count, UUID playerID, int slot)
    {
        this.count = count;
        this.playerID = playerID;
        this.slot = slot;
    }

    @Override
    public int getIdentifier()
    {
        return IStackableEvent.f(playerID.hashCode()) + slot;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && this.playerID.equals(Game.clientID))
            Game.player.hotbar.itemBar.slots[slot].stackSize = count;
    }
}
